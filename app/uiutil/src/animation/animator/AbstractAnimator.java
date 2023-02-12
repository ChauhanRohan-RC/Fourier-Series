package animation.animator;

import animation.interpolator.Interpolator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public abstract class AbstractAnimator<T> implements Runnable {

    /**
     * Defines how the animation ends
     * */
    public enum EndMode {
        /**
         * animation ended normally
         * */
        NORMAL,

        /**
         * animation ended forcefully
         * <br>
         * In this case, animation is brought to final state, and then ended
         * */
        FORCE,

        /**
         * animation ended after being cancelled
         * <br>
         * In this case, animation ends abruptly, and is <strong>NOT</strong> brought to final state.<br>
         * It is left in current state
         * */
        CANCEL
    }


    /**
     * Defines how animation behaves when it's primary iteration is finished
     * */
    public enum RepeatMode {
        /**
         * on finish, end the animation
         * */
        END("Finish"),

        /**
         * on finish, repeat animation from start
         * */
        REPEAT("Repeat"),

        /**
         * on finish, invert the animation
         * */
        CYCLE("Cycle");

        @NotNull
        public final String displayName;

        RepeatMode(@NotNull String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }



    @NotNull
    public static final Interpolator DEFAULT_INTERPOLATOR = Interpolator.LINEAR;
    public static final long DEFAULT_DURATION_MS = 300;

    private volatile T mStartVal, mEndValue;
    private volatile T mCurVal;
    private volatile long mDurationMs = DEFAULT_DURATION_MS;

    private volatile boolean mAnimating;
    private volatile boolean mPaused;
    private volatile boolean mEnded;

    @NotNull
    private volatile RepeatMode mRepeatMode = RepeatMode.END;
    private volatile int mTotalRepeatCount = -1;      // < 0 for indefinite
    private volatile int mCurRepetitionCount;

    @Nullable
    private volatile Long mStartMs;
    @Nullable
    private volatile Long mPausedMs;

    @NotNull
    private volatile Interpolator mDefaultInterpolator = DEFAULT_INTERPOLATOR;
    @Nullable
    private volatile Interpolator mInterpolator;

    @Nullable
    private volatile Object mTag;

    protected AbstractAnimator(@NotNull T startVal, @NotNull T endValue) {
        mStartVal = startVal;
        mEndValue = endValue;

        mCurVal = getStartValue();
    }

    public final T getActualStartValue() {
        return mStartVal;
    }

    public final T getActualEndValue() {
        return mEndValue;
    }

    public AbstractAnimator<T> setActualStartValue(T startVal) {
        mStartVal = startVal;
        return this;
    }

    public AbstractAnimator<T> setActualEndValue(T endValue) {
        mEndValue = endValue;
        return this;
    }

    public boolean isInverted() {
        return mRepeatMode == RepeatMode.CYCLE && (mCurRepetitionCount % 2 != 0);
    }

    @NotNull
    public final T getStartValue() {
        return isInverted()? mEndValue: mStartVal;
    }

    @NotNull
    public final T getEndValue() {
        return isInverted()? mStartVal: mEndValue;
    }


    public final void copyAttributes(@NotNull AbstractAnimator<?> animator) {
        setDurationMs(animator.getDurationMs())
                .setInterpolator(animator.getInterpolator())
                .setDefaultInterpolator(animator.getDefaultInterpolator())
                .setRepeatMode(animator.getRepeatMode())
                .setRepeatCount(animator.getRepeatCount());
    }

    @NotNull
    public final T getCurrentValue() {
        return mCurVal;
    }

    public final long getDurationMs() {
        return mDurationMs;
    }

    public final AbstractAnimator<T> setDurationMs(long durationMs) {
        final long dur = mDurationMs;
        if (dur != durationMs) {
            mDurationMs = durationMs;
            doDurationChangedInternal(dur, durationMs);
        }

        return this;
    }


    @NotNull
    public final Interpolator getDefaultInterpolator() {
        return mDefaultInterpolator;
    }

    public final AbstractAnimator<T> setDefaultInterpolator(@Nullable Interpolator defaultInterpolator) {
        if (defaultInterpolator == null) {
            defaultInterpolator = DEFAULT_INTERPOLATOR;
        }

        final Interpolator prev = mDefaultInterpolator;
        if (!defaultInterpolator.equals(prev)) {
            mDefaultInterpolator = defaultInterpolator;
            onDefaultInterpolatorChanged();
        }

        return this;
    }

    @NotNull
    public final Interpolator getInterpolator() {
        final Interpolator ip = mInterpolator;
        return ip != null? ip: mDefaultInterpolator;
    }

    public final AbstractAnimator<T> setInterpolator(@Nullable Interpolator interpolator) {
        final Interpolator prev = getInterpolator();
        mInterpolator = interpolator;
        final Interpolator _new = getInterpolator();
        if (!prev.equals(_new)) {
            onInterpolatorChanged();
        }

        return this;
    }

    public final int getRepeatCount() {
        return mTotalRepeatCount;
    }

    /**
     * Set how many times animation should be repeated (excluding first iteration)
     *
     * @param repeatCount how many times animation should be repeated, or {@code -1} to repeat forever
     * */
    public final AbstractAnimator<T> setRepeatCount(int repeatCount) {
        if (repeatCount < 0) {
            repeatCount = -1;
        }

        final int prev = mTotalRepeatCount;
        if (prev != repeatCount) {
            mTotalRepeatCount = repeatCount;
            onRepeatCountChanged();
        }

        return this;
    }

    /**
     * @return current repetition number
     * */
    public final int getCurrentRepetitionCount() {
        return mCurRepetitionCount;
    }

    @NotNull
    public RepeatMode getRepeatMode() {
        return mRepeatMode;
    }

    public AbstractAnimator<T> setRepeatMode(@NotNull RepeatMode repeatMode) {
        final RepeatMode prev = mRepeatMode;
        if (prev != repeatMode) {
            mRepeatMode = repeatMode;
            onRepeatModeChanged();
        }

        return this;
    }

    @Nullable
    public final Object getTag() {
        return mTag;
    }

    public final void setTag(Object tag) {
        mTag = tag;
    }


    public boolean isRunning() {
        return mAnimating;
    }

    public final boolean isPaused() {
        return mPaused;
    }

    public final boolean isEnded() {
        return mEnded;
    }


    /**
     * Resets this animator to initial state
     * This means that animator will be stopped, and brought back to state when it was first instantiated
     *
     * @return whether animator was running when reset is called
     *
     * @see #backToStart()
     * */
    public final boolean reset() {
        final boolean wasRunning = isRunning();
        mAnimating = false;
        mPaused = false;
        mEnded = false;
        mCurRepetitionCount = 0;        // set before getting start value, to full reset to start state
        mCurVal = getStartValue();
        mStartMs = null;
        mPausedMs = null;
        onReset(wasRunning);
        return wasRunning;
    }

    /**
     * Alternative to {@link #reset()}.
     * It does not stop the animator, only brings it back to starting point
     * <strong>It also resets any repetitions</strong>
     *
     * @see #reset()
     * */
    public final void backToStart() {
        mCurRepetitionCount = 0;        // set before getting start value, to full reset to start state
        mCurVal = getStartValue();
        mStartMs = System.currentTimeMillis();
        mPausedMs = null;        // do not want to add pause it
    }

    /**
     * Sets the elapsed fraction of the current repetition.<br>
     * There are 3 possible cases
     * <pre>
     *     1. If the animation is running (i.e {@link #isRunning()} returns true), it simply seeks the animation to given {@code elapsedFraction} (in time)<br>
     *     2. If animation is not running
     *     <pre>
     *         1. If {@code onlyIfRunning} is true, then does nothing
     *         2. else the animation will start form the given {@code elapsedFraction} next time {@link #start()} is called
     *     </pre>
     * </pre>
     *
     * @param elapsedFraction the elapsed fraction (in time) of the current repetition
     * @param onlyIfRunning true to set {@code elapsedFraction} only if animation is running
     * */
    public final void setElapsedFraction(float elapsedFraction, boolean onlyIfRunning) {
        if (onlyIfRunning) {
            if (!isRunning())
                return;

//            final Long start = mStartMills;
//            if (start == null)
//                return;
        }

        final long newStart = System.currentTimeMillis() - ((long) (elapsedFraction * mDurationMs));
        mStartMs = newStart;
        mPausedMs = null;       // do not want to add pause it
    }


    public final void start() {
        if (mEnded || mAnimating)
            return;

        final boolean paused = mPaused;
        boolean resumed = false;

        Long startMs = mStartMs;
        Long pausedMs = mPausedMs;
        if (startMs == null) {
            startMs = System.currentTimeMillis();       // first start
            mStartMs = startMs;
            mCurVal = getStartValue();
        } else if (paused && pausedMs != null) {
            startMs += (System.currentTimeMillis() - pausedMs);        // add paused duration
            mStartMs = startMs;
            resumed = true;
        }

        mPausedMs = null;
        mAnimating = true;
        mPaused = false;
        onStarted(resumed);
    }

    public final void pause() {
        if (!mAnimating)
            return;

        mPausedMs = System.currentTimeMillis();
        mAnimating = false;
        mPaused = true;
        onPaused();
    }

    protected void updateCurrentValue(T value) {
        mCurVal = value;
        onValueUpdate();
    }


    /**
     * Interpolates the domain range (StartValue -> EndValue) with the given elapsed fraction in time
     * */
    public abstract T interpolateValue(float elapsedFraction);

    protected void doUpdate(float elapsedFraction) {
        final T val = interpolateValue(elapsedFraction);
        updateCurrentValue(val);
    }

    public final boolean update() {
        final Long startMs = mStartMs;
        if (mEnded || !mAnimating || mPaused || startMs == null)
            return false;

        final long durationMs = mDurationMs;

        final long now = System.currentTimeMillis();
        float fraction = (float) (now - startMs) / durationMs;
        if (fraction < 0) {
            // bad state
            return false;
        }

        if (fraction <= 1) {
            doUpdate(fraction);
        }

        if (fraction >= 1) {
            onLoopFinished();
        }

        return true;
    }

    private void onLoopFinished() {
        switch (mRepeatMode) {
            case END -> doEndInternal(EndMode.NORMAL);
            case REPEAT, CYCLE -> {
                final int totalRepeatCount = mTotalRepeatCount;
                final int curRepeatCount = mCurRepetitionCount;
                if (totalRepeatCount >= 0 && totalRepeatCount <= curRepeatCount) {
                    doEndInternal(EndMode.NORMAL);
                } else {
                    doRepeatInternal();
                }
            }
        }
    }

    @Override
    public void run() {
        update();
    }


    @NotNull
    public final T updateAndGetCurrentValue() {
        update();
        return mCurVal;
    }

    private void doRepeatInternal() {
        final int curRepCount = mCurRepetitionCount;
        mCurRepetitionCount = curRepCount + 1;
        mCurVal = getStartValue();
        mStartMs = System.currentTimeMillis();
        mPausedMs = null;
        onRepeat();
    }

    private void end(boolean cancel) {
        if (mEnded)
            return;

        doEndInternal(cancel? EndMode.CANCEL: EndMode.FORCE);
    }

    public final void cancel() {
        end(true);
    }

    public final void forceFinish() {
        end(false);
    }


    private void doEndInternal(@NotNull AbstractAnimator.EndMode endMode) {
        mAnimating = false;
        mPaused = false;
        mPausedMs = null;

        if (endMode != EndMode.CANCEL) {
            mCurVal = getEndValue();
            mCurRepetitionCount = 0;            // reset after getEndVal to get end value of current iteration
        }

        mEnded = true;
        onEnd(endMode);
    }


    /* Callbacks */

    private void doDurationChangedInternal(long previousDurationMs, long newDurationMs) {
        final Long start = mStartMs;
        if (start != null) {
            // Change start time such that elapsed fraction remains same with newDuration
            final double fraction = (double) newDurationMs / previousDurationMs;
            final long start2 = (long) ((start * fraction) + (System.currentTimeMillis() * (1 - fraction)));
            mStartMs = start2;
        }

        onDurationChanged(previousDurationMs, newDurationMs);
    }

    protected void onStarted(boolean resumed) {
    }

    protected void onPaused() {
    }

    protected void onRepeat() {
    }

    protected void onReset(boolean wasRunning) {
    }

    protected void onEnd(@NotNull AbstractAnimator.EndMode endMode) {
    }


    protected void onValueUpdate() {
    }

    protected void onDurationChanged(long previousDurationMs, long newDurationMs) {
    }

    protected void onRepeatModeChanged() {
    }

    protected void onRepeatCountChanged() {
    }

    protected void onInterpolatorChanged() {
    }

    protected void onDefaultInterpolatorChanged() {
    }


    @Override
    public String toString() {
        return "AbstractAnimator{" +
                "StartVal=" + mStartVal +
                ", EndValue=" + mEndValue +
                ", CurVal=" + mCurVal +
                ", DurationMs=" + mDurationMs +
                ", Animating=" + mAnimating +
                ", Ended=" + mEnded +
                ", RepeatMode=" + mRepeatMode +
                ", TotalRepeatCount=" + mTotalRepeatCount +
                ", CurRepetitionCount=" + mCurRepetitionCount +
                ", StartMills=" + mStartMs +
                ", PausedMs=" + mPausedMs +
                ", DefaultInterpolator=" + mDefaultInterpolator +
                ", Interpolator=" + mInterpolator +
                ", Tag=" + mTag +
                '}';
    }
}
