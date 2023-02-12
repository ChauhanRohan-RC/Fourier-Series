package rotor;

import app.Settings;
import function.ComplexDomainFunctionWrapper;
import function.definition.CacheRotorStateProvider;
import function.definition.ComplexDomainFunctionI;

import misc.CollectionUtil;
import misc.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import provider.FunctionMeta;
import rotor.frequency.RotorFrequencyProviderI;
import ui.AuxSoundsPlayer;
import live.Listeners;
import async.*;
import util.main.ComplexUtil;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class StandardRotorStateManager extends ComplexDomainFunctionWrapper implements RotorStateManager, CacheRotorStateProvider, Settings.Listener {

    public static final String TAG = "StandardRotorStateManager";

    private static int sIdGen = 10;

    private static synchronized int nextId() {
        return sIdGen++;
    }

    public static final int DEFAULT_INITIAL_ROTOR_COUNT = 200;

    public static final int MAX_ROTORS_LOAD_PER_THREAD = 80;
    public static final boolean SYNCHRONISE_ROTORS_BATCH_LOAD = false;          // synchronised load takes a lot of time


    private final int id;
    private final Object storeLock = new Object();
    private final Object rotorCountLock = new Object();

    @NotNull
    private final FunctionMeta functionMeta;
    @NotNull
    private final RotorFrequencyProviderI mDefaultRotorFrequencyProvider;
    @Nullable
    private volatile RotorFrequencyProviderI mRotorFrequencyProvider;
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

    public StandardRotorStateManager(@NotNull ComplexDomainFunctionI f, @NotNull FunctionMeta functionMeta, int initialRotorCount) {
        super(f);
        this.id = nextId();
        this.functionMeta = functionMeta;

        if (initialRotorCount < 0) {
            initialRotorCount = functionMeta.initialRotorCount();
        }

        mInitialRotorCount = initialRotorCount < 0? DEFAULT_INITIAL_ROTOR_COUNT: initialRotorCount;
        mStore = new ConcurrentHashMap<>(Math.max((int) (mInitialRotorCount * 1.4), 20));       // concurrency

        // Meta
        RotorFrequencyProviderI defaultFreqProvider = getFunctionDefaultFrequencyProvider();
        if (defaultFreqProvider == null) {
            defaultFreqProvider = ComplexDomainFunctionI.getDefaultFrequencyProvider(getDomainRange());
        }

        mDefaultRotorFrequencyProvider = defaultFreqProvider;

        final RotorFrequencyProviderI frequencyProvider = functionMeta.frequencyProvider();
        if (frequencyProvider != null) {
            mRotorFrequencyProvider = frequencyProvider;
        }

        // Preloaded States
        addRotorStates(functionMeta.preloadedRotorStates());

//        setRotorCountAsync(mInitialRotorCount);
        Settings.getSingleton().addListener(this);
    }

    public StandardRotorStateManager(@NotNull ComplexDomainFunctionI f, @NotNull FunctionMeta functionMeta) {
        this(f, functionMeta, -1);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    @NotNull
    public final FunctionMeta getFunctionMeta() {
        return functionMeta;
    }

    @Override
    public @NotNull ComplexDomainFunctionI getFunction() {
        return getBaseFunction();
    }

    @Override
    public final int getDefaultInitialRotorCount() {
        return mInitialRotorCount;
    }

    public final void considerInitialize() {
        if (mInitPending) {
            final LoadCallResult result = setRotorCountAsync(mInitialRotorCount);
            if (result == LoadCallResult.QUEUED || result == LoadCallResult.REDUNDANT) {
                mInitPending = false;
            }
        }
    }

    @Override
    public final int getRotorCount() {
        return mRotorCount;
    }

    @Override
    public final int getPendingRotorCount() {
        return mPendingRotorCount;
    }


    protected void onRotorFrequencyProviderChanged(@Nullable RotorFrequencyProviderI old, @Nullable RotorFrequencyProviderI _new) {
        Log.v(TAG, String.format("Rotor Frequency Provider changed. Function: %s | Old Frequency Provider: %s | New Frequency Provider: %s", functionMeta.getTypedFunctionDisplayName(), old, _new));
        reloadAsync();
        mListeners.dispatchOnMainThread(l -> l.onRotorsFrequencyProviderChanged(StandardRotorStateManager.this, old, _new));

        AuxSoundsPlayer.getSingleton().playBeep();
    }

    protected void onRotorFrequencyProviderIntercepted(@Nullable RotorFrequencyProviderI rotorFrequencyProvider) {
        Log.v(TAG, String.format("Rotor Frequency Provider intercepted. Function: %s | Frequency Provider: %s", functionMeta.getTypedFunctionDisplayName(), rotorFrequencyProvider));
        mListeners.dispatchOnMainThread(l -> l.onRotorsFrequencyProviderIntercepted(StandardRotorStateManager.this, rotorFrequencyProvider));
    }

    @Override
    @NotNull
    public RotorFrequencyProviderI getManagerDefaultRotorFrequencyProvider() {
        return mDefaultRotorFrequencyProvider;
    }

    @Override
    @Nullable
    public RotorFrequencyProviderI getManagerRotorFrequencyProvider() {
        return mRotorFrequencyProvider;
    }


    private boolean doInterceptRotorFrequencyProvider(@Nullable RotorFrequencyProviderI old, @Nullable RotorFrequencyProviderI _new) {
        if (mListeners.listenersCount() > 0) {
            for (Listener l: mListeners.iterationCopy()) {
                if (l.onInterceptRotorFrequencyProvider(StandardRotorStateManager.this, old, _new))
                    return true;
            }
        }

        return false;
    }

    public final void setRotorFrequencyProvider(@Nullable RotorFrequencyProviderI rotorFrequencyProvider) {
        final RotorFrequencyProviderI old = mRotorFrequencyProvider;
        if (Objects.equals(old, rotorFrequencyProvider)) {
            return;
        }

        if (doInterceptRotorFrequencyProvider(old, rotorFrequencyProvider)) {
            onRotorFrequencyProviderIntercepted(rotorFrequencyProvider);
            return;
        }

        mRotorFrequencyProvider = rotorFrequencyProvider;
        onRotorFrequencyProviderChanged(old, rotorFrequencyProvider);
    }



    @Override
    public final boolean containsCachedRotorState(double frequency) {
        return mStore.containsKey(frequency) || getBaseFunction().containsCachedRotorState(frequency);
    }

    @Nullable
    @Override
    public final RotorState getCachedRotorState(double frequency) {
        RotorState state = mStore.get(frequency);
        if (state == null) {
            synchronized (storeLock) {
                state = mStore.get(frequency);
                if (state == null) {
                    state = getBaseFunction().getCachedRotorState(frequency);
                    if (state != null) {
                        mStore.put(frequency, state);
                    }
                }
            }
        }

        return state;
    }



//    @Override
//    @NotNull
//    public Map<Double, RotorState> copyRotorStates() {
//        synchronized (storeLock) {
//            return new HashMap<>(mStore);
//        }
//    }


    @Override
    public void forEachRotorState(@NotNull Consumer<RotorState> consumer) {
        synchronized (storeLock) {
            mStore.forEach((k, v) -> consumer.consume(v));
        }
    }

    @Override
    public void copyAllRotorStates(@NotNull Collection<RotorState> dest) {
        synchronized (storeLock) {
            dest.addAll(mStore.values());
        }
    }

    @Override
    public void copyAllRotorStates(@NotNull Map<? super Double, ? super RotorState> dest) {
        synchronized (storeLock) {
            dest.putAll(mStore);
        }
    }

    @NotNull
    protected RotorState createRotorState(double frequency) {
        final RotorState state = new RotorState(frequency, ComplexUtil.fourierSeriesCoefficient(getBaseFunction(), frequency));

//        if (Log.DEBUG) {
//            Log.v("RotorState", "Freq: " + frequency + ", mag: " + state.getMagnitudeScale() + ", phase: " + state.getCoefficientArgument());
//        }

        return state;
    }

    @NotNull
    private RotorState getRotorState(int index, int count) {
        checkIndex(index, count);
        final double frequency = getRotorFrequency(index, count);
        RotorState state = getCachedRotorState(frequency);

        if (state == null) {
            synchronized (storeLock) {
                state = createRotorState(frequency);            // heavy operation
                mStore.put(frequency, state);
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
    public boolean containsListener(@NotNull RotorStateManager.Listener l) {
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
            mListeners.dispatchOnMainThread(l -> l.onRotorsCountChanged(StandardRotorStateManager.this, prevCount, newCount));
        }
    }

    private void updateRotorsCount(int newCount, boolean notify) {
        int prev = mRotorCount;
        if (newCount == prev)
            return;

        synchronized (rotorCountLock) {
            prev = mRotorCount;
            if (newCount == prev)
                return;

            mRotorCount = newCount;
            onRotorCountUpdated(prev, newCount, notify);
        }
    }

    protected void onLoaded(int startIndex, int totalLoadCount, boolean cancelled, boolean setAfterLoad, boolean notifyLoadEnded) {
        // Notify full loads
        if (startIndex == 0) {
            final int pending = mPendingRotorCount;
            if (pending >= 0 && pending <= totalLoadCount) {
                mPendingRotorCount = -1;
                if (setAfterLoad && !cancelled) {
                    updateRotorsCount(pending, true);
                }
            }

            mListeners.dispatchOnMainThread(l -> {
                l.onRotorsLoadFinished(StandardRotorStateManager.this, totalLoadCount, cancelled);
                if (notifyLoadEnded) {
                    l.onRotorsLoadingChanged(StandardRotorStateManager.this, false);
                }
            });
        }
    }




    private void doLoadRotorStateInternal(double frequency) {
        if (containsCachedRotorState(frequency))
            return;

        mStore.put(frequency, createRotorState(frequency));
    }


    private void doLoadRotorState(double frequency, boolean _synchronized) {
        if (_synchronized) {
            synchronized (storeLock) {
                doLoadRotorStateInternal(frequency);
            }
        } else {
            doLoadRotorStateInternal(frequency);
        }
    }

    private void doLoadRotorStates(int startIndex, int endIndex, int totalLoadCount, @Nullable CancellationProvider c) {
        double frequency;
        for (int i = startIndex; i < endIndex && (c == null || !c.isCancelled()); i++) {
            frequency = getRotorFrequency(i, totalLoadCount);

            doLoadRotorState(frequency, SYNCHRONISE_ROTORS_BATCH_LOAD);
        }
    }




    private void loadSyncInternal(int startIndex, int totalLoadCount, @Nullable CancellationProvider c, boolean setAfterLoad) {
//        if (cancelPrevLoad) {
//            cancelLoad(true);
//        }

        mIsLoading = true;
        Log.v(TAG, String.format("Loading %d Rotor States for function %s", totalLoadCount, functionMeta.getTypedFunctionDisplayName()));
        mListeners.dispatchOnMainThread(l -> l.onRotorsLoadingChanged(StandardRotorStateManager.this, true));

        final int chunkSize = MAX_ROTORS_LOAD_PER_THREAD;
        final LinkedList<Callable<Object>> tasks = new LinkedList<>();

        for (int i=0; i < totalLoadCount / chunkSize; i++) {
            final int loadStart = startIndex + (i * chunkSize);
            tasks.add(Executors.callable(() -> doLoadRotorStates(loadStart, loadStart + chunkSize, totalLoadCount, c)));
        }

        // last
        final int left = totalLoadCount % chunkSize;
        if (left > 0) {
            tasks.add(Executors.callable(() -> doLoadRotorStates(totalLoadCount - left, totalLoadCount, totalLoadCount, c)));
        }

        final long startMs = System.currentTimeMillis();
        if (!tasks.isEmpty()) {
            try {
                Async.THREAD_POOL_EXECUTOR.invokeAll(tasks);
//            for (Future<Object> f: fts) {
//                if (!f.isDone())
//                    f.get();
//            }
//            for (Callable<Object> task: tasks) {
//                task.call();
//            }
            } catch (Throwable ignored) {
            }
        }


//        mIsLoading = false;
//        notifyListeners(l -> l.onRotorsLoadingChanged(false));
        final boolean cancelled = c != null && c.isCancelled();
        if (!cancelled) {
            Log.v(TAG,  String.format("%d Rotor States for function [%s] loaded in %d ms", totalLoadCount, functionMeta.getTypedFunctionDisplayName(), (System.currentTimeMillis() - startMs)));
        } else {
            Log.v(TAG, "Rotor States load for function " + functionMeta.getTypedFunctionDisplayName() + " CANCELLED");
        }

        mIsLoading = false;
        onLoaded(startIndex, totalLoadCount, cancelled, setAfterLoad, true);
    }


    private boolean doInterceptRotorsLoad(int loadCount) {
        if (mListeners.listenersCount() > 0) {
            for (Listener l: mListeners.iterationCopy()) {
                if (l.onInterceptRotorsLoad(StandardRotorStateManager.this, loadCount))
                    return true;
            }
        }

        return false;
    }

    /**
     * Called when rotors load is cancelled by any of the registered load interceptor
     **/
    protected void onRotorsLoadIntercepted(int loadCount) {
        Log.v(TAG, String.format("%d Rotors Load for function [%s] intercepted", loadCount, functionMeta.getTypedFunctionDisplayName()));
        mListeners.dispatchOnMainThread(l -> l.onRotorsLoadIntercepted(StandardRotorStateManager.this, loadCount));
    }

    protected void loadSync(int loadCount, @Nullable CancellationProvider c, boolean setAfterLoad, @Nullable Consumer<LoadCallResult> resultConsumer) {
        final int pending = mPendingRotorCount;
        if (pending >= loadCount) {
            if (resultConsumer != null)
                resultConsumer.consume(LoadCallResult.REDUNDANT);
            return;
        }

        if (doInterceptRotorsLoad(loadCount)) {
            onRotorsLoadIntercepted(loadCount);
            if (resultConsumer != null)
                resultConsumer.consume(LoadCallResult.INTERCEPTED);
            return;
        }

        cancelLoad(true);
        mPendingRotorCount = loadCount;
        if (resultConsumer != null)
            resultConsumer.consume(LoadCallResult.QUEUED);
        loadSyncInternal(0, loadCount, c, setAfterLoad);
    }

    @Override
    public void loadSync(int count, @Nullable CancellationProvider c, @Nullable Consumer<LoadCallResult> callResultCallback) {
        loadSync(count, c, false, callResultCallback);
    }

    private void loadAsyncInternal(final int startIndex, final int totalLoadCount, boolean setAfterLoad) {
//        if (cancelPrev) {
//            cancelLoad(true);      // cancel prev
//        }

        final Async.CExecutor exe = new Async.CExecutor();
        cancelLoad(true);
        mLoader = exe;

        final CRun main = c -> loadSyncInternal(startIndex, totalLoadCount, c, setAfterLoad);
        exe.execute(main);
    }

    @NotNull
    protected LoadCallResult loadAsync(final int loadCount, boolean setAfterLoad) {
        final int pending = mPendingRotorCount;
        if (pending >= loadCount) {
            return LoadCallResult.REDUNDANT;
        }

        if (doInterceptRotorsLoad(loadCount)) {
            onRotorsLoadIntercepted(loadCount);
            return LoadCallResult.INTERCEPTED;
        }

        cancelLoad(true);
        mPendingRotorCount = loadCount;
        loadAsyncInternal(0, loadCount, setAfterLoad);
        return LoadCallResult.QUEUED;
    }

    @Override
    @NotNull
    public final LoadCallResult loadAsync(final int loadCount) {
        return loadAsync(loadCount, false);
    }

    @Override
    public void reloadAsync() {
        final int count = mRotorCount;
        if (count == 0)
            return;

        cancelLoad(true);
        mPendingRotorCount = count;
        loadAsyncInternal(0, count, true);
    }



    private boolean shouldNotSetNewRotorCount(int newCount) {
        checkRotorCount(newCount);

        final int cur = mRotorCount;
        return cur == newCount;
    }

    @Override
    public final void setRotorCountSync(int count, @Nullable CancellationProvider c, @Nullable Consumer<LoadCallResult> callResultCallback) {
        if (shouldNotSetNewRotorCount(count)) {
            if (callResultCallback != null) {
                callResultCallback.consume(LoadCallResult.REDUNDANT);
            }
            return;
        }

        loadSync(count, c, true, callResultCallback);
    }

    @NotNull
    @Override
    public final LoadCallResult setRotorCountAsync(int count) {
        if (shouldNotSetNewRotorCount(count))
            return LoadCallResult.REDUNDANT;

        return loadAsync(count, true);
    }

    @Override
    public @NotNull FunctionState createFunctionState(@Nullable String funcName) {
        return FunctionState.from(this, funcName);
    }

    @Override
    public int getAllLoadedRotorStatesCount() {
        return mStore.size();
    }

    @Override
    public void clearAndResetSync() {
        cancelLoad(true);
        mPendingRotorCount = -1;

        updateRotorsCount(0, true);
        synchronized (storeLock) {
            mStore.clear();
        }
    }

    @Override
    public int addRotorStates(Collection<RotorState> states) {
        if (CollectionUtil.isEmpty(states))
            return 0;

        synchronized (storeLock) {
            states.forEach(s -> mStore.put(s.getFrequency(), s));
            return states.size();
        }
    }


    @Override
    public void onLookAndFeelChanged(@NotNull String className) {

    }

    @Override
    public void onAppearancePreferencesChanged() {

    }

    @Override
    public void onFTIntegrationIntervalCountChanged(int fourierTransformSimpson13NDefault) {
        clearAndReloadAsync();
    }

    @Override
    public void onConfigPreferencesChanged() {

    }

    @Override
    public void onLogPreferencesChanged() {

    }

    @Override
    public void onSoundPreferencesChanged() {

    }

    @Override
    public void onOtherPreferencesChanged() {

    }
}
