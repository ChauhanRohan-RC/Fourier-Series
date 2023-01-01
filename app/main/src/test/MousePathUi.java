package test;

import live.WeakListeners;
import org.jetbrains.annotations.NotNull;
import ui.action.ActionInfo;
import ui.action.UiAction;
import ui.frames.BaseFrame;
import ui.util.Ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.Path2D;
import java.util.List;

public class MousePathUi extends BaseFrame {

    public static final Dimension MINIMUM_SIZE = new Dimension(400, 400);

    public static final int INITIAL_WIDTH = SCREEN_SIZE.width - 200;
    public static final int INITIAL_HEIGHT = SCREEN_SIZE.height - 200;

    private static final List<ActionInfo> ACTIONS = List.of(
            ActionInfo.TOGGLE_FULLSCREEN,
            ActionInfo.CLEAR_CANVAS,
            ActionInfo.ENTER
    );

    public interface Callback {
        void onMousePathUiFinished(@NotNull List<Path2D> paths);
    }


    private final MousePathPanel panel;
    private final WeakListeners<Callback> callbacks = new WeakListeners<>();

    public MousePathUi() {
        super();
        setTitle(Ui.TITLE_MOUSE_PATH_UI);

        panel = new MousePathPanel();
        panel.addMouseListener(this);

        // Layout
        setBounds(Ui.windowBoundsCenterScreen(INITIAL_WIDTH, INITIAL_HEIGHT));
        setMinimumSize(MINIMUM_SIZE.getSize());

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);

        // Launch
        setupActionKeyBindings(getRootPane(), ACTIONS, JComponent.WHEN_IN_FOCUSED_WINDOW, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        setResizable(true);
        setFocusable(true);
        setVisible(true);
    }

    public void addCallback(@NotNull Callback callback) {
        callbacks.addListener(callback);
    }

    public void removeCallback(@NotNull Callback callback) {
        callbacks.removeListener(callback);
    }


    private void onEnter() {
        final List<Path2D> paths = panel.getPaths(true);
        final String msg = !paths.isEmpty() ? "Do you want to process this drawing?": "Canvas is clear. Do you want to exit?";

        final int op = JOptionPane.showConfirmDialog(this, msg, "Finish", JOptionPane.OK_CANCEL_OPTION);
        if (op == JOptionPane.OK_OPTION) {
            Ui.close(this);

            callbacks.dispatchOnMainThread(c -> c.onMousePathUiFinished(paths));
        }
    }


    @Override
    public void windowClosing(WindowEvent e) {
        super.windowClosing(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
    }

    @Override
    public void onAction(@NotNull UiAction action, @NotNull ActionEvent e) {
        switch (action.info) {
            case TOGGLE_FULLSCREEN -> toggleFullscreen();
            case CLEAR_CANVAS -> panel.clear();
            case ENTER -> onEnter();
        }
    }
}
