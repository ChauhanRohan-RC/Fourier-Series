package ui;

import app.App;
import app.R;
import app.Settings;
import org.jetbrains.annotations.NotNull;
import ui.action.ActionInfo;
import ui.action.UiAction;
import ui.util.Ui;

import javax.swing.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

public class BaseFrame extends JFrame implements Ui,
        UiAction.Listener,
        Settings.Listener,
        WindowListener,
        WindowStateListener,
        WindowFocusListener,
        MouseListener,
        MouseMotionListener,
        MouseWheelListener {

    @NotNull
    private final EnumMap<ActionInfo, UiAction> mActionMap = new EnumMap<>(ActionInfo.class);

    private boolean mFullscreen;

    public BaseFrame() {
        addWindowListener(this);
        addWindowStateListener(this);
        addWindowFocusListener(this);

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    /* Ui */

    @Override
    public JFrame getFrame() {
        return this;
    }

    public void update() {
        revalidate();
        repaint();
    }



    protected void onFullscreenChanged(boolean fullscreen) {
//        update();

        uia(ActionInfo.TOGGLE_FULLSCREEN)
                .setName(R.getFullscreenText(fullscreen))
                .setShortDescription(R.getFullscreenShortDescription(fullscreen))
                .setSelected(fullscreen);
    }


    public final boolean isFullscreen() {
        return mFullscreen;
    }

    protected final void setFullscreenInternal(boolean fullscreen) {
        getGraphicsConfiguration().getDevice().setFullScreenWindow(fullscreen? BaseFrame.this: null);
//        setMenuBarVisibleInternal(!fullscreen);
        mFullscreen = fullscreen;
        onFullscreenChanged(fullscreen);
    }

    public final void setFullscreen(boolean fullscreen) {
        if (mFullscreen == fullscreen)
            return;
        setFullscreenInternal(fullscreen);
    }

    public final void toggleFullscreen() {
        setFullscreen(!mFullscreen);
    }


    /* Actions */

    protected final void setupActionKeyBindings(@NotNull Collection<InputMap> inputMaps, @NotNull ActionMap actionMap) {
        ActionInfo.sharedValues().forEach(info -> {
            if (info.keyStroke != null) {
                inputMaps.forEach(im -> im.put(info.keyStroke, info));
            }

            actionMap.put(info, uia(info));
        });
    }

    protected final void setupActionKeyBindings(@NotNull JComponent component, int @NotNull ... inputMapConditions) {
        final List<InputMap> maps = new LinkedList<>();
        for (int i: inputMapConditions) {
            maps.add(component.getInputMap(i));
        }

        setupActionKeyBindings(maps, component.getActionMap());
    }


    @NotNull
    public final UiAction getUia(@NotNull ActionInfo info) {
        UiAction uia = mActionMap.get(info);

        if (uia == null) {
            uia = new UiAction(info);
            mActionMap.put(info, uia);
        }

        return uia;
    }

    @NotNull
    public final UiAction uia(@NotNull ActionInfo info) {
        final UiAction action = getUia(info);
        action.ensureListener(this);
        return action;
    }

    @Override
    public void onAction(@NotNull UiAction action, @NotNull ActionEvent e) {

    }

    @Override
    public void onActionPropertyChange(@NotNull UiAction action, @NotNull PropertyChangeEvent e) {

    }



    /* R Listener */

    @Override
    public void onLookAndFeelChanged(@NotNull String className) {
        SwingUtilities.updateComponentTreeUI(this);
    }

    @Override
    public void onFTIntegrationIntervalCountChanged(int fourierTransformSimpson13NDefault) {

    }

    /* Mouse Listeners */

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            toggleFullscreen();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {

    }



    /* Window Listeners */

    @Override
    public void windowGainedFocus(WindowEvent e) {

    }

    @Override
    public void windowLostFocus(WindowEvent e) {

    }

    @Override
    public void windowOpened(WindowEvent e) {
        Settings.getSingleton().ensureListener(this);

        App.onWindowOpen(this);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        Settings.getSingleton().removeListener(this);

        App.onWindowClose(this);
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

    @Override
    public void windowStateChanged(WindowEvent e) {

    }
}
