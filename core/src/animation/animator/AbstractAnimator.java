package animation.animator;

import animation.interpolator.Interpolator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.Log;


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
    private volatile boolean mEnded;

    @NotNull
    private volatile RepeatMode mRepeatMode = RepeatMode.END;
    private volatile int mTotalRepeatCount = -1;      // < 0 for indefinite
    private volatile int mCurRepetitionCount;

    @Nullable
    private volatile Long mStartMills;
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

    protected final AbstractAnimator<T> setDefaultInterpolator(@Nullable Interpolator defaultInterpolator) {
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
        return mPausedMs != null && !(mAnimating || mEnded);
    }

    public final boolean isEnded() {
        return mEnded;
    }

    public final void backToStart() {
        mCurRepetitionCount = 0;        // set before getting start value, to full reset to start state
        mCurVal = getStartValue();
        mStartMills = System.currentTimeMillis();
    }

    /**
     * Resets this animator to initial state
     * This means that animator will be stopped, and brought back to state when it was first instantiated
     *
     * @return whether animator was running when reset is called
     * */
    public final boolean reset() {
        final boolean wasRunning = isRunning();
        mAnimating = false;
        mEnded = false;
        mCurRepetitionCount = 0;        // set before getting start value, to full reset to start state
        mCurVal = getStartValue();
        mStartMills = null;
        mPausedMs = null;
        onReset(wasRunning);
        return wasRunning;
    }

    public final void start() {
        if (mEnded || mAnimating)
            return;

        boolean resumed = false;
        Long startMs = mStartMills;
        Long pausedMs = mPausedMs;
        if (startMs == null) {
            startMs = System.currentTimeMillis();       // first start
            mStartMills = startMs;
            mCurVal = getStartValue();
        } else if (pausedMs != null) {
            startMs += (System.currentTimeMillis() - pausedMs);        // add paused duration
            mStartMills = startMs;
            resumed = true;
        }

        mPausedMs = null;
        mAnimating = true;
        onStarted(resumed);
    }

    public final void pause() {
        if (!mAnimating)
            return;

        mPausedMs = System.currentTimeMillis();
        mAnimating = false;
        onPaused();
    }

    protected void updateCurValue(T value) {
        mCurVal = value;
        onValueUpdate();
    }

    protected abstract void doUpdate(float elapsedFraction);

    public final boolean update() {
        final Long startMs = mStartMills;
        if (mEnded || !mAnimating || startMs == null)
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
        mStartMills = System.currentTimeMillis();
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
        final Long start = mStartMills;
        if (start != null) {
            // Change start time such that elapsed fraction remains same with newDuration
            final double fraction = (double) newDurationMs / previousDurationMs;
            final long start2 = (long) ((start * fraction) + (System.currentTimeMillis() * (1 - fraction)));
            mStartMills = start2;
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
                ", StartMills=" + mStartMills +
                ", PausedMs=" + mPausedMs +
                ", DefaultInterpolator=" + mDefaultInterpolator +
                ", Interpolator=" + mInterpolator +
                ", Tag=" + mTag +
                '}';
    }
}
