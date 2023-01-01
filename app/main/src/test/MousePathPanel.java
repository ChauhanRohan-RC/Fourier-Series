package test;

import action.BaseAction;
import app.Colors;
import misc.CollectionUtil;
import misc.MathUtil;
import models.Size;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ui.util.Ui;
import util.main.PathUtil;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.swing.*;

public class MousePathPanel extends JPanel {

    private static final boolean FORCE_ANTIALIASING = true;

    private static final double DEFAULT_MINIMUM_SCALE = 0.1;
    private static final double DEFAULT_MAXIMUM_SCALE = 50;
    private static final double SCALE_WHEEL_ROTATION_MULTIPLIER = 0.2;
    private static final double DEFAULT_SCALE_UNIT_INCREMENT = 0.5;
    private static final double DEFAULT_SCALE_UNIT_DECREMENT_BELOW_1 = 0.1;

    private static final double DRAG_X_UNITS = 50;
    private static final double DRAG_Y_UNITS = 50;


    @Nullable
    private Point2D mCurAnchor;
    @Nullable
    private Path2D mCurPath;

    @NotNull
    private final List<Path2D> mPaths = new LinkedList<>();

    private double mScale = 1;
    @Nullable
    private Size mDrag;

    @NotNull
    private final MouseHandler mMouseHandler = new MouseHandler();
    @Nullable
    private ClearAction mClearAction;

    public MousePathPanel() {
        setBackground(Colors.BG_DARK);

        addMouseListener(mMouseHandler);
        addMouseMotionListener(mMouseHandler);
        addMouseWheelListener(mMouseHandler);
    }

    @Override
    protected void paintComponent(Graphics _g) {
        super.paintComponent(_g);

        final Graphics2D g = (Graphics2D) _g;
        if (FORCE_ANTIALIASING) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }


        /* ..........................  Pre-Transforms ...........................*/
        final AffineTransform t = g.getTransform();

        // 1. Translate
        double tx = 0, ty = 0;      // initial
        final Size drag = getDrag();
        if (drag != null) {
            tx += drag.width; ty += drag.height;
        }

        t.translate(tx, ty);

        // 2. Scale
        final double scale = mScale;
        t.scale(scale, scale);
        g.setTransform(t);

        /* ............................... Drawing ........................ */


        // 1. Current Path
        final Path2D current = mCurPath;
        if (current != null) {
            g.setColor(Colors.ACCENT_FG_DARK);
            g.draw(current);
        }

        // 2. Other Paths
        if (!mPaths.isEmpty()) {
            g.setColor(Colors.ACCENT_FG_LIGHT);
            for (Path2D gp: mPaths) {
                g.draw(gp);
            }
        }
    }


    private void update() {
        repaint();
    }


    @NotNull
    protected Point2D transformPoint(@NotNull Point2D point) {
        final Size drag = mDrag;
        final double scale = mScale;

        double x = point.getX();
        double y = point.getY();
        if (drag != null) {
            x -= drag.width;
            y -= drag.height;
        }

        x /= scale;
        y /= scale;

        return new Point2D.Double(x, y);
    }

    private void startPath(@NotNull Point2D anchor) {
        final Path2D old = mCurPath;
        if (old != null) {
            endPath(false);
        }

        anchor = transformPoint(anchor);
        mCurAnchor = anchor;

        final GeneralPath path = new GeneralPath();
        mCurPath = path;

        path.moveTo(anchor.getX(), anchor.getY());
        update();
    }

    private void appendPoint(@NotNull Point2D point) {
        final Path2D cur = mCurPath;
        if (cur != null) {
            point = transformPoint(point);
            cur.lineTo(point.getX(), point.getY());
            update();
        }
    }

    private void endPath(boolean close) {
        final Point2D anchor = mCurAnchor;
        final Path2D path = mCurPath;
        mCurAnchor = null;
        mCurPath = null;

        if (anchor != null && path != null) {
            if (close) {
                path.closePath();
            }

            mPaths.add(path);
        }

        update();
    }

    public boolean hasPaths() {
        return !(mPaths.isEmpty() && mCurPath == null);
    }



    @NotNull
    public BaseAction getClearAction() {
        ClearAction a = mClearAction;
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

    protected void onCleared() {
        final ClearAction clsAction = mClearAction;
        if (clsAction != null) {
            clsAction.sync();
        }
    }

    public void clear() {
        mCurPath = null;
        mCurAnchor = null;
        mPaths.clear();

        onCleared();

        update();
    }

    @NotNull
    public List<Path2D> getPaths(boolean withCurrentLive) {
        final List<Path2D> paths = CollectionUtil.linkedListCopy(mPaths);
        final Path2D cur = mCurPath;
        if (withCurrentLive && cur != null) {
            paths.add(new Path2D.Double(cur));
        }

        return paths;
    }

    @Nullable
    public Path2D getFinalPath(boolean withCurrentLive, boolean connectPaths) {
        return PathUtil.mergePaths(getPaths(withCurrentLive), connectPaths);
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

//        forEachPanelListener(l -> l.onScaleChanged(scale));
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
        return e.getButton() == MouseEvent.BUTTON3;         // TODO
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

//        final Size copy = drag != null? drag.copy(): null;
//        forEachPanelListener(l -> l.onDragChanged(copy));
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
            if (shouldDragOnMousePress(e)) {
                mMouseDragStartPoint = e.getPoint();
                mMouseDragStart = mDrag;
            } else {
                mMouseDragStartPoint = null;
                mMouseDragStart = null;

                startPath(e.getPoint());
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            mMouseDragStartPoint = null;
            mMouseDragStart = null;

            endPath(false);
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
            final Point2D startPoint = mMouseDragStartPoint;
            final Size startDrag = mMouseDragStart;
            if (startPoint != null) {
                final Size del = new Size(startPoint, e.getPoint());
                setDrag(startDrag != null? startDrag.add(del): del);
            } else {
                appendPoint(e.getPoint());
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


    private class ClearAction extends BaseAction {

        private ClearAction() {
            this.setName("Clear");
            this.setShortDescription("Clear Canvas");
        }

        private void sync() {
            this.setEnabled(hasPaths());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            clear();
        }
    }





    public static void main(String[] args) {
        final MousePathPanel c = new MousePathPanel();
        c.setPreferredSize(new Dimension(409, 726));

        JFrame frame = new JFrame("Mouse Path");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        c.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), "clear");
        c.getActionMap().put("clear", c.getClearAction());

        frame.add(c);
        frame.setResizable(true);

        frame.setBounds(Ui.windowBoundsCenterScreen(700, 400));
        frame.setVisible(true);
    }
}