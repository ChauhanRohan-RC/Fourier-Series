package main.models.rotor;

import main.models.function.ComplexDomainFunctionI;
import main.models.function.ComplexDomainFunctionWrapper;
import main.util.Listeners;
import main.util.async.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import main.util.ComplexUtil;
import main.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class StandardRotorStateManager extends ComplexDomainFunctionWrapper implements RotorStateManager {

    public static final String TAG = "StandardRotorStateManager";
    public static final int DEFAULT_INITIAL_ROTOR_COUNT = 200;

    public static final int MAX_ROTORS_LOAD_PER_THREAD = 80;

    @NotNull
    private final Map<Double, RotorState> mStore;
    private final int mInitialRotorCount;

    private volatile int mRotorCount;
    private volatile int mPendingRotorCount = -1;

    private volatile boolean mIsLoading;
    @Nullable
    private volatile Async.CExecutor mLoader;
    @NotNull
    private final Listeners<Listener> mListeners = new Listeners<>();
    private boolean mInitPending = true;


    private static void checkRotorCount(int rotorCount) {
        if (rotorCount < 0)
            throw new IllegalArgumentException("Rotor count must be positive, given " + rotorCount);
    }

    private static void checkIndex(int index, int count) {
        if (index < 0 || index >= count)
            throw new IndexOutOfBoundsException("Rotor index is out of bounds. Index: " + index + ", Count: " + count);
    }

    public StandardRotorStateManager(@NotNull ComplexDomainFunctionI f, int initialRotorCount) {
        super(f);
        mInitialRotorCount = initialRotorCount > 0? initialRotorCount: DEFAULT_INITIAL_ROTOR_COUNT;
        mStore = new HashMap<>(Math.max((int) (mInitialRotorCount * 1.4), 20));
//        setRotorCountAsync(mInitialRotorCount);
    }

    public StandardRotorStateManager(@NotNull ComplexDomainFunctionI f) {
        this(f, -1);
    }


    @Override
    public int getDefaultInitialRotorCount() {
        return mInitialRotorCount;
    }


    public void considerInitialize() {
        if (mInitPending) {
            setRotorCountAsync(mInitialRotorCount);
            mInitPending = false;
        }
    }

    @Override
    public int getRotorCount() {
        return mRotorCount;
    }

    @Override
    public int getPendingRotorCount() {
        return mPendingRotorCount;
    }


    @Override
    public double getRotorFrequency(int index, int count) {
        return RotorFrequencyProvider.centering(index, count) / getDomainRange();
    }

    @Nullable
    protected final RotorState getStoredRotorState(double frequency) {
        return mStore.get(frequency);
    }

    protected final boolean containsRotorState(double frequency) {
        return mStore.containsKey(frequency);
    }


    @NotNull
    protected RotorState createRotorState(double frequency) {
        final RotorState state = new RotorState(frequency, ComplexUtil.fourierSeriesCoefficient(getBaseFunction(), frequency));

        if (Log.DEBUG) {
            Log.v("RotorState", "Freq: " + frequency + ", coefficient: " + state.getCoefficient());
        }

        return state;
    }

    @NotNull
    private RotorState getRotorState(int index, int count) {
        checkIndex(index, count);
        final double frequency = getRotorFrequency(index, count);
        RotorState state = getStoredRotorState(frequency);

        if (state == null) {
            synchronized (mStore) {
                state = getStoredRotorState(frequency);
                if (state == null) {
                    state = createRotorState(frequency);            // heavy operation
                    mStore.put(frequency, state);
                }
            }
        }

        return state;
    }

    @Override
    @NotNull
    public RotorState getRotorState(int index) {
        return getRotorState(index, mRotorCount);
    }



    @Override
    public void addListener(@NotNull RotorStateManager.Listener l) {
        mListeners.addListener(l);
    }

    @Override
    public boolean removeListener(@NotNull RotorStateManager.Listener l) {
        return mListeners.removeListener(l);
    }

    @Override
    public boolean hasListener(@NotNull RotorStateManager.Listener l) {
        return mListeners.containsListener(l);
    }

    @Override
    public boolean isLoading() {
        return mIsLoading;
    }

    @Override
    public void cancelLoad(boolean interrupt) {
        final Async.CExecutor exe = mLoader;
        mLoader = null;

        if (exe != null) {
            exe.cancel(true);
        }
    }


    private void notifyListeners(@NotNull Consumer<Listener> action) {
        Async.postIfNotOnMainThread(() -> mListeners.forEachListener(action));
    }


    private volatile double mAllRotorsMagnitudeSum;

    private double computeRotorsMagnitudeSum(int count, int firstMultiplier) {
        double r = 0;

        if (count > 0) {
            r += getRotorState(0).getMagnitudeScale() * firstMultiplier;

            for (int i=1; i < count; i++) {
                r += getRotorState(i).getMagnitudeScale();
            }
        }

        return r;
    }

    @Override
    public final double getAllRotorsMagnitudeScaleSum() {
        final double cached = mAllRotorsMagnitudeSum;
        if (cached > 0)
            return cached;

        double r = computeRotorsMagnitudeSum(mRotorCount, 2);
        mAllRotorsMagnitudeSum = r;
        return r;
    }

    protected void onRotorCountUpdated(int prevCount, int newCount, boolean notify) {
        mAllRotorsMagnitudeSum = -1;        // invalidate
        getAllRotorsMagnitudeScaleSum();

        if (notify) {
            notifyListeners(l -> l.onRotorCountChanged(StandardRotorStateManager.this, newCount, prevCount));
        }
    }

    private void updateRotorsCount(int newCount, boolean notify) {
        final int prev = mRotorCount;
        if (newCount == prev)
            return;

        mRotorCount = newCount;
        onRotorCountUpdated(prev, newCount, notify);
    }

    protected void onLoaded(int count, int start, boolean cancelled, boolean setAfterLoad) {

        // Notify full loads
        if (start == 0) {
            final int pending = mPendingRotorCount;
            if (pending > 0 && pending <= count) {
                mPendingRotorCount = -1;
                if (setAfterLoad && !cancelled) {
                    updateRotorsCount(pending, true);
                }
            }

            notifyListeners(l -> l.onRotorsLoadFinished(StandardRotorStateManager.this, count, cancelled));
        }
    }



    private void doLoadRotorStates(int count, int start, @Nullable CancellationProvider c) {
        double frequency;
        for (int i = start; i < count && (c == null || !c.isCancelled()); i++) {
            frequency = getRotorFrequency(i, count);
            if (containsRotorState(frequency))
                continue;

            synchronized (mStore) {
                if (containsRotorState(frequency))
                    return;

                mStore.put(frequency, createRotorState(frequency));
            }
        }
    }




    private void loadSyncInternal(int count, int start, @Nullable CancellationProvider c, boolean setAfterLoad) {
//        if (cancelPrevLoad) {
//            cancelLoad(true);
//        }

        mIsLoading = true;

        final int range = count - start;
        final LinkedList<Callable<Object>> tasks = new LinkedList<>();

        for (int i=0; i < range / MAX_ROTORS_LOAD_PER_THREAD; i++) {
            final int _s = start + (i * MAX_ROTORS_LOAD_PER_THREAD);
            tasks.add(Executors.callable(() -> doLoadRotorStates(MAX_ROTORS_LOAD_PER_THREAD, _s, c)));
        }

        // last
        final int left = range % MAX_ROTORS_LOAD_PER_THREAD;
        if (left > 0) {
            tasks.add(Executors.callable(() -> doLoadRotorStates(left, count - left, c)));
        }

        final long startMs = System.currentTimeMillis();
        try {
            Async.THREAD_POOL_EXECUTOR.invokeAll(tasks);
        } catch (Throwable ignored) {
        }

        mIsLoading = false;
        final boolean cancelled = c != null && c.isCancelled();
        if (!cancelled) {
            Log.d(TAG, range + " fourier series coefficients loaded in " + (System.currentTimeMillis() - startMs) + "ms");
        }

        onLoaded(count, start, cancelled, setAfterLoad);
    }

    protected void loadSync(int count, @Nullable CancellationProvider c, boolean setAfterLoad) {
        final int pending = mPendingRotorCount;
        if (pending >= count)
            return;

        cancelLoad(true);
        mPendingRotorCount = count;
        loadSyncInternal(count, 0, c, setAfterLoad);
    }

    @Override
    public final void loadSync(int count, @Nullable CancellationProvider c) {
        loadSync(count, c, false);
    }

                            @NotNull
    private Canceller loadAsyncInternal(final int count, final int start, boolean setAfterLoad) {
//        if (cancelPrev) {
//            cancelLoad(true);      // cancel prev
//        }

        final Async.CExecutor exe = new Async.CExecutor();
        mLoader = exe;

        final CRun main = c -> loadSyncInternal(count, start, c, setAfterLoad);
        exe.execute(main);
        return exe;
    }

    @Nullable
    protected Canceller loadAsync(final int count, boolean setAfterLoad) {
        final int pending = mPendingRotorCount;
        if (pending >= count)
            return null;

        cancelLoad(true);
        mPendingRotorCount = count;
        return loadAsyncInternal(count, 0, setAfterLoad);
    }

    @Override
    @Nullable
    public final Canceller loadAsync(final int count) {
        return loadAsync(count, false);
    }


    private boolean shouldNotSetNewRotorCount(int newCount) {
        checkRotorCount(newCount);

        final int cur = mRotorCount;
        return cur == newCount;
    }

    @Override
    public final void setRotorCountSync(int count, @Nullable CancellationProvider c) {
        if (shouldNotSetNewRotorCount(count))
            return;

        loadSync(count, c, true);
    }

    @Nullable
    @Override
    public final Canceller setRotorCountAsync(int count) {
        if (shouldNotSetNewRotorCount(count))
            return null;

        return loadAsync(count, true);
    }


}
