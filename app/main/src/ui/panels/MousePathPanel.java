package ui.panels;

import action.BaseAction;
import app.Colors;
import app.R;
import live.Listeners;
import misc.*;
import models.Size;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ui.action.ActionInfo;
import util.main.PathUtil;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.*;

public class MousePathPanel extends JPanel {

    public static final String TAG = "MousePathPanel";

    private static final boolean FORCE_ANTIALIASING = true;

    private static final double DEFAULT_MINIMUM_SCALE = 0.1;
    private static final double DEFAULT_MAXIMUM_SCALE = 50;
    private static final double SCALE_WHEEL_ROTATION_MULTIPLIER = 0.2;
    private static final double DEFAULT_SCALE_UNIT_INCREMENT = 0.5;
    private static final double DEFAULT_SCALE_UNIT_DECREMENT_BELOW_1 = 0.1;

    private static final double DRAG_X_UNITS = 50;
    private static final double DRAG_Y_UNITS = 50;

    private static final boolean DEFAULT_JOIN_POINTS = true;
    public static final boolean DEFAULT_INVERT_X = false;
    public static final boolean DEFAULT_INVERT_Y = false;

    private static final boolean UNDO_ENABLED = true;
    private static final int MAX_UNDO_COUNT = 20;
    private static final int MAX_REDO_COUNT = 20;

    private static final boolean LINE_INTERPOLATION_ENABLED = false;            // approximation accuracy decreases with more points
    private static final int LINE_INTERPOLATION_MAX_POINT_COUNT = 10000;

    @NotNull
    private static LinkedList<Point2D> interpolateLine(@NotNull Point2D p0, @NotNull Point2D p1, int count) {
        final LinkedList<Point2D> r = new LinkedList<>();
        r.add(p0);

        if (count > 2) {
            final float step = 1f / (count - 1);

            for (int i=1; i < count - 1; i++) {
                r.add(PathUtil.interpolateLinear(p0, p1, i * step));
            }
        }

        r.add(p1);
        return r;
    }

    private static final Stroke STROKE_PATH = new BasicStroke(1f);
    private static final Stroke STROKE_PATH_OPEN = new BasicStroke(1.25f);

    private static final Color COLOR_PATH = Colors.ACCENT_FG_LIGHT;
    private static final Color COLOR_PATH_OPEN = Colors.ACCENT_FG_DARK;


    @NotNull
    private static LinkedList<Point2D> copyPath(Collection<Point2D> src, boolean deep) {
        final LinkedList<Point2D> dest = new LinkedList<>();
        if (CollectionUtil.notEmpty(src)) {
            if (deep) {
                src.forEach(p -> dest.add(new Point2D.Double(p.getX(), p.getY())));
            } else {
                CollectionUtil.addAll(src, dest);
            }
        }

        return dest;
    }

    @NotNull
    private static LinkedList<LinkedList<Point2D>> copyPaths(Collection<? extends Collection<Point2D>> src, boolean deep) {
        final LinkedList<LinkedList<Point2D>> dest = new LinkedList<>();
        if (CollectionUtil.notEmpty(src)) {
            src.forEach(path -> dest.add(copyPath(path, deep)));
        }

        return dest;
    }

    /* Stick Mode */

    private enum StickMode {
        LINE(InputEvent.SHIFT_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK,
                InputEvent.CTRL_DOWN_MASK),

        AXIAL_LINE(InputEvent.CTRL_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK,
                InputEvent.SHIFT_DOWN_MASK);

        public final int onMask;
        public final int offMask;

        StickMode(int onMask, int offMask) {
            this.onMask = onMask;
            this.offMask = offMask;
        }

        public boolean shouldStickOnMouseEvent(int modifiersEx) {
            return Flaggable.checkOnOff(modifiersEx, onMask, offMask);
        }

        @Nullable
        public static StickMode fromMouseEvent(int modifiersEx) {
            // explicit checking, efficient than array loop since it is called many times

            if (LINE.shouldStickOnMouseEvent(modifiersEx))
                return LINE;

            if (AXIAL_LINE.shouldStickOnMouseEvent(modifiersEx))
                return AXIAL_LINE;

            return null;
        }
    }


    public interface Listener {

        void onPointsJoiningEnabledChanged(@NotNull MousePathPanel panel, boolean enabled);

        void onInvertXChanged(@NotNull MousePathPanel panel, boolean invertX);

        void onInvertYChanged(@NotNull MousePathPanel panel, boolean invertY);

        void onScaleChanged(@NotNull MousePathPanel panel, double scale);

        void onDragChanged(@NotNull MousePathPanel panel, @Nullable Size drag);

        void onEraseModeEnabledChanged(@NotNull MousePathPanel panel, boolean enabled);

        void onPathCountChanged(@NotNull MousePathPanel panel, int count);

        void onUndoStackChanged(@NotNull MousePathPanel panel);

        void onRedoStackChanged(@NotNull MousePathPanel panel);
    }

//    @Nullable
//    private Path2D mCurPath;
//
//    @NotNull
//    private final List<Path2D> mPaths = new LinkedList<>();


    private record UndoRecord(@NotNull Runnable undo, @Nullable Runnable redo) {
    }


    @NotNull
    private final LinkedList<LinkedList<Point2D>> paths = new LinkedList<>();
    @NotNull
    private final LinkedList<UndoRecord> mUndoStack = new LinkedList<>();
    @NotNull
    private final LinkedList<Runnable> mRedoStack = new LinkedList<>();

    private boolean mPathOpen;
    private boolean mEraseMode;
    private volatile boolean mJoinPoints = DEFAULT_JOIN_POINTS;
    private boolean mInvertX = DEFAULT_INVERT_X;
    private boolean mInvertY = DEFAULT_INVERT_Y;

    private double mScale = 1;
    @Nullable
    private Size mDrag;

    @NotNull
    private final MouseHandler mMouseHandler = new MouseHandler();
    @Nullable
    private volatile BaseAction mClearAction;
    @Nullable
    private volatile BaseAction mEraseAction;
    @Nullable
    private volatile BaseAction mUndoAction;
    @Nullable
    private volatile BaseAction mRedoAction;
    @Nullable
    private volatile BaseAction mJoinPointsAction;

    @NotNull
    private final Listeners<Listener> mListeners = new Listeners<>();

    public MousePathPanel() {
        setBackground(Colors.BG_DARK);

        addMouseListener(mMouseHandler);
        addMouseMotionListener(mMouseHandler);
        addMouseWheelListener(mMouseHandler);
    }


    public void addListener(@NotNull Listener l) {
        mListeners.addListener(l);
    }

    public boolean removeListener(@NotNull Listener l) {
        return mListeners.removeListener(l);
    }

    public boolean containsListener(@NotNull Listener l) {
        return mListeners.containsListener(l);
    }

    public void ensureListener(@NotNull Listener l) {
        mListeners.ensureListener(l);
    }


    private void update() {
        repaint();
    }

    private void drawPath(@NotNull Graphics2D g, @NotNull List<Point2D> points, boolean joinPoints) {
        Point2D prevPoint = null;
        for (Point2D point: points) {
            g.draw(new Line2D.Double(joinPoints && prevPoint != null? prevPoint : point, point));
            prevPoint = point;
        }
    }

    private void drawPath(@NotNull Graphics2D g, @NotNull List<Point2D> points) {
        drawPath(g, points, mJoinPoints);
    }

    @Override
    protected void paintComponent(Graphics _g) {
        super.paintComponent(_g);

        final Graphics2D g = (Graphics2D) _g;
        if (FORCE_ANTIALIASING) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }


        final int width = getWidth();
        final int height = getHeight();
        final boolean joinPoints = mJoinPoints;
        final boolean invertX = mInvertX;
        final boolean invertY = mInvertY;
        final boolean open = mPathOpen;


        /* ..........................  Pre-Transforms ...........................*/
        final AffineTransform t = g.getTransform();

        // 1. Translate
        double tx = width / 2f, ty = height / 2f;      // initial
        final Size drag = getDrag();
        if (drag != null) {
            tx += drag.width; ty += drag.height;
        }

        t.translate(tx, ty);

        // 2. Scale
        final double scale = mScale;
        t.scale(scale * (invertX? -1: 1), scale * (invertY? -1: 1));
        g.setTransform(t);

        /* ............................... Drawing ........................ */

//        // 1. Current Path
//        final Path2D current = mCurPath;
//        if (current != null) {
//            g.setColor(Colors.ACCENT_FG_DARK);
//            g.draw(current);
//        }
//
//        // 2. Other Paths
//        if (!mPaths.isEmpty()) {
//            g.setColor(Colors.ACCENT_FG_LIGHT);
//            for (Path2D gp: mPaths) {
//                g.draw(gp);
//            }
//        }




        // 1. Current path
        final List<Point2D> cur;
        if (open && CollectionUtil.notEmpty((cur = paths.peekLast()))) {
            g.setStroke(STROKE_PATH_OPEN);
            g.setColor(COLOR_PATH_OPEN);
            drawPath(g, cur, true);
        }

        // Others
        final int o_size = paths.size() - (open? 1: 0);
        if (o_size > 0) {
            final ListIterator<LinkedList<Point2D>> itr = paths.listIterator();

            g.setStroke(STROKE_PATH);
            g.setColor(COLOR_PATH);

            while (itr.hasNext() && itr.nextIndex() < o_size) {
                drawPath(g, itr.next(), joinPoints);
            }
        }


        /* ...............................  Overlays  ..................................  */

        final Rectangle2D.Double _eraseRect;
        if (mEraseMode && (_eraseRect = eraseRect) != null) {
            g.setColor(new Color(90, 90, 90, 128));
            g.fill(_eraseRect);
        }
    }



    @NotNull
    protected Point2D.Double transformPoint(@NotNull Point2D point) {
        final Size drag = mDrag;
        final double scale = mScale;

        double x = point.getX() - (getWidth() / 2f);
        double y = point.getY() - (getHeight() / 2f);
        if (drag != null) {
            x -= drag.width;
            y -= drag.height;
        }

        x /= scale;
        y /= scale;
        return new Point2D.Double(x, y);
    }


    protected void onPathStart(@NotNull LinkedList<Point2D> path) {

    }

    protected void onPathEnd(@Nullable LinkedList<Point2D> path) {
        syncPathActions();
    }

    private void startPath(@NotNull Point2D anchor) {
//        final Path2D old = mCurPath;
//        if (old != null) {
//            endPath(false);
//        }

        endPath();

//        final GeneralPath path = new GeneralPath();
//        mCurPath = path;

        final LinkedList<Point2D> path = new LinkedList<>();
        path.addLast(anchor);

        addPathList(Collections.singleton(path), true);
        mPathOpen = true;
        onPathStart(path);

//        path.moveTo(anchor.getX(), anchor.getY());
        update();
    }

    private void onNewPoint(@NotNull Point2D point, @Nullable StickMode stick) {
        final LinkedList<Point2D> curPath = paths.peekLast();
        if (curPath == null) {
            startPath(point);
        } else {
            if (stick != null && curPath.size() > 1) {
                curPath.pollLast();

                if (stick == StickMode.AXIAL_LINE) {
                    // change point base on stick anchor
                    final Point2D a = curPath.peekLast();       // anchor
                    if (a != null) {
                        final double delX = Math.abs(a.getX() - point.getX());
                        final double delY = Math.abs(a.getY() - point.getY());

                        if (delX < delY) {
                            point.setLocation(a.getX(), point.getY());          // stick to y-axis
                        } else {
                            point.setLocation(point.getX(), a.getY());          // stick to X-axis
                        }
                    }
                }
            }

            curPath.addLast(point);
            update();
        }
    }




    private void endPath() {
        if (!mPathOpen) {
            return;
        }

        mPathOpen = false;
        LinkedList<Point2D> path = paths.peekLast();
        if (path != null && path.size() == 2 && LINE_INTERPOLATION_ENABLED) {     // line
            final Point2D p0 = path.getFirst();
            final Point2D p1 = path.getLast();

            path = interpolateLine(p0, p1, Math.min((int) (p0.distance(p1) / mScale), LINE_INTERPOLATION_MAX_POINT_COUNT));
            paths.pollLast();
            paths.addLast(path);
        }

        onPathEnd(path);
        update();
    }



    public int getPathCount() {
        return paths.size();
    }

    public boolean isClear() {
        return paths.isEmpty();
    }


    protected void onPathCountChanged(int count) {
        syncPathActions();

        mListeners.forEachListener(l -> l.onPathCountChanged(this, count));
    }

    protected void onPathCountChanged() {
        onPathCountChanged(paths.size());
    }

    public void addPathList(Collection<LinkedList<Point2D>> pathsToAdd, boolean undo) {
        if (CollectionUtil.isEmpty(pathsToAdd))
            return;

        endPath();      // end current path
        if (paths.addAll(pathsToAdd)) {
            onPathCountChanged();
            update();

            if (undo && UNDO_ENABLED) {
                enqueueUndoAction(new UndoRecord(() -> removePathList(pathsToAdd, false), () -> addPathList(pathsToAdd, true)));
            }
        }
    }

    public void addPaths(Collection<? extends Collection<Point2D>> pathsToAdd, boolean undo) {
        addPathList(copyPaths(pathsToAdd, false), undo);
    }


    private void removePathList(Collection<LinkedList<Point2D>> pathsToRemove, boolean undo) {
        if (CollectionUtil.isEmpty(pathsToRemove))
            return;

        endPath();      // end current path
        if (paths.removeAll(pathsToRemove)) {
            onPathCountChanged();
            update();

            if (undo && UNDO_ENABLED) {
                enqueueUndoAction(new UndoRecord(() -> addPathList(pathsToRemove, false), () -> removePathList(pathsToRemove, true)));
            }
        }
    }


    public void clear(boolean undo) {
        if (paths.isEmpty())
            return;

        final LinkedList<LinkedList<Point2D>> copy = undo && UNDO_ENABLED? CollectionUtil.linkedListCopy(paths): null;
        paths.clear();
        mPathOpen = false;
        onPathCountChanged(0);
        update();

        if (undo && CollectionUtil.notEmpty(copy)) {
            enqueueUndoAction(new UndoRecord(() -> addPathList(copy, false), () -> removePathList(copy, true)));
        }
    }


    @NotNull
    public List<Path2D> getIndividualPaths(boolean closeEach, boolean withCurrentLive) {
        final LinkedList<LinkedList<Point2D>> copy = CollectionUtil.linkedListCopy(paths);
        if (!withCurrentLive && mPathOpen) {
            copy.pollLast();
        }

        final List<Path2D> result = new LinkedList<>();
        for (List<Point2D> points: copy) {
            final Path2D gp = PathUtil.createPath(points, closeEach);
            if (gp != null) {
                result.add(gp);
            }
        }

        return result;
    }

    @NotNull
    public GeneralPath getFinalPath(boolean withCurrentLive, boolean connectPaths) {
        return PathUtil.mergePaths(getIndividualPaths(false, withCurrentLive), connectPaths);
    }



    protected void onJoinPointsChanged(boolean joinPoints) {
        update();

        final BaseAction jp = mJoinPointsAction;
        if (jp != null) {
            jp.sync();
        }

        mListeners.forEachListener(l -> l.onPointsJoiningEnabledChanged(this, joinPoints));
    }

    public boolean isJoiningPoints() {
        return mJoinPoints;
    }

    public boolean setJoinPoints(boolean joinPoints) {
        final boolean jp = mJoinPoints;
        if (jp == joinPoints) {
            return false;
        }

        mJoinPoints = joinPoints;
        onJoinPointsChanged(joinPoints);
        return true;
    }

    public boolean toggleJoinPoints() {
        final boolean newState = !mJoinPoints;
        setJoinPoints(newState);
        return newState;
    }


    private void onInvertYChanged(boolean yInverted) {
        update();
        mListeners.forEachListener(l -> l.onInvertYChanged(this, yInverted));
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
        mListeners.forEachListener(l -> l.onInvertXChanged(this, xInverted));
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
        return DEFAULT_MAXIMUM_SCALE;
    }

    protected void onScaleChanged(double scale, boolean update) {
        if (update) {
            update();
        }

        mListeners.forEachListener(l -> l.onScaleChanged(this, scale));
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

    protected boolean shouldDrag(@NotNull MouseEvent e) {
        return e.getButton() == MouseEvent.BUTTON3;
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
        mListeners.forEachListener(l -> l.onDragChanged(this, copy));
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


    /* Erase */


    @Nullable
    private Point2D eraseStart;
    @Nullable
    private Rectangle2D.Double eraseRect;

    protected void onEraseModeEnabledChanged(boolean enabled) {
        if (!enabled) {
            eraseStart = null;
            eraseRect = null;
        }

        syncPathActions();
        update();

        mListeners.forEachListener(l -> l.onEraseModeEnabledChanged(this, enabled));
    }

    protected void onEraseStarted(@NotNull Point2D pivot) {

    }

    protected void onEraseEnded(@Nullable Rectangle2D eraseRectangle, boolean changed) {
        if (!changed) {
            update();
        }
    }


    public boolean isEraseModeEnabled() {
        return mEraseMode;
    }

    public boolean setEraseModeEnabled(boolean enabled) {
        if (mEraseMode == enabled)
            return false;

        mEraseMode = enabled;
        onEraseModeEnabledChanged(enabled);
        return true;
    }

    public boolean toggleEraseModeEnabled() {
        final boolean me = !mEraseMode;
        setEraseModeEnabled(me);
        return me;
    }


    private void startErase(@NotNull Point2D start) {
        if (!mEraseMode)
            return;

        eraseStart = start;
        eraseRect = new Rectangle2D.Double(eraseStart.getX(), eraseStart.getY(), 0, 0);

        onEraseStarted(start);
        update();
    }

    private void updateEraseRect(@NotNull Point2D end) {
        final Point2D start;
        if (!mEraseMode || (start = eraseStart) == null)
            return;

        final double xMIn, xMax, yMin, yMax;

        if (start.getX() <= end.getX()) {
            xMIn = start.getX();
            xMax = end.getX();
        } else {
            xMIn = end.getX();
            xMax = start.getX();
        }

        if (start.getY() <= end.getY()) {
            yMin = start.getY();
            yMax = end.getY();
        } else {
            yMin = end.getY();
            yMax = start.getY();
        }

        Rectangle2D.Double rect = eraseRect;
        if (rect == null) {
            rect = new Rectangle2D.Double();
            eraseRect = rect;
        }

        rect.x = xMIn;
        rect.y = yMin;
        rect.width = xMax - xMIn;
        rect.height = yMax - yMin;
        update();
    }


    private boolean erase(@NotNull Rectangle2D rect, boolean undo) {
        if (CollectionUtil.isEmpty(paths))
            return false;

        final LinkedList<LinkedList<Point2D>> copy = undo && UNDO_ENABLED? copyPaths(paths, false): null;
        final AtomicBoolean changed = new AtomicBoolean(false);

        paths.forEach(path -> {
            if (path.removeIf(rect::contains)) {
                changed.set(true);
            }
        });

        if (!changed.get())
            return false;

        paths.removeIf(Collection::isEmpty);        // prune
        onPathCountChanged();
        update();

        if (undo && CollectionUtil.notEmpty(copy)) {
            enqueueUndoAction(new UndoRecord(() -> {
                paths.clear();
                addPathList(copy, false);
            }, () -> erase(rect, true)));
        }

        return true;
    }

    private void endErase() {
        if (!mEraseMode)
            return;

        final Rectangle2D rect = eraseRect;
        eraseStart = null;
        eraseRect = null;

        boolean changed = false;
        if (rect != null) {
            changed = erase(rect, true);
        }

        onEraseEnded(rect, changed);
    }




//    @Nullable
//    public LinkedList<Point2D> removeLastPath() {
//        final LinkedList<Point2D> last = paths.pollLast();
//        if (last == null)
//            return null;
//
//        onPathCountChanged();
//        update();
//        return last;
//    }

    /* Undo-Redo */

    public int getUndoStackCount() {
        return mUndoStack.size();
    }

    private void enqueueUndoAction(@NotNull UndoRecord record) {
        if (!UNDO_ENABLED)
            return;

        if (mUndoStack.size() >= MAX_UNDO_COUNT)
            mUndoStack.removeFirst();

        mUndoStack.addLast(record);
        onUndoStackChanged();
    }

    public int getRedoStackCount() {
        return mRedoStack.size();
    }

    private void enqueueRedoAction(@NotNull Runnable action) {
        if (!UNDO_ENABLED)
            return;

        if (mRedoStack.size() >= MAX_REDO_COUNT)
            mRedoStack.removeFirst();

        mRedoStack.addLast(action);
        onRedoStackChanged();
    }

    protected void onUndoStackChanged() {
        final BaseAction ua = mUndoAction;
        if (ua != null) {
            ua.sync();
        }

        mListeners.forEachListener(l -> l.onUndoStackChanged(this));
    }

    protected void onRedoStackChanged() {
        final BaseAction ra = mRedoAction;
        if (ra != null) {
            ra.sync();
        }

        mListeners.forEachListener(l -> l.onRedoStackChanged(this));
    }


    public boolean canUndo() {
        return getUndoStackCount() > 0;
    }

    public boolean canRedo() {
        return getRedoStackCount() > 0;
    }

    public boolean undo() {
        final UndoRecord record = mUndoStack.pollLast();
        if (record == null)
            return false;

        onUndoStackChanged();
        record.undo.run();

        if (record.redo != null) {
            enqueueRedoAction(record.redo);
        }

        return true;
    }

    public boolean redo() {
        final Runnable action = mRedoStack.pollLast();
        if (action == null)
            return false;

        onRedoStackChanged();
        action.run();
        return true;
    }




    private boolean shouldStartFromLastPathEnd(@NotNull MouseEvent e) {
        return Flaggable.hasAllFlags(e.getModifiersEx(), InputEvent.ALT_DOWN_MASK);
    }

    private class MouseHandler implements MouseListener, MouseMotionListener, MouseWheelListener {

        @Nullable
        private Point2D mMouseDragStartPoint;
        @Nullable
        private Size mMouseDragStart;     // For each Press-Release


        /* Mouse */

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (shouldDrag(e)) {
                mMouseDragStartPoint = e.getPoint();
                mMouseDragStart = mDrag;
            } else {
                mMouseDragStartPoint = null;
                mMouseDragStart = null;

                if (mEraseMode) {
                    startErase(transformPoint(e.getPoint()));
                } else {
                    Point2D anchor = null;

                    final LinkedList<Point2D> lastPath;
                    if (shouldStartFromLastPathEnd(e) && (lastPath = paths.peekLast()) != null) {
                        final Point2D lastPoint = lastPath.peekLast();
                        if (lastPoint != null) {
                            anchor = new Point2D.Double(lastPoint.getX(), lastPoint.getY());
                        }
                    }

                    if (anchor == null) {
                        anchor = transformPoint(e.getPoint());
                    }

                    startPath(anchor);
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            mMouseDragStartPoint = null;
            mMouseDragStart = null;

            if (mEraseMode) {
                endErase();
            } else {
                endPath();
            }
        }

        /* Motion */

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        @Override
        public void mouseDragged(MouseEvent e) {
            final Point2D startDragPoint = mMouseDragStartPoint;
            final Size startDrag = mMouseDragStart;

            if (startDragPoint != null) {
                final Size del = new Size(startDragPoint, e.getPoint());
                setDrag(startDrag != null? startDrag.add(del): del);
            } else {
                if (mEraseMode) {
                    updateEraseRect(transformPoint(e.getPoint()));
                } else {
                    onNewPoint(transformPoint(e.getPoint()), StickMode.fromMouseEvent(e.getModifiersEx()));
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }

        /* Wheel */

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




    /* ................................ Actions ...............................*/

    @NotNull
    public BaseAction getClearAction() {
        BaseAction a = mClearAction;
        if (a == null) {
            synchronized (this) {
                a = mClearAction;
                if (a == null) {
                    a = new ClearAction();
                    mClearAction = a;
                }
            }
        }

        return a;
    }

    @NotNull
    public BaseAction getEraseAction() {
        BaseAction a = mEraseAction;
        if (a == null) {
            synchronized (this) {
                a = mEraseAction;
                if (a == null) {
                    a = new EraseAction();
                    mEraseAction = a;
                }
            }
        }

        return a;
    }

    @NotNull
    public BaseAction getUndoAction() {
        BaseAction a = mUndoAction;
        if (a == null) {
            synchronized (this) {
                a = mUndoAction;
                if (a == null) {
                    a = new UndoAction(true);
                    mUndoAction = a;
                }
            }
        }

        return a;
    }

    @NotNull
    public BaseAction getRedoAction() {
        BaseAction a = mRedoAction;
        if (a == null) {
            synchronized (this) {
                a = mRedoAction;
                if (a == null) {
                    a = new RedoAction();
                    mRedoAction = a;
                }
            }
        }

        return a;
    }

    @NotNull
    public BaseAction getJoinPointsAction() {
        BaseAction a = mJoinPointsAction;
        if (a == null) {
            synchronized (this) {
                a = mJoinPointsAction;
                if (a == null) {
                    a = new JoinPointsAction();
                    mJoinPointsAction = a;
                }
            }
        }

        return a;
    }


    private void syncPathActions() {
        final BaseAction ca = mClearAction;
        if (ca != null) {
            ca.sync();
        }

        final BaseAction ea = mEraseAction;
        if (ea != null) {
            ea.sync();
        }

        final BaseAction rl = mUndoAction;
        if (rl != null) {
            rl.sync();
        }
    }





    private class ClearAction extends BaseAction {

        private ClearAction() {
            this.setName("Clear");
            this.setShortDescription("Clear Canvas");
            this.setLargeIcon(R.createLargeIcon(R.IMG_DELETE_LIGHT_64));
            sync();
        }

        public void sync() {
            super.sync();
            this.setEnabled(!isClear());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            clear(true);
        }
    }


    private class EraseAction extends BaseAction {

        private EraseAction() {
            this.setName("Erase");
            this.setShortDescription("Erase Area");
            this.setLargeIconOnSelect(false, R.createLargeIcon(R.IMG_ERASE_LIGHT_64));
            this.setLargeIconOnSelect(true, R.createLargeIcon(R.IMG_ERASE_ACCENT_64));
            this.sync();
        }

        public void sync() {
            super.sync();

            final boolean isErasing = isEraseModeEnabled();

            this.setEnabled(isErasing || !isClear());
            this.setSelected(isErasing);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            toggleEraseModeEnabled();
        }
    }

    private class UndoAction extends BaseAction {

        private UndoAction(boolean saveInStack) {
            this.setName("Undo");
            this.setShortDescription("Undo [ctrl-Z]");
            this.setLargeIcon(R.createLargeIcon(R.IMG_UNDO_LIGHT_64));
            sync();
        }

        public void sync() {
            super.sync();
            this.setEnabled(canUndo());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            undo();
        }
    }


    private class RedoAction extends BaseAction {

        private RedoAction() {
            this.setName("Redo");
            this.setShortDescription("Redo [ctrl-X]");
            this.setLargeIcon(R.createLargeIcon(R.IMG_REDO_LIGHT_64));
            sync();
        }

        public void sync() {
            super.sync();
            this.setEnabled(canRedo());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            redo();
        }
    }


    private class JoinPointsAction extends BaseAction {

        private JoinPointsAction() {
            this.useInfo(ActionInfo.TOGGLE_POINTS_JOIN);
            this.setLargeIconOnSelect(false, R.createLargeIcon(R.IMG_POINTS_LIGHT_64));
            this.setLargeIconOnSelect(true, R.createLargeIcon(R.IMG_POINTS_ACCENT_64));
            this.sync();
        }

        public void sync() {
            super.sync();
            this.setSelected(isJoiningPoints());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            toggleJoinPoints();
        }
    }

}