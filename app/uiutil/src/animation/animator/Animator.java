package animation.animator;

import org.jetbrains.annotations.NotNull;
import async.Consumer;
import live.Listeners;

public abstract class Animator<T> extends AbstractAnimator<T> {

    public interface AnimationListener<T> {

        void onStarted(@NotNull Animator<T> animator, boolean resumed);

        void onRepeat(@NotNull Animator<T> animator);

        void onPaused(@NotNull Animator<T> animator);

        void onEnd(@NotNull Animator<T> animator, @NotNull EndMode endMode);

        void onReset(@NotNull Animator<T> animator, boolean wasRunning);

        // called on current thread, possibly background
        void onAnimationUpdate(@NotNull Animator<T> animator);

        void onDurationChanged(@NotNull Animator<T> animator);

        void onRepeatModeChanged(@NotNull Animator<T> animator);

        void onRepeatCountChanged(@NotNull Animator<T> animator);

        void onInterpolatorChanged(@NotNull Animator<T> animator);

        void onDefaultInterpolatorChanged(@NotNull Animator<T> animator);
    }


    public interface AnimationListenerAdapter<T> extends AnimationListener<T> {

        default void onStarted(@NotNull Animator<T> animator, boolean resumed) { }

        default void onRepeat(@NotNull Animator<T> animator) { }

        default void onPaused(@NotNull Animator<T> animator) { }

        default void onEnd(@NotNull Animator<T> animator, @NotNull EndMode endMode) { }

        @Override
        default void onReset(@NotNull Animator<T> animator, boolean wasRunning) {
        }

        // called on current thread, possibly background
        default void onAnimationUpdate(@NotNull Animator<T> animator) { }

        @Override
        default void onDurationChanged(@NotNull Animator<T> animator) { }

        @Override
        default void onRepeatModeChanged(@NotNull Animator<T> animator) { }

        @Override
        default void onRepeatCountChanged(@NotNull Animator<T> animator) { }

        @Override
        default void onInterpolatorChanged(@NotNull Animator<T> animator) { }

        @Override
        default void onDefaultInterpolatorChanged(@NotNull Animator<T> animator) { }

    }

    @FunctionalInterface
    public interface AnimationUpdateListener<T> extends AnimationListenerAdapter<T> {

        void onAnimationUpdate(@NotNull Animator<T> animator);

        default void onStarted(@NotNull Animator<T> animator, boolean resumed) { }

        default void onRepeat(@NotNull Animator<T> animator) { }

        default void onPaused(@NotNull Animator<T> animator) { }

        default void onEnd(@NotNull Animator<T> animator, @NotNull EndMode endMode) { }

        @Override
        default void onReset(@NotNull Animator<T> animator, boolean wasRunning) { }
    }



    @NotNull
    private final Listeners<AnimationListener<T>> mAnimListeners = new Listeners<>();

    protected Animator(@NotNull T startVal, @NotNull T endValue) {
        super(startVal, endValue);
    }

    public boolean containsAnimationListener(@NotNull AnimationListener<T> animationListener) {
        return mAnimListeners.containsListener(animationListener);
    }

    public Animator<T> addAnimationListener(@NotNull AnimationListener<T> animationListener) {
        mAnimListeners.addListener(animationListener);
        return this;
    }

    public boolean removeAnimationListener(@NotNull AnimationListener<T> animationListener) {
        return mAnimListeners.removeListener(animationListener);
    }

    public Animator<T> ensureAnimationListener(@NotNull AnimationListener<T> animationListener) {
        mAnimListeners.ensureListener(animationListener);
        return this;
    }


    @Override
    protected void onStarted(boolean resumed) {
        super.onStarted(resumed);
        mAnimListeners.dispatchOnMainThread(l -> l.onStarted(Animator.this, resumed));
    }

    @Override
    protected void onRepeat() {
        super.onRepeat();
        mAnimListeners.dispatchOnMainThread(l -> l.onRepeat(Animator.this));
    }

    @Override
    protected void onPaused() {
        super.onPaused();
        mAnimListeners.dispatchOnMainThread(l -> l.onPaused(Animator.this));
    }

    @Override
    protected void onEnd(@NotNull EndMode endMode) {
        super.onEnd(endMode);
        mAnimListeners.dispatchOnMainThread(l -> l.onEnd(Animator.this, endMode));
    }

    @Override
    protected void onReset(boolean wasRunning) {
        super.onReset(wasRunning);
        mAnimListeners.dispatchOnMainThread(l -> l.onReset(Animator.this, wasRunning));
    }

    @Override
    protected void onValueUpdate() {
        super.onValueUpdate();
        final Consumer<AnimationListener<T>> action = l -> l.onAnimationUpdate(Animator.this);

//        mAnimListeners.dispatchOnMainThread(l -> l.onAnimationUpdate(Animator.this));
        mAnimListeners.forEachListener(action);    // called on current thread, possibly background
    }

    @Override
    protected void onDurationChanged(long previousDurationMs, long newDurationMs) {
        super.onDurationChanged(previousDurationMs, newDurationMs);
        mAnimListeners.dispatchOnMainThread(l -> l.onDurationChanged(Animator.this));
    }

    @Override
    protected void onRepeatModeChanged() {
        super.onRepeatModeChanged();
        mAnimListeners.dispatchOnMainThread(l -> l.onRepeatModeChanged(Animator.this));
    }

    @Override
    protected void onRepeatCountChanged() {
        super.onRepeatCountChanged();
        mAnimListeners.dispatchOnMainThread(l -> l.onRepeatCountChanged(Animator.this));
    }

    @Override
    protected void onInterpolatorChanged() {
        super.onInterpolatorChanged();
        mAnimListeners.dispatchOnMainThread(l -> l.onInterpolatorChanged(Animator.this));
    }

    @Override
    protected void onDefaultInterpolatorChanged() {
        super.onDefaultInterpolatorChanged();
        mAnimListeners.dispatchOnMainThread(l -> l.onDefaultInterpolatorChanged(Animator.this));
    }
}
