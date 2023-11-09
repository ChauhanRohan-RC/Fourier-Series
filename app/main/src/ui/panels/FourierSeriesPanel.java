package ui.panels;

import animation.animator.AbstractAnimator;
import animation.animator.Animator;
import animation.animator.DoubleAnimator;
import app.Colors;
import app.R;
import function.definition.DomainProviderI;
import misc.Format;
import misc.MathUtil;
import models.ComplexBuilder;
import models.Size;
import models.Triangle;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rotor.RotorState;
import rotor.RotorStateManager;
import rotor.frequency.RotorFrequencyProviderI;
import live.Listeners;
import async.Consumer;
import ui.util.Ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Objects;

/**
 * {@link #setPlay(boolean)} to start
 * */
public class FourierSeriesPanel extends JPanel implements Runnable {

    public static final String TAG = "FourierPanel";

    public static final float ROTORS_FRAME_WIDTH_RATIO_WAVE = 0.38f;        // as a ratio of width
    public static final float ROTORS_FRAME_WIDTH_RATIO_GRAPH = 0.35f;       // as a ratio of width (on;y when not graphing in center)

    public static final int ROTOR_COUNT_MIN = 0;
    public static final int ROTOR_COUNT_MAX = 1000;
    public static final int ROTOR_COUNT_SLIDER_LABEL_INCREMENT = 200;

    public static final float ROTORS_ZOOM_SCALE = 0.48f;
    public static final float ROTORS_ZOOM_SCALE_CENTER = 0.75f;
    public static final float ROTORS_ZOOM_SCALE_CENTER_AUTO_TRACK = 10f;

    public static float getRotorZoomScale(boolean graphingInCenter, boolean autoTrack) {
        return graphingInCenter? autoTrack? ROTORS_ZOOM_SCALE_CENTER_AUTO_TRACK: ROTORS_ZOOM_SCALE_CENTER: ROTORS_ZOOM_SCALE;
    }

    public static final int MAX_WAVE_POINTS = 20_00_000;
    public static final boolean DEFAULT_DRAW_AS_WAVE = false;
    public static final boolean DEFAULT_INVERT_X = false;
    public static final boolean DEFAULT_INVERT_Y = false;
    public static final boolean DEFAULT_GRAPH_IN_CENTER = true;
    public static final boolean DEFAULT_POINTS_JOINING_ENABLED = true;
    public static final boolean DEFAULT_AUTO_TRACK_IN_CENTER = false;

    public static final boolean AUTO_DISABLE_AUTO_TRACK_ON_END = true;

    @NotNull
    public static final AbstractAnimator.RepeatMode DEFAULT_REPEAT_MODE = AbstractAnimator.RepeatMode.REPEAT;

    public static final boolean DRAW_CIRCULAR_TIP_JOINTS = true;

    private static final double DEFAULT_TIP_SIZE_TO_RADIUS_RATIO = 0.1;
    private static final double DEFAULT_TIP_SIZE_TO_RADIUS_RATIO_CENTER = 0.11;

    private static final double DEFAULT_MINIMUM_SCALE = 0.1;
    private static final double SCALE_WHEEL_ROTATION_MULTIPLIER = 0.2;
    private static final double DEFAULT_SCALE_UNIT_INCREMENT = 0.5;
    private static final double DEFAULT_SCALE_UNIT_DECREMENT_BELOW_1 = 0.1;
    private static final double MAXIMUM_SCALE_BY_ROTOR_COUNT_MULTIPLIER = 1.5;

    private static final double DRAG_X_UNITS = 50;
    private static final double DRAG_Y_UNITS = 50;


    /* .....................................  Stroke  ................................. */

//    public static final Stroke STROKE_WAVE = new BasicStroke(0.95f);
//    public static final Stroke STROKE_WAVE_CENTER = new BasicStroke(1f);
//    public static final Stroke STROKE_WAVE_CENTER_AUTO_TRACK = new BasicStroke(3.6f);
//
//    public static final Stroke STROKE_ROTOR_CIRCLE = new BasicStroke(0.58f);
//    public static final Stroke STROKE_ROTOR_RADIUS = new BasicStroke(0.6f);
//    public static final Stroke STROKE_ROTOR_TO_WAVE_JOINT = new BasicStroke(0.45f);


    public static final Stroke STROKE_WAVE_IN_WAVE = new BasicStroke(0.75f);
    public static final Stroke STROKE_WAVE_CENTER = new BasicStroke(0.75f);
    public static final Stroke STROKE_WAVE_CENTER_AUTO_TRACK = new BasicStroke(1f);

    public static final Stroke STROKE_ROTOR_CIRCLE_WAVE = new BasicStroke(0.58f);
    public static final Stroke STROKE_ROTOR_CIRCLE_CENTER = new BasicStroke(0.37f);
    public static final Stroke STROKE_ROTOR_CIRCLE_CENTER_AUTO_TRACK = new BasicStroke(0.3f);

    public static final Stroke STROKE_ROTOR_RADIUS_WAVE = new BasicStroke(0.58f);
    public static final Stroke STROKE_ROTOR_RADIUS_CENTER = new BasicStroke(0.4f);
    public static final Stroke STROKE_ROTOR_RADIUS_CENTER_AUTO_TRACK = new BasicStroke(0.58f);

    public static final Stroke STROKE_ROTOR_TO_WAVE_JOINT_WAVE = new BasicStroke(0.45f);
    public static final Stroke STROKE_ROTOR_TO_WAVE_JOINT_CENTER = new BasicStroke(0.45f);
    public static final Stroke STROKE_ROTOR_TO_WAVE_JOINT_CENTER_AUTO_TRACK = new BasicStroke(0.45f);

    public record Strokes(@NotNull Stroke wave,
                          @NotNull Stroke rotorCircle,
                          @NotNull Stroke rotorRadius,
                          @NotNull Stroke rotorToWaveJoint) {

        @NotNull
        public static Strokes get(boolean graphingInCenter, boolean autoTrack) {
            if (graphingInCenter) {
                if (autoTrack) {
                    return new Strokes(STROKE_WAVE_CENTER_AUTO_TRACK,
                            STROKE_ROTOR_CIRCLE_CENTER_AUTO_TRACK,
                            STROKE_ROTOR_RADIUS_CENTER_AUTO_TRACK,
                            STROKE_ROTOR_TO_WAVE_JOINT_CENTER_AUTO_TRACK);
                }

                return new Strokes(STROKE_WAVE_CENTER,
                        STROKE_ROTOR_CIRCLE_CENTER,
                        STROKE_ROTOR_RADIUS_CENTER,
                        STROKE_ROTOR_TO_WAVE_JOINT_CENTER);
            }

            return new Strokes(STROKE_WAVE_IN_WAVE,
                    STROKE_ROTOR_CIRCLE_WAVE,
                    STROKE_ROTOR_RADIUS_WAVE,
                    STROKE_ROTOR_TO_WAVE_JOINT_WAVE);
        }
    }

//    @NotNull
//    public static Stroke getWaveStroke(boolean graphingInCenter, boolean autoTrack) {
//        return graphingInCenter? autoTrack? STROKE_WAVE_CENTER_AUTO_TRACK: STROKE_WAVE_CENTER: STROKE_WAVE;
//    }
//
//    @NotNull
//    public static Stroke getRotorCircleStroke(boolean graphingInCenter, boolean autoTrack) {
//        return graphingInCenter? autoTrack? STROKE_ROTOR_CIRCLE_CENTER_AUTO_TRACK: STROKE_ROTOR_CIRCLE_CENTER: STROKE_ROTOR_CIRCLE_WAVE;
//    }
//
//    @NotNull
//    public static Stroke getRotorRadiusStroke(boolean graphingInCenter, boolean autoTrack) {
//        return graphingInCenter? autoTrack? STROKE_ROTOR_RADIUS_CENTER_AUTO_TRACK: STROKE_ROTOR_RADIUS_CENTER: STROKE_ROTOR_RADIUS_WAVE;
//    }
//
//    @NotNull
//    public static Stroke getRotorToWaveJointStroke(boolean graphingInCenter, boolean autoTrack) {
//        return graphingInCenter? autoTrack? STROKE_ROTOR_TO_WAVE_JOINT_CENTER_AUTO_TRACK: STROKE_ROTOR_TO_WAVE_JOINT_CENTER: STROKE_ROTOR_TO_WAVE_JOINT_WAVE;
//    }





    public interface PanelListener {

        void onIsPlayingChanged(boolean playing);

        void onDrawingAsWaveChanged(boolean drawingAsWave);

        void onYInvertedChanged(boolean yInverted);

        void onXInvertedChanged(boolean xInverted);

        void onGraphInCenterChanged(boolean graphInCenter);             // no

        void onDomainAnimationSpeedChanged(int percent);                   // no, rotor count anim speed

        void onRotorStateManagerChanged(@Nullable RotorStateManager old, @NotNull RotorStateManager _new);

        default void onRepeatModeChanged(@NotNull AbstractAnimator.RepeatMode repeatMode) { }

        default void onPointsJoiningEnabledChanged(boolean pointsJoiningEnabled) { }

        default void onScaleChanged(double scale) { }

        default void onScalePivotChanged(@Nullable Point2D scalePivot) { }

        default void onDragChanged(@Nullable Size drag) { }

        default void onAutoTrackInCenterChanged(boolean autoTrackInCenter) { }          // no
    }



    private static class WavePoint {

        public final double input;
        @NotNull
        public final Complex value;

        @Nullable
        private Color color;

        private WavePoint(double input, @NotNull Complex value, @Nullable Color color) {
            this.input = input;
            this.value = value;
            this.color = color;
        }

        private WavePoint(double input, @NotNull Complex value) {
            this(input, value, null);
        }

        @Nullable
        public Color getColor() {
            return color;
        }

        public Color getColorOrDefault(Color defValue) {
            return color != null? color : defValue;
        }

        public void setColor(@Nullable Color color) {
            this.color = color;
        }
    }


    @NotNull
    private static DoubleAnimator createDomainAnimator(@NotNull DomainProviderI domainProvider) {
        final DoubleAnimator anim = new DoubleAnimator(domainProvider.getDomainStart(), domainProvider.getDomainEnd());
        anim.setDurationMs(domainProvider.getDomainAnimationDurationMsDefault());
        anim.setRepeatMode(DEFAULT_REPEAT_MODE);
        return anim;
    }



    private final RotorStateManager.Listener mRotorStateListener = new RotorStateManager.Listener() {

        @Override
        public boolean onInterceptRotorsLoad(@NotNull RotorStateManager manager, int loadCount) {
            return RotorStateManager.Listener.super.onInterceptRotorsLoad(manager, loadCount);
        }

        @Override
        public void onRotorsLoadIntercepted(@NotNull RotorStateManager manager, int loadCount) {
        }

        @Override
        public void onRotorsLoadingChanged(@NotNull RotorStateManager manager, boolean isLoading) {
//            if (isLoading) {
//                setPlay(false);
//            }

            update();
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
            reset(false);
            setPlay(true);
        }


        @Override
        public boolean onInterceptRotorFrequencyProvider(@NotNull RotorStateManager manager, @Nullable RotorFrequencyProviderI old, @Nullable RotorFrequencyProviderI _new) {
            return RotorStateManager.Listener.super.onInterceptRotorFrequencyProvider(manager, old, _new);
        }

        @Override
        public void onRotorsFrequencyProviderIntercepted(@NotNull RotorStateManager manager, @Nullable RotorFrequencyProviderI rotorFrequencyProvider) {

        }

        @Override
        public void onRotorsFrequencyProviderChanged(@NotNull RotorStateManager manager, @Nullable RotorFrequencyProviderI old, @Nullable RotorFrequencyProviderI _new) {
            reset(false);
        }
    };


    private final Animator.AnimationListenerAdapter<Double> mDomainAnimListener = new Animator.AnimationListenerAdapter<>() {
        @Override
        public void onStarted(@NotNull Animator<Double> animator, boolean resumed) {
            onIsPlayingChanged(true);
        }

        @Override
        public void onRepeat(@NotNull Animator<Double> animator) {

        }

        @Override
        public void onPaused(@NotNull Animator<Double> animator) {
            onIsPlayingChanged(false);
        }

        @Override
        public void onEnd(@NotNull Animator<Double> animator, AbstractAnimator.@NotNull EndMode endMode) {
            if (AUTO_DISABLE_AUTO_TRACK_ON_END && isAutoTrackingInCenter()) {
                setAutoTrackInCenter(false);
            }

            onIsPlayingChanged(false);
        }

        @Override
        public void onReset(@NotNull Animator<Double> animator, boolean wasRunning) {
            Animator.AnimationListenerAdapter.super.onReset(animator, wasRunning);
            if (wasRunning) {
                onIsPlayingChanged(false);
            }
        }

        @Override
        public void onAnimationUpdate(@NotNull Animator<Double> animator) {
        }

        @Override
        public void onDurationChanged(@NotNull Animator<Double> animator) {
            FourierSeriesPanel.this.onDomainAnimationDurationChanged(animator.getDurationMs());
        }

        @Override
        public void onRepeatModeChanged(@NotNull Animator<Double> animator) {
            FourierSeriesPanel.this.onRepeatModeChanged(animator.getRepeatMode());
        }
    };


//    @NotNull
//    private final Timer mLooper;

    @NotNull
    private RotorStateManager mRotorStateManager;
    @NotNull
    private final LinkedList<WavePoint> wave = new LinkedList<>();
    private boolean mDrawAsWave = DEFAULT_DRAW_AS_WAVE;
    private boolean mInvertX = DEFAULT_INVERT_X;
    private boolean mInvertY = DEFAULT_INVERT_Y;
    private boolean mGraphInCenter = DEFAULT_GRAPH_IN_CENTER;
    private boolean mPointsJoiningEnabled = DEFAULT_POINTS_JOINING_ENABLED;
    private boolean mAutoTrackInCenter = DEFAULT_AUTO_TRACK_IN_CENTER;
    @NotNull
    private final DoubleAnimator mDomainAnimator;

    private double mScale = 1;
    @Nullable
    private Size mDrag;
    @NotNull
    private final MouseHandler mMouseHandler = new MouseHandler();
    @NotNull
    private final ComponentListener mComponentListener = new ComponentListener();
    @NotNull
    private final Listeners<PanelListener> mPanelListeners = new Listeners<>();

    public FourierSeriesPanel(@NotNull RotorStateManager stateManager) {
        mRotorStateManager = stateManager;
        mDomainAnimator = createDomainAnimator(stateManager.getFunction());
        mDomainAnimator.addAnimationListener(mDomainAnimListener);

        setBackground(Colors.BG_DARK);

        addMouseWheelListener(mMouseHandler);
        addMouseMotionListener(mMouseHandler);
        addMouseListener(mMouseHandler);
        addComponentListener(mComponentListener);

        syncRotorStateManagerInternal(false);
    }

    public void addPanelListener(@NotNull FourierSeriesPanel.PanelListener l) {
        mPanelListeners.addListener(l);
    }

    public boolean removePanelListener(@NotNull FourierSeriesPanel.PanelListener l) {
        return mPanelListeners.removeListener(l);
    }

    public boolean containsPanelListener(@NotNull FourierSeriesPanel.PanelListener l) {
        return mPanelListeners.containsListener(l);
    }

    public void ensurePanelListener(@NotNull FourierSeriesPanel.PanelListener l) {
        mPanelListeners.ensureListener(l);
    }

    private void forEachPanelListener(@NotNull Consumer<PanelListener> action) {
        mPanelListeners.forEachListener(action);
    }

    protected void onDomainAnimationDurationChanged(long durationMs) {
        final int percent = (int) (mRotorStateManager.getFunction().durationMsToDomainAnimationSpeedFraction(durationMs) * 100);
        forEachPanelListener(l -> l.onDomainAnimationSpeedChanged(percent));
    }

    public final void setDomainAnimationSpeedFraction(float fraction) {
        final long duration = mRotorStateManager.getFunction().domainAnimationSpeedFractionToDurationMs(fraction);
        mDomainAnimator.setDurationMs(duration);
    }

    public final void setDomainAnimationSpeedPercent(float percent) {
        setDomainAnimationSpeedFraction(percent / 100);
    }

    public final float getDomainAnimationSpeedFraction() {
        return mRotorStateManager.getFunction().durationMsToDomainAnimationSpeedFraction(mDomainAnimator.getDurationMs());
    }

    public final int getDomainAnimationSpeedPercent() {
        return (int) (getDomainAnimationSpeedFraction() * 100);
    }

    public void setRotorCount(int rotorCount) {
        mRotorStateManager.setRotorCountAsync(MathUtil.constraint(ROTOR_COUNT_MIN, ROTOR_COUNT_MAX, rotorCount));      // async
    }

    public int getRotorCount() {
        return mRotorStateManager.getRotorCount();
    }

    public int getConstrainedRotorCount() {
        return MathUtil.constraint(ROTOR_COUNT_MIN, ROTOR_COUNT_MAX, getRotorCount());
    }


    private void syncRotorStateManagerInternal(boolean update) {
        mRotorStateManager.ensureListener(mRotorStateListener);
        resetInput(false);
        invalidateWave();
        mDomainAnimator.setDurationMs(mRotorStateManager.getFunction().getDomainAnimationDurationMsDefault());

        final AbstractAnimator.RepeatMode pref = mRotorStateManager.getDefaultRepeatMode();
        if (pref != null) {
            mDomainAnimator.setRepeatMode(pref);
        }

        mRotorStateManager.considerInitialize();

        if (update) {
            update();
        }
    }


    protected void onRotorStateManagerChanged(@Nullable RotorStateManager old, @NotNull RotorStateManager _new) {
        // Detach old loader
        if (old != null) {
            old.cancelLoad(true);
            old.removeListener(mRotorStateListener);
        }

        mDomainAnimator.ensureAnimationListener(mDomainAnimListener)
                .setActualStartValue(_new.getFunction().getDomainStart())
                .setActualEndValue(_new.getFunction().getDomainEnd());

        forEachPanelListener(l -> l.onRotorStateManagerChanged(old, _new));
        syncRotorStateManagerInternal(true);
    }

    public final void setRotorStateManager(@NotNull RotorStateManager rotorStateManager) {
        if (mRotorStateManager == rotorStateManager) {
           return;
        }

        final RotorStateManager old = mRotorStateManager;
        mRotorStateManager = rotorStateManager;
        onRotorStateManagerChanged(old, rotorStateManager);
    }

    @NotNull
    public final RotorStateManager getRotorStateManager() {
        return mRotorStateManager;
    }



    protected void onDrawAsWaveChanged(boolean drawingASWave) {
        update();
        forEachPanelListener(l -> l.onDrawingAsWaveChanged(drawingASWave));
    }

    public boolean isDrawingWave() {
        return mDrawAsWave;
    }

    public void setDrawAsWave(boolean drawAsWave) {
        if (mDrawAsWave != drawAsWave) {
            mDrawAsWave = drawAsWave;
            onDrawAsWaveChanged(drawAsWave);
        }
    }

    public void toggleDrawAsWave() {
        setDrawAsWave(!isDrawingWave());
    }


    private void onInvertYChanged(boolean yInverted) {
        update();
        forEachPanelListener(l -> l.onYInvertedChanged(yInverted));
    }

    public void setInvertY(boolean invertY) {
        if (mInvertY != invertY) {
            mInvertY = invertY;
            onInvertYChanged(invertY);
        }
    }

    public boolean isYInverted() {
        return mInvertY;
    }


    private void onInvertXChanged(boolean xInverted) {
        update();
        forEachPanelListener(l -> l.onXInvertedChanged(xInverted));
    }

    public void setInvertX(boolean invertX) {
        if (mInvertX != invertX) {
            mInvertX = invertX;
            onInvertXChanged(invertX);
        }
    }

    public boolean isXInverted() {
        return mInvertX;
    }

    public boolean toggleInvertX() {
        final boolean state = !isXInverted();
        setInvertX(state);
        return state;
    }

    public boolean toggleInvertY() {
        final boolean state = !isYInverted();
        setInvertY(state);
        return state;
    }

    private void onGraphInCenterChanged(boolean graphInCenter) {
        update();
        forEachPanelListener(l -> l.onGraphInCenterChanged(graphInCenter));
    }

    public boolean isGraphCenterEnabled() {
        return mGraphInCenter;
    }

    public void setGraphInCenter(boolean graphInCenter) {
        if (mGraphInCenter != graphInCenter) {
            mGraphInCenter = graphInCenter;
            onGraphInCenterChanged(graphInCenter);
        }
    }

    public void toggleGraphInCenter() {
        setGraphInCenter(!isGraphCenterEnabled());
    }

    public final boolean isGraphingInCenter() {
        return mGraphInCenter && !mDrawAsWave;
    }

    public final boolean isAutoTrackingInCenter() {
        return isGraphingInCenter() && mAutoTrackInCenter;
    }


    /* Transform */

    private double transformY(double y) {
        return mInvertY? -y: y;
    }

    private double transformX(double x) {
        return mInvertX? -x: x;
    }

    @NotNull
    private Complex transform(@NotNull Complex in) {
        return new Complex(transformX(in.getReal()), transformY(in.getImaginary()));
    }



    /* Scale */

    protected boolean shouldScaleByMouseWheel(@NotNull MouseWheelEvent e) {
        return true;
    }

    protected double getScaleIncrement(@NotNull MouseWheelEvent e) {
        return -e.getPreciseWheelRotation() * SCALE_WHEEL_ROTATION_MULTIPLIER;
    }

    protected double getScaleUnitIncrement(double scale) {
        final int int_scale = (int) scale;
        if (int_scale == scale)
            return DEFAULT_SCALE_UNIT_INCREMENT;

        return Math.min(int_scale + 1 - scale, DEFAULT_SCALE_UNIT_INCREMENT);
    }

    protected double getScaleUnitDecrement(double scale) {
        final int int_scale = (int) scale;
        final double def = scale > 1? DEFAULT_SCALE_UNIT_INCREMENT: DEFAULT_SCALE_UNIT_DECREMENT_BELOW_1;
        if (int_scale == scale)
            return def;
        return Math.min(scale - int_scale, def);
    }

    public double getMinimumScale() {
        return DEFAULT_MINIMUM_SCALE;
    }

    public double getMaximumScale() {
        return getRotorCount() * MAXIMUM_SCALE_BY_ROTOR_COUNT_MULTIPLIER;
    }


    protected void onScaleChanged(double scale, boolean update) {
        if (update) {
            update();
        }

        forEachPanelListener(l -> l.onScaleChanged(scale));
    }

    private boolean setScale(double scale, boolean update) {
        scale = MathUtil.constraint(getMinimumScale(), getMaximumScale(), scale);
        if (mScale == scale)
            return false;

        mScale = scale;
        onScaleChanged(scale, update);
        return true;
    }

    public final boolean setScale(double scale) {
        return setScale(scale, true);
    }

    private boolean increaseScale(double scaleDelta, boolean update) {
        return setScale(mScale + scaleDelta, update);
    }

    public final boolean increaseScale(double scaleDelta) {
        return increaseScale(scaleDelta, true);
    }

    public final boolean incrementScaleByUnit() {
        return increaseScale(getScaleUnitIncrement(mScale));
    }

    public final boolean decrementScaleByUnit() {
        return increaseScale(-getScaleUnitDecrement(mScale));
    }


    public final double getScale() {
        return mScale;
    }


    /* Drag */

    protected boolean shouldDragOnMousePress(@NotNull MouseEvent e) {
        return e.getButton() == MouseEvent.BUTTON1;
    }

    public boolean isMaxDragDimensionDependent() {
        return true;
    }

    @Nullable
    public Size getMaxDrag() {
        final double s = Math.max(0.5, mScale);
        return new Size(getWidth() * s, getHeight() * s);
    }

    protected void onDragChanged(@Nullable Size drag, boolean update) {
        if (update) {
            update();
        }

        final Size copy = drag != null? drag.copy(): null;
        forEachPanelListener(l -> l.onDragChanged(copy));
    }

    private boolean setDrag(@Nullable Size drag, boolean update) {
        Size max;
        if (drag != null && (max = getMaxDrag()) != null) {
            drag = new Size(Math.signum(drag.width) * Math.min(Math.abs(max.width), Math.abs(drag.width)), Math.signum(drag.height) * Math.min(Math.abs(max.height), Math.abs(drag.height)));
        }

        if (Objects.equals(mDrag, drag))
            return false;

        mDrag = drag;
        onDragChanged(drag, update);
        return true;
    }

    public final boolean setDrag(@Nullable Size drag) {
        return setDrag(drag, true);
    }

    private boolean dragBy(@NotNull Size dragDelta, boolean update) {
        return setDrag(mDrag != null? mDrag.add(dragDelta): dragDelta, update);
    }

    public final boolean dragBy(@NotNull Size dragDelta) {
        return dragBy(dragDelta, true);
    }

    public final boolean dragXBy(double dragDelta) {
        return dragBy(new Size(dragDelta, 0), true);
    }

    public final boolean dragYBy(double dragDelta) {
        return dragBy(new Size(0, dragDelta), true);
    }

    @Nullable
    public final Size getDrag() {
        return mDrag;
    }

    public final boolean hasScale() {
        return mScale != 1;
    }


    public final boolean hasDrag() {
        return mDrag != null && (mDrag.width != 0 || mDrag.height != 0);
    }

    public final boolean hasScaleOrDrag() {
        return hasScale() || hasDrag();
    }



    public double getXDragUnitIncrement() {
        return getWidth() / DRAG_X_UNITS;
    }

    public double getYDragUnitIncrement() {
        return getHeight() / DRAG_Y_UNITS;
    }

    public final boolean dragXByUnit(boolean right) {
        return dragXBy((right? 1: -1) * getXDragUnitIncrement());
    }

    public final boolean dragYByUnit(boolean down) {
        return dragYBy((down? 1: -1) * getYDragUnitIncrement());
    }




    @NotNull
    protected RotorState retrieveRotorStateImpl(int index) {
        return mRotorStateManager.getRotorState(index);
    }

    @NotNull
    private RotorState getRotorState(int index) {
        return retrieveRotorStateImpl(index);
    }

    protected final void invalidateWave() {
        wave.clear();
    }


    public void resetInput(boolean update) {
        mDomainAnimator.backToStart();

        if (update) {
            update();
        }
    }

    public boolean resetScale(boolean update) {
        final boolean scaleChanged = setScale(1, false);

        if (update && scaleChanged) {
            update();
        }

        return scaleChanged;
    }

    public boolean resetDrag(boolean update) {
        return setDrag(null, update);
    }

    private boolean resetScaleAndDrag(boolean update) {
        final boolean scaleChanged = resetScale(false);
        final boolean dragChanged = resetDrag(false);

        final boolean changed = dragChanged || scaleChanged;
        if (update && changed) {
            update();
        }

        return changed;
    }

    public final void resetScaleAndDrag() {
        resetScaleAndDrag(true);
    }

    public final void reset(boolean resetScaleAndDrag) {
        resetInput(false);
        invalidateWave();
        if (resetScaleAndDrag) {
            resetScaleAndDrag(false);
        }

        update();
    }


    @NotNull
    protected WavePoint createWavePoint(@NotNull Complex sum, double input, double baseRadius) {
        return new WavePoint(input, sum.divide(baseRadius), mRotorStateManager.getFunction().getColor(input));
    }

    @NotNull
    protected Point2D parseWavePoint(int index, @NotNull WavePoint wp, double baseRadius) {
        if (mDrawAsWave) {
            return new Point2D.Double(index, transformY(wp.value.getReal()) * baseRadius);
        }

        return new Point2D.Double(transformX(wp.value.getReal() * baseRadius), transformY(wp.value.getImaginary() * baseRadius));
    }


    // as a factor of width
    protected float getRotorsFrameWidthRatio() {
        return mDrawAsWave? ROTORS_FRAME_WIDTH_RATIO_WAVE: isGraphCenterEnabled()? 1: ROTORS_FRAME_WIDTH_RATIO_GRAPH;
    }

    protected final double getRotorsFrameWidth() {
        return getWidth() * getRotorsFrameWidthRatio();
    }

    protected double getWaveOffsetX(double width, double rotorFrameW) {
        return mDrawAsWave? rotorFrameW / 2: isGraphCenterEnabled()? 0: width / 2;
    }


    // as a factor of size
    protected double getBaseRotorRadiusRatio() {
        return 1 / mRotorStateManager.getAllRotorsMagnitudeScaleSum();
    }

    protected final double getBaseRotorRadiusPix(boolean graphingInCenter, boolean autoTrack) {
        return Math.min(getRotorsFrameWidth(), getHeight()) * getBaseRotorRadiusRatio() * getRotorZoomScale(graphingInCenter, autoTrack);
    }


    // Tip size as ratio of radius
    protected double getBaseRotorTipToRadiusRatio(boolean graphingInCenter) {
        return graphingInCenter? DEFAULT_TIP_SIZE_TO_RADIUS_RATIO_CENTER: DEFAULT_TIP_SIZE_TO_RADIUS_RATIO;
    }

    protected final double getBaseRotorTipSize(boolean graphingInCenter, double baseRadius) {
        return baseRadius * getBaseRotorTipToRadiusRatio(graphingInCenter);     // can cap to max pixels
    }

    protected boolean shouldDrawRotors() {
        return (isPlaying() || isDrawingWave()) && mRotorStateManager.getRotorCount() > 0;
    }

    @Override
    protected void paintComponent(Graphics _g) {
        super.paintComponent(_g);
        Graphics2D g = (Graphics2D) _g;

        if (Ui.FORCE_ANTIALIASING) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        final boolean shouldDrawRotors = shouldDrawRotors();
        final double input = mDomainAnimator.getCurrentValue();
        final boolean graphingInCenter = isGraphingInCenter();
        final boolean autoTrack = mAutoTrackInCenter;

        final int count = getRotorCount();
        final int width = getWidth();
        final int height = getHeight();
        final double rotorsFrameW = getRotorsFrameWidth();
        final double baseRadius = getBaseRotorRadiusPix(graphingInCenter, autoTrack);
        final double baseTipSize = getBaseRotorTipSize(graphingInCenter, baseRadius);
        final double waveOffsetX = getWaveOffsetX(width, rotorsFrameW);

        final Strokes strokes = Strokes.get(graphingInCenter, autoTrack);


        /* ...........................  HUD .....................................*/

        // 1. Status
        final String statusText = R.getStatusText(mRotorStateManager.isLoading(), mRotorStateManager.getPendingRotorCount());
        if (Format.notEmpty(statusText)) {
            g.setColor(Colors.FG_DARK);
            g.setFont(g.getFont().deriveFont(18f));
            g.drawString(statusText, 8, height - 18);
        }

        /* ..........................  Pre-Transforms ...........................*/
        final AffineTransform t = g.getTransform();

        // 1. Translate
        double tx = rotorsFrameW / 2f, ty = height / 2f;
        final Size drag = getDrag();
        if (drag != null) {
            tx += drag.width; ty += drag.height;
        }

        final WavePoint last;
        if (autoTrack && graphingInCenter && (last = wave.peekFirst()) != null) {
            final Point2D p = parseWavePoint(0, last, baseRadius);
            tx -= p.getX(); ty -= p.getY();
        }

        t.translate(tx, ty);

        // 2. Scale
        final double scale = mScale;
        t.scale(scale, scale);

        g.setTransform(t);


        /* ...........................  Rotors  ..............................*/
        Shape tipToWaveJoint = null;

        if (shouldDrawRotors) {
            final ComplexBuilder sum = new ComplexBuilder();
            double prevX = 0, prevY = 0;

            for (int i=0; i < count; i++) {
                final RotorState state = getRotorState(i);

                // Circle
                final double centerX = transformX(prevX);
                final double centerY = transformY(prevY);
                final double radius = state.getMagnitude(baseRadius);

                g.setStroke(strokes.rotorCircle);
                g.setColor(Colors.getCircleColor(graphingInCenter));
                g.draw(new Ellipse2D.Double(centerX - radius, centerY - radius,radius * 2, radius * 2));

                // Tip of this circle
                final Complex tipPoint = state.getTip(input);
                sum.add(tipPoint, baseRadius, baseRadius);

                final double tipX = transformX(sum.getReal());
                final double tipY = transformY(sum.getImaginary());
                final double tipSize = state.getTipSize(baseTipSize);

                g.setColor(Colors.getTipColor(graphingInCenter));
                if (DRAW_CIRCULAR_TIP_JOINTS && i < count - 1) {
                    g.fill(new Ellipse2D.Double(tipX - (tipSize / 2), tipY - (tipSize / 2), tipSize, tipSize));
                } else {
                    // Last rotor: Triangular tip
                    final AffineTransform prev = g.getTransform();
                    g.rotate(transform(tipPoint).getArgument() + (Math.PI / 2), tipX, tipY);
                    g.fill(new Triangle(tipX - (tipSize / 2), tipY, tipSize, tipSize));
                    g.setTransform(prev);       // restore
                }

                // Radius
                g.setStroke(strokes.rotorRadius);
                g.setColor(Colors.getRadiusColor(graphingInCenter));
                g.draw(new Line2D.Double(centerX, centerY, tipX, tipY));

                prevX = sum.getReal();
                prevY = sum.getImaginary();
            }

            // Update Wave
            final Complex rawTip = sum.toComplex();
            final WavePoint finalTip = createWavePoint(rawTip, input, baseRadius);
            wave.addFirst(finalTip);

            tipToWaveJoint = new Line2D.Double(new Point2D.Double(transformX(rawTip.getReal()) - waveOffsetX, transformY(rawTip.getImaginary())), parseWavePoint(0, finalTip, baseRadius));
        }

        /* ..............................  Main Wave ............................. */
        g.translate(waveOffsetX, 0);

        // Final Tip - Wave Joint
        if (tipToWaveJoint != null) {
            g.setStroke(strokes.rotorToWaveJoint);
            g.setColor(Colors.getTipToWaveJointColor(graphingInCenter));
            g.draw(tipToWaveJoint);
        }

        final boolean joinPoints = mPointsJoiningEnabled;
        final Color waveColor = Colors.getDynamicWaveColor(mRotorStateManager.getId());
        final ListIterator<WavePoint> itr = wave.listIterator();
        int i; WavePoint wp;

        Point2D prevPoint = null;

        g.setStroke(strokes.wave);
        while (itr.hasNext()) {
            i = itr.nextIndex();
            wp = itr.next();

            final Point2D point = parseWavePoint(i, wp, baseRadius);
            g.setColor(wp.getColorOrDefault(waveColor));
            g.draw(new Line2D.Double(joinPoints && prevPoint != null? prevPoint: point, point));
            prevPoint = point;
        }

        // purge
        if (wave.size() > MAX_WAVE_POINTS) {
            wave.removeLast();
        }

        g.dispose();
    }


    public void update() {
        repaint();
    }

    protected void mainLoop() {
        final boolean updated = mDomainAnimator.update();
        if (updated) {
            update();
        }
    }

    @Override
    public final void run() {
        mainLoop();
    }


    protected void onIsPlayingChanged(boolean playing) {
        update();
        forEachPanelListener(l -> l.onIsPlayingChanged(playing));
    }

    public final boolean isPlaying() {
        return mDomainAnimator.isRunning();
    }

    public final boolean isPaused() {
        return mDomainAnimator.isPaused();
    }

    public final void setPlay(boolean play) {
        if (isPlaying() == play) {
            return;
        }

        if (play) {
            if (mRotorStateManager.isNoOp() || mRotorStateManager.getRotorCount() == 0)
                return;

            if (mDomainAnimator.isEnded()) {
                mDomainAnimator.reset();
            }

            mDomainAnimator.start();
        } else {
            mDomainAnimator.pause();
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


    protected void onRepeatModeChanged(@NotNull AbstractAnimator.RepeatMode repeatMode) {
        forEachPanelListener(l -> l.onRepeatModeChanged(repeatMode));
    }

    public final void setRepeatMode(@Nullable AbstractAnimator.RepeatMode repeatMode) {
        if (repeatMode == null) {
            repeatMode = DEFAULT_REPEAT_MODE;
        }

        mDomainAnimator.setRepeatMode(repeatMode);
    }

    @NotNull
    public final AbstractAnimator.RepeatMode getRepeatMode() {
        return mDomainAnimator.getRepeatMode();
    }



    public final boolean isPointsJoiningEnabled() {
        return mPointsJoiningEnabled;
    }

    protected void onPointsJoiningEnabledChanged(boolean pointsJoiningEnabled) {
        update();
        forEachPanelListener(l -> l.onPointsJoiningEnabledChanged(pointsJoiningEnabled));
    }

    public final void setJoinPointsEnabled(boolean pointsJoiningEnabled) {
        if (mPointsJoiningEnabled != pointsJoiningEnabled) {
            mPointsJoiningEnabled = pointsJoiningEnabled;
            onPointsJoiningEnabledChanged(pointsJoiningEnabled);
        }
    }

    public boolean togglePointsJoining() {
        final boolean now = !isPointsJoiningEnabled();
        setJoinPointsEnabled(now);
        return now;
    }


    protected void onAutoTrackInCenterChanged(boolean autoTrackInCenter) {
        update();
        forEachPanelListener(l -> l.onAutoTrackInCenterChanged(autoTrackInCenter));
    }

    private void setAutoTrackInCenterInternal(boolean autoTrackInCenter) {
        mAutoTrackInCenter = autoTrackInCenter;
        onAutoTrackInCenterChanged(autoTrackInCenter);
    }

    public final void setAutoTrackInCenter(boolean autoTrackInCenter) {
        if (mAutoTrackInCenter == autoTrackInCenter)
            return;
        setAutoTrackInCenterInternal(autoTrackInCenter);
    }

    public final boolean toggleAutoTrackInCenter() {
        final boolean n = !mAutoTrackInCenter;
        setAutoTrackInCenterInternal(n);
        return n;
    }

    public final boolean isAutoTrackInCenterEnabled() {
        return mAutoTrackInCenter;
    }


//    public final int getLoopDelay() {
//        return mLooper.getDelay();
//    }
//
//    public final void setLoopDelay(int loopDelay) {
//        mLooper.setDelay(loopDelay);
//    }


    public final boolean hasColorOverrides() {
        return mRotorStateManager.getColorProvider() != null;
    }

    public final void setHueCycleEnabled(boolean enabled) {
        if (enabled) {
            mRotorStateManager.hueCycle();
        } else {
            mRotorStateManager.setColorProvider(null);
        }
    }

    public boolean isHueCycleEnabled() {
        return hasColorOverrides();
    }

    public boolean toggleHueCycle() {
        final boolean now = !isHueCycleEnabled();
        setHueCycleEnabled(now);
        return now;
    }

//    @Nullable
//    public final FourierUi getFourierUI() {
//        final Window w = SwingUtilities.windowForComponent(FourierSeriesPanel.this);
//        return w instanceof FourierUi? (FourierUi) w: null;
//    }


    private class ComponentListener implements java.awt.event.ComponentListener {

        @Override
        public void componentResized(ComponentEvent e) {
            if (mDrag != null && isMaxDragDimensionDependent()) {
                setDrag(mDrag.copy());      // update
            }
        }

        @Override
        public void componentMoved(ComponentEvent e) {
        }

        @Override
        public void componentShown(ComponentEvent e) {

        }

        @Override
        public void componentHidden(ComponentEvent e) {

        }
    }


    private class MouseHandler implements MouseListener, MouseMotionListener, MouseWheelListener {

        @Nullable
        private Point2D mMouseDragStartPoint;
        @Nullable
        private Size mMouseDragStart;     // For each Press-Release

        // Mouse main

        @Override
        public void mouseClicked(MouseEvent e) {
//            FourierUi ui;
//            if (e.getClickCount() == 2 && (ui = getFourierUI()) != null) {
//                ui.toggleFullscreen();
//            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (shouldDragOnMousePress(e)) {
                mMouseDragStartPoint = e.getPoint();
                mMouseDragStart = mDrag;
            } else {
                mMouseDragStartPoint = null;
                mMouseDragStart = null;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            mMouseDragStartPoint = null;
            mMouseDragStart = null;
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        // Mouse Motion

        @Override
        public void mouseDragged(MouseEvent e) {
            final Point2D startPoint = mMouseDragStartPoint;
            final Size startDrag = mMouseDragStart;
            if (startPoint != null) {
                final Size del = new Size(startPoint, e.getPoint());
                setDrag(startDrag != null? startDrag.add(del): del);
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            // no-op
        }

        // Mouse Wheel

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (!shouldScaleByMouseWheel(e))
                return;

//            final boolean pivotChanged = setScalePivot(e.getPoint(), false);
            final boolean scaleChanged = increaseScale(getScaleIncrement(e), false);

//            if (pivotChanged || scaleChanged) {
//                update();
//            }

            if (scaleChanged) {
                update();
            }
        }
    }


}
