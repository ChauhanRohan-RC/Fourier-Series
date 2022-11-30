package misc;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class DrawableFrame extends JFrame {

    @Nullable
    private Drawable mDrawable;

    public DrawableFrame() throws HeadlessException {
        super();
    }

    public DrawableFrame(GraphicsConfiguration gc) {
        super(gc);
    }

    public DrawableFrame(String title) throws HeadlessException {
        super(title);
    }

    public DrawableFrame(String title, GraphicsConfiguration gc) {
        super(title, gc);
    }

    @Override
    public void paint(Graphics _g) {
        super.paint(_g);

        Graphics2D g = (Graphics2D) _g;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (mDrawable != null) {
            mDrawable.draw(g);
        }
    }

    public void setDrawable(@Nullable Drawable d) {
        if (mDrawable != d) {
            mDrawable = d;
            repaint();
        }
    }
}
