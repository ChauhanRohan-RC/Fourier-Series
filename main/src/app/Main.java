package app;

import animation.animator.AbstractAnimator;
import animation.animator.Animator;
import animation.animator.DoubleAnimator;
import animation.interpolator.Interpolator;
import org.jetbrains.annotations.NotNull;
import provider.Providers;
import ui.FourierUi;
import ui.Ui;
import util.Format;
import util.Log;
import util.async.Async;

import javax.swing.*;
import java.nio.file.Path;

public class Main {

    public static final String TAG = "Main";

//    private static void setupFourierUi(int initialFuncProviderIndex, boolean preload) {
////        if (preload) {
////            stateManager.loadSync(stateManager.getDefaultInitialRotorCount(), null);
////        }
//
//        Async.postIfNotOnMainThread(() -> {
//            final FourierUi ui = new FourierUi(null, initialFuncProviderIndex);
//        });
//    }



    // TODO: Main production launcher

    private static void launchMain(String[] args) {
        R.init();

        Async.postIfNotOnMainThread(() -> {
            final FourierUi ui = new FourierUi(null, -1);
            ui.setFunctionProvider(Providers.NoopProvider.getSingleton());          // start with None
        });
    }


    // TODO: Test launcher

    private static void launchTest(String[] args) {
//
//
//        final AbstractAnimator<Double> anim = new DoubleAnimator(0, 100)
//                .addAnimationListener(new Animator.AnimationListenerAdapter<>() {
//                    @Override
//                    public void onStarted(@NotNull Animator<Double> animator, boolean resumed) {
//                        Log.d(TAG, "onStarted: resumed = " + resumed);
//                    }
//
//                    @Override
//                    public void onRepeat(@NotNull Animator<Double> animator) {
//                        Log.d(TAG, "onRepeat: " + animator.getCurrentRepetitionCount());
//                    }
//
//                    @Override
//                    public void onPaused(@NotNull Animator<Double> animator) {
//                        Log.d(TAG, "onPause:");
//                    }
//
//                    @Override
//                    public void onEnd(@NotNull Animator<Double> animator, AbstractAnimator.@NotNull EndMode endMode) {
//                        Log.d(TAG, "onEnd: mode = " + endMode);
//                    }
//
//                    @Override
//                    public void onReset(@NotNull Animator<Double> animator) {
//                        Log.d(TAG, "onReset:");
//                    }
//
//                    @Override
//                    public void onAnimationUpdate(@NotNull Animator<Double> animator) {
//
//                    }
//                }).addAnimationListener(new Animator.AnimationListenerAdapter<Double>() {
//                    @Override
//                    public void onAnimationUpdate(@NotNull Animator<Double> animator) {
//                        Log.d(TAG, "onAnimationUpdate: " + animator.getCurrentValue());
//                    }
//                }).setDurationMs(10000)
//                .setInterpolator(Interpolator.ACCELERATE_DECELERATE)
//                .setRepeatMode(AbstractAnimator.RepeatMode.CYCLE)
//                .setRepeatCount(-1);
//
////        Async.uiPost(anim::forceFinish, 1700);
//
//        Ui.createLooper(anim, 100).start();
//
//        anim.start();
    }


    public static void main(String[] args) {
//        launchTest(args);
        launchMain(args);
    }

}
