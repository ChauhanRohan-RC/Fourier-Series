package ui;

import animation.animator.AbstractAnimator;
import animation.animator.Animator;
import animation.animator.IntAnimator;
import animation.interpolator.Interpolator;
import app.Colors;
import app.R;
import function.definition.ComplexDomainFunctionI;
import models.Size;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rotor.RotorState;
import rotor.RotorStateManager;
import rotor.frequency.RotorFrequencyProviderI;
import util.Format;
import util.live.Listeners;
import util.main.ComplexUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.ListIterator;

public class FTWinderPanel extends JPanel implements Runnable {

    private static final String TAG = "FourierTransformPanel";

//    final ComplexDomainFunctionI func = new MergedFunction(
//            new SineSignal(5, 25, 0),
//            new SineSignal(3, 20, ComplexUtil.HALF_PI, 1, 0.5),
//            new SineSignal(4, 20, 0, 0, 0.7)
//    );

    public static final int ROTOR_COUNT_MIN = 0;
    public static final int ROTOR_COUNT_MAX = 1000;
    public static final int ROTOR_COUNT_SLIDER_LABEL_INCREMENT = 200;

    @NotNull
    public static final AbstractAnimator.RepeatMode DEFAULT_REPEAT_MODE = AbstractAnimator.RepeatMode.END;
    @NotNull
    public static final Interpolator INTERPOLATOR = Interpolator.LINEAR;        // Required for seek operation

    private static final int DEFAULT_INTERVAL_COUNT = 2000;
    private static final boolean DEFAULT_JOIN_POINTS = true;


    /* Speed */
    public static final long ROTORS_ANIMATION_DURATION_MS_EACH_ROTOR_MIN = 1L;
    public static final long ROTORS_ANIMATION_DURATION_MS_EACH_ROTOR_MAX = 400L;
    public static final long ROTORS_ANIMATION_DURATION_MS_EACH_ROTOR_DEFAULT = 70L;

    public static final float ROTORS_ANIMATION_SPEED_FRACTION_DEFAULT = durationMsToSpeedFraction(ROTORS_ANIMATION_DURATION_MS_EACH_ROTOR_DEFAULT, 1);

    public static float durationMsToSpeedFraction(long durationMs, int rotorCount) {
        if (rotorCount < 1)
            return 0;

        final double durationPerRotor = ComplexUtil.constraint(ROTORS_ANIMATION_DURATION_MS_EACH_ROTOR_MIN, ROTORS_ANIMATION_DURATION_MS_EACH_ROTOR_MAX, (double) Math.abs(durationMs) / rotorCount);
        return (float) (1 - ComplexUtil.norm(ROTORS_ANIMATION_DURATION_MS_EACH_ROTOR_MIN, ROTORS_ANIMATION_DURATION_MS_EACH_ROTOR_MAX, durationPerRotor));
    }

    public static long speedFractionToDurationMs(float speedFraction, int rotorCount) {
        if (rotorCount < 1)
            return 0;

        final double durPerRotor = ComplexUtil.lerp(ROTORS_ANIMATION_DURATION_MS_EACH_ROTOR_MIN, ROTORS_ANIMATION_DURATION_MS_EACH_ROTOR_MAX, ComplexUtil.constraint(0, 1, 1 - speedFraction));
        return (long) (durPerRotor * rotorCount);
    }




    /*  init */

    @NotNull
    private static IntAnimator createRotorsAnimator(int rotorCount, float speedFraction) {
        final IntAnimator anim = new IntAnimator(0, 0);
        anim.setDefaultInterpolator(INTERPOLATOR);
        anim.setInterpolator(INTERPOLATOR);
        return configureRotorsAnimator(anim, rotorCount, speedFraction);
    }

    @NotNull
    private static IntAnimator configureRotorsAnimator(@NotNull IntAnimator anim, int rotorCount, float speedFraction) {
        anim.setActualEndValue(rotorCount < 1? 0: rotorCount - 1);
        anim.setDurationMs(speedFractionToDurationMs(speedFraction, rotorCount));
        return anim;
    }



    public interface Listener {

        void onRotorsCountChanged(@NotNull FTWinderPanel panel, int rotorsCount);

        void onIsLoadingChanged(@NotNull FTWinderPanel panel, boolean isLoading);

        void onIsPlayingChanged(@NotNull FTWinderPanel panel, boolean playing);

        void onRotorsAnimationSpeedChanged(@NotNull FTWinderPanel panel, int speedPercent);

        void onRotorsAnimationRepeatModeChanged(@NotNull FTWinderPanel panel, @NotNull AbstractAnimator.RepeatMode repeatMode);

        void onCurrentRotorChanged(@NotNull FTWinderPanel panel, int currentRotorIndex);

        void onRotorsFrequencyProviderChanged(@NotNull FTWinderPanel panel, @Nullable RotorFrequencyProviderI old, @Nullable RotorFrequencyProviderI _new);

        void onPointsJoiningEnabledChanged(@NotNull FTWinderPanel panel, boolean pointsJoiningEnabled);

    }






    private final RotorStateManager.Listener managerListener = new RotorStateManager.Listener() {
        @Override
        public void onRotorsLoadIntercepted(@NotNull RotorStateManager manager, int loadCount) {

        }

        @Override
        public boolean onInterceptRotorsLoad(@NotNull RotorStateManager manager, int loadCount) {
            return RotorStateManager.Listener.super.onInterceptRotorsLoad(manager, loadCount);
        }

        @Override
        public void onRotorsLoadingChanged(@NotNull RotorStateManager manager, boolean isLoading) {
//            if (isLoading && manager.getRotorCount() == 0) {
//                reset(false);
//            }

            update();
            listeners.dispatchOnMainThread(l -> l.onIsLoadingChanged(FTWinderPanel.this, isLoading));
        }

        @Override
        public void onRotorsLoadFinished(@NotNull RotorStateManager manager, int count, boolean cancelled) {
            if (!cancelled) {
                reset(false);
                setPlay(true);
            }
        }

        @Override
        public void onRotorsCountChanged(@NotNull RotorStateManager manager, int prevCount, int newCount) {
            setPlay(false);
            reset(false);
            syncRotorsAnimator();

            listeners.forEachListener(l -> l.onRotorsCountChanged(FTWinderPanel.this, newCount));
            setPlay(true);
            update();
        }

        @Override
        public void onRotorsFrequencyProviderIntercepted(@NotNull RotorStateManager manager, @Nullable RotorFrequencyProviderI rotorFrequencyProvider) {

        }

        @Override
        public boolean onInterceptRotorFrequencyProvider(@NotNull RotorStateManager manager, @Nullable RotorFrequencyProviderI old, @Nullable RotorFrequencyProviderI _new) {
            return RotorStateManager.Listener.super.onInterceptRotorFrequencyProvider(manager, old, _new);
        }

        @Override
        public void onRotorsFrequencyProviderChanged(@NotNull RotorStateManager manager, @Nullable RotorFrequencyProviderI old, @Nullable RotorFrequencyProviderI _new) {
            reset(true);
            listeners.dispatchOnMainThread(l -> l.onRotorsFrequencyProviderChanged(FTWinderPanel.this, old, _new));
        }
    };


    private final Animator.AnimationListener<Integer> rotorsAnimListener = new Animator.AnimationListenerAdapter<Integer>() {

        @Override
        public void onStarted(@NotNull Animator<Integer> animator, boolean resumed) {
            onIsPlayingChanged(true);
        }

        @Override
        public void onRepeat(@NotNull Animator<Integer> animator) {

        }

        @Override
        public void onPaused(@NotNull Animator<Integer> animator) {
            onIsPlayingChanged(false);
        }

        @Override
        public void onEnd(@NotNull Animator<Integer> animator, AbstractAnimator.@NotNull EndMode endMode) {
            reset(false);
            onIsPlayingChanged(false);
        }

        @Override
        public void onReset(@NotNull Animator<Integer> animator, boolean wasRunning) {
            if (wasRunning) {
                onIsPlayingChanged(false);
            }
        }

        @Override
        public void onAnimationUpdate(@NotNull Animator<Integer> animator) {
            FTWinderPanel.this.onRotorsAnimationUpdate(animator.getCurrentValue());
        }

        @Override
        public void onDurationChanged(@NotNull Animator<Integer> animator) {
            FTWinderPanel.this.onRotorsAnimationDurationChanged(animator.getDurationMs());
        }

        @Override
        public void onRepeatModeChanged(@NotNull Animator<Integer> animator) {
            FTWinderPanel.this.onRepeatModeChanged(animator.getRepeatMode());
        }
    };

    @NotNull
    private final RotorStateManager manager;
    @NotNull
    private final IntAnimator rotorsAnim;
    private float speedFraction = ROTORS_ANIMATION_SPEED_FRACTION_DEFAULT;
//    @NotNull
//    private final Set<RotorState> rotorStates = new TreeSet<>(RotorState.COMPARATOR_FREQ_ASC);

    private volatile boolean mPointsJoiningEnabled = DEFAULT_JOIN_POINTS;
//    private double mPrevBaseScale = -1;

    private final Listeners<Listener> listeners = new Listeners<>();

    public FTWinderPanel(@NotNull RotorStateManager manager) {
        this.manager = manager;
        this.rotorsAnim = createRotorsAnimator(manager.getRotorCount(), speedFraction);

        setBackground(Colors.BG_DARK);

        rotorsAnim.addAnimationListener(rotorsAnimListener);
        manager.ensureListener(managerListener);
        manager.considerInitialize();
//        if (manager.getRotorCount() > 0) {
//            rotorsAnim.start();
//        }
    }

    @NotNull
    public RotorStateManager getRotorStateManager() {
        return manager;
    }

    @NotNull
    public IntAnimator getRotorsAnimator() {
        return rotorsAnim;
    }

    public int getCurrentRotorIndex() {
        final int index = rotorsAnim.getCurrentValue();
        return index < 0 || index >= getRotorCount()? -1: index;
    }

    @Nullable
    public RotorState getCurrentRotorState() {
        final int index = getCurrentRotorIndex();
        if (index == -1)
            return null;
        return manager.getRotorState(index);
    }

    public boolean seekToRotorIndex(int rotorIndex) {
        final int count = manager.getRotorCount();
        if (count < 2 || rotorIndex < 0 || rotorIndex >= count)
            return false;

        seekToRotorFraction((float) rotorIndex / (count - 1));
        return true;
    }

    public void seekToRotorFraction(float rotorFraction) {
        final float timeFraction = rotorFraction;      // since i know interpolator is linear
        seekToTimeFraction(timeFraction);
    }

    public void seekToTimeFraction(float timeFraction) {
        rotorsAnim.setElapsedFraction(timeFraction, false);
        if (!rotorsAnim.isRunning()) {
            setPlay(true);
            rotorsAnim.update();
            setPlay(false);
        } else {
            rotorsAnim.update();
        }
    }



    public void addRotorsAnimationListener(@NotNull Animator.AnimationListener<Integer> listener) {
        rotorsAnim.addAnimationListener(listener);
    }

    public boolean removeRotorsAnimationListener(@NotNull Animator.AnimationListener<Integer> listener) {
        return rotorsAnim.removeAnimationListener(listener);
    }

    public void ensureRotorsAnimationListener(@NotNull Animator.AnimationListener<Integer> listener) {
        rotorsAnim.ensureAnimationListener(listener);
    }

    public void addListener(@NotNull Listener listener) {
        listeners.addListener(listener);
    }

    public boolean removeListener(@NotNull Listener listener) {
        return listeners.removeListener(listener);
    }

    public void ensureListener(@NotNull Listener listener) {
        listeners.ensureListener(listener);
    }


    private void update() {
        repaint();
    }

    private void resetAnimator(boolean update) {
        rotorsAnim.backToStart();

        if (update) {
            update();
        }
    }

    private void reset(boolean update) {
        resetAnimator(false);

        if (update) {
            update();
        }
    }

    public void reset() {
        reset(true);
    }



    protected void onIsPlayingChanged(boolean playing) {
        update();
        listeners.dispatchOnMainThread(l -> l.onIsPlayingChanged(FTWinderPanel.this, playing));
    }

    public final boolean isPlaying() {
        return rotorsAnim.isRunning();
    }

    public final boolean isPaused() {
        return rotorsAnim.isPaused();
    }

    public final void setPlay(boolean play) {
        if (isPlaying() == play) {
            return;
        }

        if (play) {
            if (manager.isNoOp() || manager.getRotorCount() == 0)
                return;

            if (rotorsAnim.isEnded()) {
                rotorsAnim.reset();
            }

            rotorsAnim.start();
        } else {
            rotorsAnim.pause();
        }
    }

    public final boolean togglePlay() {
        final boolean play = !isPlaying();
        setPlay(play);
        return play;
    }

    public final void stop() {
        setPlay(false);
        reset(false);
    }


    public final boolean isPointsJoiningEnabled() {
        return mPointsJoiningEnabled;
    }

    protected void onPointsJoiningEnabledChanged(boolean pointsJoiningEnabled) {
        update();
        listeners.dispatchOnMainThread(l -> l.onPointsJoiningEnabledChanged(FTWinderPanel.this, pointsJoiningEnabled));
    }

    public final void setJoinPointsEnabled(boolean pointsJoiningEnabled) {
        final boolean old = mPointsJoiningEnabled;
        if (old == pointsJoiningEnabled) {
            return;
        }

        mPointsJoiningEnabled = pointsJoiningEnabled;
        onPointsJoiningEnabledChanged(pointsJoiningEnabled);
    }

    public boolean togglePointsJoining() {
        final boolean now = !isPointsJoiningEnabled();
        setJoinPointsEnabled(now);
        return now;
    }




    private void syncRotorsAnimator() {
        configureRotorsAnimator(rotorsAnim, manager.getRotorCount(), speedFraction);
    }

    protected void onRotorsAnimationUpdate(int currentRotorIndex) {
        update();

        listeners.forEachListener(l -> l.onCurrentRotorChanged(FTWinderPanel.this, currentRotorIndex));
    }


    protected void onRotorsAnimationDurationChanged(long durationMs) {
        final int percent = (int) (durationMsToSpeedFraction(durationMs, getRotorCount()) * 100);
        listeners.dispatchOnMainThread(l -> l.onRotorsAnimationSpeedChanged(FTWinderPanel.this, percent));
    }

    public final void setRotorsAnimationSpeedFraction(float fraction) {
        fraction = ComplexUtil.constraint(0, 1, fraction);
        if (speedFraction == fraction)
            return;

        speedFraction = fraction;
        final long duration = speedFractionToDurationMs(fraction, getRotorCount());
        rotorsAnim.setDurationMs(duration);
    }

    public final void setRotorsAnimationSpeedPercent(float percent) {
        setRotorsAnimationSpeedFraction(percent / 100);
    }

    public final float getRotorsAnimationSpeedFraction() {
        return speedFraction;
    }

    public final int getRotorsAnimationSpeedPercent() {
        return (int) (getRotorsAnimationSpeedFraction() * 100);
    }

    public void setRotorCount(int rotorCount) {
        manager.setRotorCountAsync(ComplexUtil.constraint(ROTOR_COUNT_MIN, ROTOR_COUNT_MAX, rotorCount));      // async
    }

    public int getRotorCount() {
        return manager.getRotorCount();
    }

    public int getConstrainedRotorCount() {
        return ComplexUtil.constraint(ROTOR_COUNT_MIN, ROTOR_COUNT_MAX, getRotorCount());
    }

    protected void onRepeatModeChanged(@NotNull AbstractAnimator.RepeatMode repeatMode) {
        listeners.dispatchOnMainThread(l -> l.onRotorsAnimationRepeatModeChanged(FTWinderPanel.this, repeatMode));
    }

    public final void setRepeatMode(@Nullable AbstractAnimator.RepeatMode repeatMode) {
        if (repeatMode == null) {
            repeatMode = DEFAULT_REPEAT_MODE;
        }

        rotorsAnim.setRepeatMode(repeatMode);
    }

    @NotNull
    public final AbstractAnimator.RepeatMode getRepeatMode() {
        return rotorsAnim.getRepeatMode();
    }


//    @Nullable
//    public final FTUi getFtUI() {
//        final Window w = SwingUtilities.windowForComponent(FTWinderPanel.this);
//        return w instanceof FTUi? (FTUi) w: null;
//    }


    @Override
    protected void paintComponent(Graphics _g) {
        super.paintComponent(_g);
        Graphics2D g = (Graphics2D) _g;

        if (FourierSeriesPanel.FORCE_ANTIALIASING) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        final int count = manager.getRotorCount();
        final int width = getWidth();
        final int height = getHeight();

        /* ...........................  HUD .....................................*/

        // 1. Status
        final String statusText = R.getStatusText(manager.isLoading(), manager.getPendingRotorCount());
        if (Format.notEmpty(statusText)) {
            g.setColor(Colors.FG_DARK);
            g.setFont(g.getFont().deriveFont(18f));
            g.drawString(statusText, 8, height - 18);
        }


        final int rotorIndex = rotorsAnim.getCurrentValue();
        if (rotorIndex >= 0 && rotorIndex < count) {
            final RotorState state = manager.getRotorState(rotorIndex);
            final double freq =state.getFrequency();

            // TODO: frequency label
            g.setColor(Colors.FG_DARK);
            g.setFont(g.getFont().deriveFont(16f));
            g.drawString(String.format("Frequency: %.2f", freq), 20, 26);

            final ComplexDomainFunctionI baseFunction = manager.getFunction();
            final ComplexDomainFunctionI ftFunction = ComplexUtil.fourierTransformIntegrand(baseFunction, freq);

            // Creating Samples
            int intervals = ftFunction.getNumericalIntegrationIntervalCount();
            if (intervals < 1) {
                intervals = DEFAULT_INTERVAL_COUNT;
            }

            final double dStart = ftFunction.getDomainStart();
            final double dStep = ftFunction.getDomainRange() / intervals;

            double input = dStart;
            double minReal = Double.MAX_VALUE;
            double maxReal = Double.MIN_VALUE;
            double minImg = Double.MAX_VALUE;
            double maxImg = Double.MIN_VALUE;

            final LinkedList<Complex> samples = new LinkedList<>();

            for (int i=0; i <= intervals; i++) {
                final Complex value = ftFunction.compute(input);
                samples.add(value);

                double real = value.getReal();
                double img = value.getImaginary();

                if (real > maxReal) {
                    maxReal = real;
                } else if (real < minReal) {
                    minReal = real;
                }

                if (img > maxImg) {
                    maxImg = img;
                } else if (img < minImg) {
                    minImg = img;
                }
                input += dStep;
            }

            // Actual Drawing
            g.setColor(Colors.COLOR_WAVE);
            g.translate(width / 2, height / 2);
            g.scale(1, -1);

            double baseScale = Math.abs(Math.min(width, height) / Math.max(maxReal - minReal, maxImg - minImg)) * 0.45;
//            final double prevBaseScale = mPrevBaseScale;
//            if (prevBaseScale == -1) {
//                mPrevBaseScale = baseScale;
//            } else {
//                double del = (baseScale / prevBaseScale) - 1;
//                if (del < 0.2 && del > -0.1) {
//                    baseScale = prevBaseScale;      // ignore
//                }
//            }

            final ListIterator<Complex> itr = samples.listIterator();
            Point2D prev = null;
            final boolean joinPoints = mPointsJoiningEnabled;

            while (itr.hasNext()) {
                final int index = itr.nextIndex();
                final Complex val = itr.next();

                final Point2D point = parseWavePoint(index, val, baseScale);
                g.draw(new Line2D.Double(joinPoints && prev != null? prev: point, point));
                prev = point;
            }

//            rotorStates.add(state);
//            g.translate(getWidth() / 4, getHeight() / 4);
//            g.setColor(new Color(55, 248, 248));
//            drawTransform(g, true);
//
//            g.setColor(new Color(252, 55, 111));
//            g.translate(0, -getHeight() / 2);
//            drawTransform(g, false);
        }


        g.dispose();
    }

//    private void drawTransform(Graphics2D g, boolean mag) {
//        Point2D _p = null;
//        boolean join = true;
//
//        for (RotorState s: rotorStates) {
//            final Point2D real = new Point2D.Double(s.getFrequency() * 50, mag? s.getMagnitude(200): s.getCoefficientArgument() + 100);
//            g.draw(new Line2D.Double(join && _p != null? _p: real, real));
//            _p = real;
//        }
//    }

    @NotNull
    private Point2D parseWavePoint(int index, @NotNull Complex complex, double scale) {
        return new Point2D.Double(complex.getReal() * scale, complex.getImaginary() * scale);
    }

    @Override
    public void run() {
        rotorsAnim.update();
    }


}
