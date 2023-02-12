package ui.frames;

import app.App;
import app.R;
import app.Settings;
import async.Function;
import misc.CollectionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ui.action.ActionInfo;
import ui.action.UiAction;
import ui.util.Ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.util.*;
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

//    @Nullable
//    private volatile Keyboard mKeyboard;

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

//    @NotNull
//    public Keyboard getKeyboard() {
//        Keyboard kb = mKeyboard;
//        if (kb == null) {
//            synchronized (this) {
//                kb = mKeyboard;
//                if (kb == null) {
//                    kb = new Keyboard(this);
//                    mKeyboard = kb;
//                }
//            }
//        }
//
//        return kb;
//    }

    public void update() {
        revalidate();
        repaint();
    }



    protected void onFullscreenChanged(boolean fullscreen) {
        uia(ActionInfo.TOGGLE_FULLSCREEN)
                .setName(R.getFullscreenText(fullscreen))
                .setShortDescription(R.getFullscreenShortDescription(fullscreen))
                .setSelected(fullscreen);

        syncPresentationMode();
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



    protected void onMenuBarVisibilityChanged(boolean visible) {
        uia(ActionInfo.TOGGLE_MENUBAR)
                .setName(R.getToggleMenuBarText(visible))
                .setShortDescription(R.getToggleMenuBarShortDescription(visible))
                .setSelected(visible);

        syncPresentationMode();
        update();
    }

    public final boolean isMenuBarVisible() {
        final JMenuBar menuBar = getJMenuBar();
        return menuBar != null && menuBar.isVisible();
    }

    protected final void setMenuBarVisibleInternal(boolean visible) {
        final JMenuBar menuBar = getJMenuBar();
        if (menuBar == null)
            return;

        menuBar.setVisible(visible);
        onMenuBarVisibilityChanged(visible);
    }

    public final void setMenuBarVisible(boolean visible) {
        if (visible == isMenuBarVisible())
            return;

        setMenuBarVisibleInternal(visible);
    }

    public final boolean toggleMenuBarVisible() {
        final boolean newState = !isMenuBarVisible();
        setMenuBarVisibleInternal(newState);
        return newState;
    }


    @Nullable
    public Component getControlsComponent() {
        return null;
    }

    protected void onControlsVisibilityChanged(boolean controlsVisible) {
        uia(ActionInfo.TOGGLE_CONTROLS)
                .setName(R.getToggleControlsText(controlsVisible))
                .setShortDescription(R.getToggleControlsShortDescription(controlsVisible))
                .setSelected(controlsVisible);

        syncPresentationMode();
        update();
    }

    public final boolean areControlsVisible() {
        final Component cc = getControlsComponent();
        return cc != null && cc.isVisible();
    }

    protected final void setControlsVisibleInternal(boolean visible) {
        final Component cc = getControlsComponent();
        if (cc == null)
            return;

        cc.setVisible(visible);
        onControlsVisibilityChanged(visible);
    }

    public final void setControlsVisible(boolean visible) {
        if (visible == areControlsVisible())
            return;

        setControlsVisibleInternal(visible);
    }

    public final boolean toggleControlsVisibility() {
        final boolean newState = !areControlsVisible();
        setControlsVisibleInternal(newState);
        return newState;
    }


    protected void onPresentationModeEnabledChanged(boolean presenting) {
        uia(ActionInfo.TOGGLE_PRESENTATION_MODE)
                .setName(R.getTogglePresentationModeText(presenting))
                .setShortDescription(R.getTogglePresentationModeShortDescription(presenting))
                .setSelected(presenting);
    }

    protected final void syncPresentationMode() {
        onPresentationModeEnabledChanged(isPresenting());
    }

    public final boolean isPresenting() {
        return isFullscreen() && !areControlsVisible();
    }

    protected final void setPresentationModeEnabledInternal(boolean present) {
        setMenuBarVisible(!present);
        setControlsVisible(!present);
        setFullscreen(present);

        onPresentationModeEnabledChanged(present);
    }

    public final void setPresentationModeEnabled(boolean present) {
        if (present == isPresenting())
            return;

        setPresentationModeEnabledInternal(present);
    }

    public final boolean togglePresentationMode() {
        final boolean newState = !isPresenting();
        setPresentationModeEnabledInternal(newState);
        return newState;
    }




    /* Actions */

    @NotNull
    protected final JMenu createViewMenu(@Nullable JMenu dest) {
        if (dest == null) {
            dest = new JMenu("View");
        }

        dest.add(uia(ActionInfo.TOGGLE_MENUBAR));
        dest.add(uia(ActionInfo.TOGGLE_CONTROLS));
        dest.addSeparator();
        dest.add(uia(ActionInfo.TOGGLE_FULLSCREEN));
        dest.add(uia(ActionInfo.TOGGLE_PRESENTATION_MODE));
        return dest;
    }

    @NotNull
    protected final JMenu createViewMenu() {
        return createViewMenu(null);
    }



    protected final void setupActionKeyBindings(@NotNull Collection<InputMap> inputMaps, @NotNull ActionMap actionMap, @Nullable Collection<ActionInfo> actions) {
        if (CollectionUtil.isEmpty(actions)) {
            actions =  ActionInfo.sharedValues();  // all
        }

        actions.forEach(info -> {
            if (info.keyStroke != null) {
                inputMaps.forEach(im -> im.put(info.keyStroke, info));
            }

            actionMap.put(info, uia(info));
        });
    }

    protected final void setupActionKeyBindings(@NotNull JComponent component, @Nullable Collection<ActionInfo> actions, int @NotNull ... inputMapConditions) {
        final List<InputMap> maps = new LinkedList<>();
        for (int i: inputMapConditions) {
            maps.add(component.getInputMap(i));
        }

        setupActionKeyBindings(maps, component.getActionMap(), actions);
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
    public boolean onAction(@NotNull UiAction action, @NotNull ActionEvent e) {
        switch (action.info) {
            case TOGGLE_FULLSCREEN -> toggleFullscreen();
            case TOGGLE_CONTROLS -> toggleControlsVisibility();
            case TOGGLE_MENUBAR -> toggleMenuBarVisible();
            case TOGGLE_PRESENTATION_MODE -> togglePresentationMode();
            default -> {
                return false;
            }
        }

        return true;
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
    public void onAppearancePreferencesChanged() {

    }

    @Override
    public void onFTIntegrationIntervalCountChanged(int fourierTransformSimpson13NDefault) {

    }

    @Override
    public void onConfigPreferencesChanged() {

    }

    @Override
    public void onLogPreferencesChanged() {

    }

    @Override
    public void onSoundPreferencesChanged() {

    }

    @Override
    public void onOtherPreferencesChanged() {

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
