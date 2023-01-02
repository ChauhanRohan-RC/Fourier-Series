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

public class MousePathUi extends BaseFrame implements MousePathPanel.Listener {

    public static final String TAG = "MousePathUi";

    public static final Dimension MINIMUM_SIZE = new Dimension(400, 400);

    public static final int INITIAL_WIDTH = SCREEN_SIZE.width - 200;
    public static final int INITIAL_HEIGHT = SCREEN_SIZE.height - 200;

    private static final float INITIAL_DOMAIN_ANIMATION_DURATION_SCALE = 0.25f;  // 4x faster

    private static final List<ActionInfo> ACTIONS = List.of(
            ActionInfo.TOGGLE_FULLSCREEN,
            ActionInfo.RESET_SCALE,
            ActionInfo.RESET_DRAG,
            ActionInfo.RESET_SCALE_DRAG
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

    private final JPanel controlPanel1;
    private final JPanel controlPanel2;
    private final JPanel controlPanel;

    public MousePathUi() {
        super();
        setTitle(Ui.TITLE_MOUSE_PATH_UI);

        panel = new MousePathPanel();

        /* Buttons and Actions */
        final JToggleButton pointsButton = new JToggleButton(panel.getJoinPointsAction());
        configureImageButton(pointsButton);

        final JButton clearButton = new JButton(panel.getClearAction());
        configureImageButton(clearButton);

        final JToggleButton eraseButton = new JToggleButton(panel.getEraseAction());
        configureImageButton(eraseButton);

        final JButton removeLastButton = new JButton(panel.getRemoveLastAction());
        configureImageButton(removeLastButton);

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

        /* Layout */
        controlPanel1 = new JPanel();
        controlPanel1.setOpaque(false);
        controlPanel1.setLayout(new BoxLayout(controlPanel1, BoxLayout.X_AXIS));
        controlPanel1.add(pointsButton);
        controlPanel1.add(clearButton);
        controlPanel1.add(eraseButton);
        controlPanel1.add(removeLastButton);
        controlPanel1.add(doneButton);

        controlPanel2 = new JPanel();
        controlPanel2.setOpaque(false);
        controlPanel2.setLayout(new BoxLayout(controlPanel2, BoxLayout.X_AXIS));
        controlPanel2.add(fullscreenButton);
        controlPanel2.add(resetScaleButton);
        controlPanel2.add(resetDragButton);

        controlPanel = new JPanel();
        controlPanel.setOpaque(false);
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
        controlPanel.add(controlPanel2);
        controlPanel.add(Box.createHorizontalGlue());
        controlPanel.add(controlPanel1);

//        overlayPanel.setLayout(new BoxLayout(overlayPanel, BoxLayout.Y_AXIS));
//        overlayPanel.add(controlPanel1);
//        overlayPanel.add(Box.createVerticalStrut(4));
//        overlayPanel.add(controlPanel2);

        final JPanel root = new JPanel() {
            @Override
            public boolean isOptimizedDrawingEnabled() {
                return false;
            }
        };

        root.setLayout(new OverlayLayout(root));

        final JPanel overlayWrapper = new JPanel();
        overlayWrapper.setOpaque(false);
        overlayWrapper.setLayout(new BorderLayout());
        overlayWrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        overlayWrapper.add(controlPanel, BorderLayout.SOUTH);
        root.add(overlayWrapper);
        root.add(panel);

        // Layout
        setBounds(Ui.windowBoundsCenterScreen(INITIAL_WIDTH, INITIAL_HEIGHT));
        setMinimumSize(MINIMUM_SIZE.getSize());
        add(root);

        // Launch
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
            showErrorMessageDialog("Failed to create Path Function", "Parse failed");
        }
    }


    public void syncScaleAndDrag() {
        final double scale = panel.getScale();
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
    public void windowClosing(WindowEvent e) {
        super.windowClosing(e);
        MusicPlayer.getSingleton().requestPause(token);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
    }

    @Override
    public void onAction(@NotNull UiAction action, @NotNull ActionEvent e) {
        switch (action.info) {
            case TOGGLE_FULLSCREEN -> toggleFullscreen();
            case RESET_SCALE -> panel.resetScale(true);
            case RESET_DRAG -> panel.resetDrag(true);
            case RESET_SCALE_DRAG -> panel.resetScaleAndDrag();
        }
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
