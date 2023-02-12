package ui.frames;

import action.BaseAction;
import app.Colors;
import app.R;
import async.Async;
import function.path.PathFunctionMerger;
import misc.Log;
import models.Size;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import provider.FunctionType;
import ui.MusicPlayer;
import ui.action.ActionInfo;
import ui.action.UiAction;
import ui.panels.FunctionExportPanel;
import ui.panels.MousePathPanel;
import ui.util.Ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.GeneralPath;
import java.util.List;
import java.util.StringJoiner;

public class MousePathUi extends BaseFrame implements MousePathPanel.Listener {

    public static final String TAG = "MousePathUi";

    public static final Dimension MINIMUM_SIZE = new Dimension(600, 400);

    public static final int INITIAL_WIDTH = SCREEN_SIZE.width - 200;
    public static final int INITIAL_HEIGHT = SCREEN_SIZE.height - 200;

    private static final float INITIAL_DOMAIN_ANIMATION_DURATION_SCALE = 0.1f;  // 10x faster

    private static final List<ActionInfo> ACTIONS = List.of(
            ActionInfo.TOGGLE_FULLSCREEN,
            ActionInfo.TOGGLE_MENUBAR,
            ActionInfo.TOGGLE_CONTROLS,
            ActionInfo.RESET_SCALE,
            ActionInfo.RESET_DRAG,
            ActionInfo.RESET_SCALE_DRAG,
            ActionInfo.UNDO,
            ActionInfo.REDO,
            ActionInfo.INVERT_X,
            ActionInfo.INVERT_Y,
            ActionInfo.TOGGLE_POINTS_JOIN
    );


    private static final Color BUTTON_BG_HOVER = new Color(60, 60, 60, 255);
    private static final Color BUTTON_BG_SELECTED = new Color(26, 26, 26, 255);
    private static final int BUTTON_IPAD = 3;

    private static void configureImgButtonOnSelect(@NotNull AbstractButton button, boolean selected) {
        button.setBackground(selected? BUTTON_BG_SELECTED: Colors.TRANSPARENT);
        button.setContentAreaFilled(selected);
    }

    private static void configureImgButtonOnSelect(@NotNull AbstractButton button) {
        configureImgButtonOnSelect(button, button.isSelected());
    }

    private static void configureImageButton(@NotNull AbstractButton button) {
        button.setHideActionText(true);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(BUTTON_IPAD, BUTTON_IPAD, BUTTON_IPAD, BUTTON_IPAD));

        button.addPropertyChangeListener(evt -> {
            final String pn = evt.getPropertyName();

            if (Action.SELECTED_KEY.equals(pn) || "enabled".equals(pn)) {
                configureImgButtonOnSelect(button);
            }
        });

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(BUTTON_BG_HOVER);
                    button.setContentAreaFilled(true);
                } else {
                    button.setContentAreaFilled(false);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                configureImgButtonOnSelect(button);
            }
        });


        configureImgButtonOnSelect(button);     // sync
    }




    private final Object token = new Object();

    private final MousePathPanel panel;
    private final DoneAction doneAction;

    /* Menu */
    private final JMenuBar menuBar;

    private final JPanel rootPanel;

    private final JLabel statusLabel;
    private final JPanel overlayPanel;
    private final JPanel controlPanel;


    public MousePathUi() {
        super();
        setTitle(R.TITLE_MOUSE_PATH_UI);

        panel = new MousePathPanel();

        /* Buttons and Actions */
        final JToggleButton pointsJoinButton = new JToggleButton(panel.getJoinPointsAction());
        configureImageButton(pointsJoinButton);

        final JButton clearButton = new JButton(panel.getClearAction());
        configureImageButton(clearButton);

        final JToggleButton eraseButton = new JToggleButton(panel.getEraseAction());
        configureImageButton(eraseButton);

        final JButton undoButton = new JButton(panel.getUndoAction());
        configureImageButton(undoButton);

        final JButton redoButton = new JButton(panel.getRedoAction());
        configureImageButton(redoButton);

        doneAction = new DoneAction();
        final JButton doneButton = new JButton(doneAction);
        configureImageButton(doneButton);

        final BaseAction fsAction = uia(ActionInfo.TOGGLE_FULLSCREEN)
                .setLargeIconOnSelect(false, R.createIcon(R.IMG_MAXIMISE_LIGHT_64, 21))
                .setLargeIconOnSelect(true, R.createIcon(R.IMG_MINIMISE_ACCENT_64, 21));
        final JToggleButton fullscreenButton = new JToggleButton(fsAction);
        configureImageButton(fullscreenButton);

        final BaseAction resetScaleAction = uia(ActionInfo.RESET_SCALE)
                .setLargeIcon(R.createIcon(R.IMG_COLLAPSE_LIGHT_64, 21));
        final JButton resetScaleButton = new JButton(resetScaleAction);
        configureImageButton(resetScaleButton);

        final BaseAction resetDragAction = uia(ActionInfo.RESET_DRAG)
                .setLargeIcon(R.createIcon(R.IMG_CENTER_FOCUS_LIGHT_64, 21));
        final JButton resetDragButton = new JButton(resetDragAction);
        configureImageButton(resetDragButton);

        // todo large icons
        final BaseAction invertXAction = uia(ActionInfo.INVERT_X)
                .setSelected(panel.isXInverted());

        final BaseAction invertYAction = uia(ActionInfo.INVERT_Y)
                .setSelected(panel.isYInverted());

        statusLabel = new JLabel();
        statusLabel.setOpaque(false);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        statusLabel.setForeground(Color.WHITE);

        /* Layout */
        // 1. Controls
        controlPanel = new JPanel();
        controlPanel.setOpaque(false);
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
        controlPanel.add(fullscreenButton);
        controlPanel.add(resetScaleButton);
        controlPanel.add(resetDragButton);
        controlPanel.add(statusLabel);
        controlPanel.add(Box.createHorizontalGlue());
        controlPanel.add(undoButton);
        controlPanel.add(clearButton);
        controlPanel.add(eraseButton);
        controlPanel.add(redoButton);
        controlPanel.add(Box.createHorizontalGlue());
        controlPanel.add(pointsJoinButton);
        controlPanel.add(doneButton);

        // 2. Overlay
        overlayPanel = new JPanel();
        overlayPanel.setOpaque(false);
        overlayPanel.setLayout(new BorderLayout());
        overlayPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        overlayPanel.add(controlPanel, BorderLayout.SOUTH);

        // 3. Root (Overlay + Main)
        rootPanel = new JPanel() {
            @Override
            public boolean isOptimizedDrawingEnabled() {
                return false;
            }
        };

        rootPanel.setLayout(new OverlayLayout(rootPanel));
        rootPanel.add(overlayPanel);
        rootPanel.add(panel);

        // Layout
        setBounds(Ui.windowBoundsCenterScreen(INITIAL_WIDTH, INITIAL_HEIGHT));
        setMinimumSize(MINIMUM_SIZE.getSize());
        add(rootPanel);


        /* Menu */
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // View menu
        final JMenu viewMenu = new JMenu("View");
        viewMenu.add(new JCheckBoxMenuItem(invertXAction));
        viewMenu.add(new JCheckBoxMenuItem(invertYAction));
        viewMenu.addSeparator();
        createViewMenu(viewMenu);
        menuBar.add(viewMenu);

        // Music Menu
        menuBar.add(MusicPlayer.getSingleton().createPlaybackMenu());

        // Settings
        menuBar.add(Ui.createSettingsMenu(this));


        // Run
        setupActionKeyBindings(getRootPane(), ACTIONS, JComponent.WHEN_IN_FOCUSED_WINDOW, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        panel.addMouseListener(this);
        panel.addListener(this);
        syncScaleAndDrag();

        final Image appIcon = R.createAppIconColorful();
        if (appIcon != null) {
            setIconImage(appIcon);
        }

        setResizable(true);
        setFocusable(true);
        setVisible(true);
    }


    @Override
    public @Nullable Component getControlsComponent() {
        return controlPanel;
    }

    @NotNull
    public MousePathPanel getPathPanel() {
        return panel;
    }

    private void onDone() {
        if (panel.isClear()) {
            showInfoMessageDialog("Canvas is clear. Get started by drawing something...", "Export");
            return;
        }

        final GeneralPath path = panel.getFinalPath(true, false);
        try {
            final PathFunctionMerger func = PathFunctionMerger.create(path);
            func.setDomainAnimationDurationScale(INITIAL_DOMAIN_ANIMATION_DURATION_SCALE);

            final FunctionExportPanel expPanel = new FunctionExportPanel(this);
            expPanel.setCloseOnDoneSelected(true);
            expPanel.setShowCloseOnDone(true);

            final FunctionExportPanel.ExportMode exportMode = expPanel.showDialog(func, FunctionType.EXTERNAL_PATH, "Custom Drawing " + FunctionExportPanel.nextFunctionNumber());
            if (exportMode != null && expPanel.isCloseOnDoneSelected()) {
                Async.uiPost(() -> Ui.close(this), 10);
            }
        } catch (Throwable e) {
            Log.e(TAG, "Failed to parse PathFunction", e);
            showErrorMessageDialog(String.format("Failed to create Path Function\nError: %s -> %s", e.getClass().getSimpleName(), e.getMessage()), "Parse failed");
        }
    }



    @NotNull
    private static String createScaleDragDisplayStatus(double scale, @Nullable Size drag) {
        final StringJoiner sj = new StringJoiner("  |  ");

        // Scale
        sj.add(String.format("%.1fx", scale));

        // Drag
        if (drag != null) {
            final int tx = (int) drag.width, ty = (int) drag.height;

            if (tx != 0 || ty != 0) {
                sj.add(String.format("Tx: %d", tx)).add(String.format("Ty: %d", ty));
            }
        }

        return sj.toString();
    }


    public void syncScaleAndDrag() {
        final double scale = panel.getScale();
        final Size drag = panel.getDrag();
        statusLabel.setText(createScaleDragDisplayStatus(scale, drag));

//        scaleText.setText(R.getScaleText(scale));
        uia(ActionInfo.SCALE_UP).setEnabled(scale < panel.getMaximumScale());
        uia(ActionInfo.SCALE_DOWN).setEnabled(scale > panel.getMinimumScale());

        final boolean hasScale = panel.hasScale(), hasDrag = panel.hasDrag();

        uia(ActionInfo.RESET_SCALE).setEnabled(hasScale);
        uia(ActionInfo.RESET_DRAG).setEnabled(hasDrag);
        uia(ActionInfo.RESET_SCALE_DRAG).setEnabled(hasScale || hasDrag);
        update();
    }


    @Override
    public void onPointsJoiningEnabledChanged(@NotNull MousePathPanel panel, boolean enabled) {

    }

    @Override
    public void onInvertXChanged(@NotNull MousePathPanel panel, boolean invertX) {
        uia(ActionInfo.INVERT_X).setSelected(invertX);
    }

    @Override
    public void onInvertYChanged(@NotNull MousePathPanel panel, boolean invertY) {
        uia(ActionInfo.INVERT_Y).setSelected(invertY);
    }

    @Override
    public void onScaleChanged(@NotNull MousePathPanel panel, double scale) {
        syncScaleAndDrag();
    }

    @Override
    public void onDragChanged(@NotNull MousePathPanel panel, @Nullable Size drag) {
        syncScaleAndDrag();
    }

    @Override
    public void onEraseModeEnabledChanged(@NotNull MousePathPanel panel, boolean enabled) {
    }

    @Override
    public void onPathCountChanged(@NotNull MousePathPanel panel, int count) {
        doneAction.sync();

        MusicPlayer.getSingleton().requestPlayPause(token, count > 0);
    }

    @Override
    public void onUndoStackChanged(@NotNull MousePathPanel panel) {

    }

    @Override
    public void onRedoStackChanged(@NotNull MousePathPanel panel) {

    }


    @Override
    public void windowClosing(WindowEvent e) {
        super.windowClosing(e);
        MusicPlayer.getSingleton().requestPause(token);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
    }

    @Override
    public boolean onAction(@NotNull UiAction action, @NotNull ActionEvent e) {
        if (super.onAction(action, e))
            return true;

        switch (action.info) {
            case RESET_SCALE -> panel.resetScale(true);
            case RESET_DRAG -> panel.resetDrag(true);
            case RESET_SCALE_DRAG -> panel.resetScaleAndDrag();
            case UNDO -> panel.undo();
            case REDO -> panel.redo();
            case INVERT_X -> panel.toggleInvertX();
            case INVERT_Y -> panel.toggleInvertY();
            case TOGGLE_POINTS_JOIN -> panel.toggleJoinPoints();
            default -> {
                return false;
            }
        }

        return true;
    }



    private class DoneAction extends BaseAction {

        private DoneAction() {
            this.setName("Done");
            this.setShortDescription("Finish Drawing");
            this.setLargeIcon(R.createLargeIcon(R.IMG_CHECK_CIRCLE_ACCENT_64));
            this.sync();
        }

        public void sync() {
            super.sync();
            this.setEnabled(!panel.isClear());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            onDone();
        }
    }
}
