package ui;

import animation.animator.AbstractAnimator;
import app.App;
import app.R;
import function.definition.ComplexDomainFunctionI;
import models.Size;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import provider.*;
import rotor.RotorStateManager;
import rotor.StandardRotorStateManager;
import rotor.frequency.RotorFrequencyProviderI;
import ui.action.ActionInfo;
import ui.action.UiAction;
import ui.util.Ui;
import util.Format;
import util.Log;
import util.async.Canceller;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.List;

public class FourierUi extends JFrame implements RotorStateManager.Listener, FourierSeriesPanel.PanelListener, Ui {

    public static final String TAG = "FourierUi";

    public static final boolean DEFAULT_FULLSCREEN = false;
    public static final boolean DEFAULT_CONTROLS_VISIBLE = true;
    public static final boolean DEFAULT_MENUBAR_VISIBLE = true;
    public static final boolean AUTO_PLAY_ON_ROTOR_STATE_MANAGER_CHANGE = true;

    public static final Dimension MINIMUM_SIZE = new Dimension(400, 400);

    public static final int INITIAL_WIDTH = SCREEN_SIZE.width - 200;
    public static final int INITIAL_HEIGHT = SCREEN_SIZE.height - 200;

    @NotNull
    private final Timer looper;
    final FourierSeriesPanel fsPanel;
    private boolean mFullscreen = DEFAULT_FULLSCREEN;

    final JPanel controlPanel;
    final JScrollPane controlScrollPane;
    final JLabel rotorCountText;
    final JSlider rotorCountSlider;
    final JLabel speedText;
    final JSlider speedSlider;

    @NotNull
    final FunctionProviders functionProviders;
    final JLabel functionLabel;
    final JComboBox<FunctionProviderI> functionComboBox;

    final JButton ftUiButton;
    final JButton playButton;
    final JButton resetButton;
    final JButton resetScaleAndDragButton;
    final JCheckBox waveCheck;
    final JCheckBox invertXCheck;
    final JCheckBox invertYCheck;
    final JCheckBox graphInCenterCheck;
    final JCheckBox pointsJoinCheck;
    final JCheckBox hueCycleCheckBox;
    final JCheckBox autoTrackInCenterCheckBox;

    final JLabel endBehaviourLabel;
    final JComboBox<AbstractAnimator.RepeatMode> repeatModeComboBox;

    final JLabel scaleText;
    final JButton scaleIncButton;
    final JButton scaleDecButton;

    final JButton leftButton;
    final JButton rightButton;
    final JButton upButton;
    final JButton downButton;

    final JButton toggleControlsButton;
    private final JComponent[] funcDependentComps;              // Ui components that depend on function

    /* Menu */
    final JMenuBar menuBar;

    final JMenu menuFunctions;
    final JMenu menuPathFunctions;
    final JMenu menuPrograms;
    final JMenu menuTransform;
    final JMenu menuRotorStates;
    final JMenu menuFunctionState;
    final JMenu menuView;

    @NotNull
    private final FunctionProvidersListener mFunctionProvidersListener = new FunctionProvidersListener();

    @NotNull
    private final WindowHandler mWindowHandler = new WindowHandler();
    @NotNull
    private final ActionHandler mActionHandler = new ActionHandler();
    @NotNull
    private final MouseHandler mMouseHandler = new MouseHandler();

    public FourierUi() {
        this(null, 0 /* First */);
    }

    public FourierUi(@Nullable String title, int initialProviderIndex) {
        super(title == null || title.isEmpty()? MAIN_TITLE : title);

        looper = Ui.createLooper(null);

        fsPanel = new FourierSeriesPanel(new RotorStateManager.NoOp());
        looper.addActionListener(Ui.actionListener(fsPanel));

        // Function
        functionProviders = new FunctionProviders(Providers.ALL_INTERNAL_FUNCTIONS, -1);

        functionLabel = new JLabel(R.getFunctionProviderLabelText());
        functionLabel.setToolTipText(R.getFunctionProviderShortDescription());
        functionComboBox = new JComboBox<>(functionProviders);
        functionComboBox.setToolTipText(R.getFunctionProviderShortDescription());

        ftUiButton = new JButton(uia(ActionInfo.SHOW_FT_UI));

        pointsJoinCheck = new JCheckBox(uia(ActionInfo.TOGGLE_POINTS_JOIN).setSelected(fsPanel.isPointsJoiningEnabled()));
        hueCycleCheckBox = new JCheckBox(uia(ActionInfo.TOGGLE_HUE_CYCLE).setSelected(fsPanel.isHueCycleEnabled()));

        // Rotor Count
        final int rotorCount = fsPanel.getConstrainedRotorCount();
        rotorCountText = new JLabel(R.getRotorCountText(rotorCount));

        rotorCountSlider = new JSlider(SwingConstants.HORIZONTAL, FourierSeriesPanel.ROTOR_COUNT_MIN, FourierSeriesPanel.ROTOR_COUNT_MAX, rotorCount);
        rotorCountSlider.setToolTipText(R.getRotorCountSliderShortDescription());
        rotorCountSlider.setLabelTable(rotorCountSlider.createStandardLabels(FourierSeriesPanel.ROTOR_COUNT_SLIDER_LABEL_INCREMENT, FourierSeriesPanel.ROTOR_COUNT_MIN));
        rotorCountSlider.setPaintLabels(true);

        // Speed
        final int speed = fsPanel.getDomainAnimationSpeedPercent();
        speedText = new JLabel(R.getSpeedPercentText(speed));

        speedSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, speed);
        speedSlider.setToolTipText(R.getSpeedSliderShortDescription());
        speedSlider.setLabelTable(speedSlider.createStandardLabels(25, 0));
        speedSlider.setPaintLabels(true);

        // Play/Pause Toggle
        final boolean playing = fsPanel.isPlaying();
        final Action togglePlayAction = uia(ActionInfo.TOGGLE_PLAY_PAUSE)
                .setName(R.getPlayPauseText(playing))
                .setShortDescription(R.getPlayPauseShortDescription(playing))
                .setSelected(!playing);

        playButton = new JButton(togglePlayAction);

        // Reset
        resetButton = new JButton(uia(ActionInfo.RESET_MAIN));
        resetScaleAndDragButton = new JButton(uia(ActionInfo.RESET_SCALE_DRAG));

        // Ops
        final boolean drawingWave = fsPanel.isDrawingWave();
        final Action waveUia = uia(ActionInfo.TOGGLE_WAVE)
                .setName(R.getWaveToggleText(drawingWave))
                .setShortDescription(R.getWaveToggleShortDescription(drawingWave))
                .setSelected(drawingWave);

        waveCheck = new JCheckBox(waveUia);

        final Action graphInCenterUia = uia(ActionInfo.TOGGLE_GRAPH_CENTER)
                .setSelected(fsPanel.isGraphCenterEnabled());
        graphInCenterUia.setEnabled(!drawingWave);
        graphInCenterCheck = new JCheckBox(graphInCenterUia);

        invertXCheck = new JCheckBox(uia(ActionInfo.INVERT_X).setSelected(fsPanel.isXInverted()));
        invertYCheck = new JCheckBox(uia(ActionInfo.INVERT_Y).setSelected(fsPanel.isYInverted()));

        final Action autoTrackUia = uia(ActionInfo.TOGGLE_AUTO_TRACK)
                .setSelected(fsPanel.isAutoTrackInCenterEnabled());
        autoTrackUia.setEnabled(fsPanel.isGraphingInCenter());
        autoTrackInCenterCheckBox = new JCheckBox(autoTrackUia);

        endBehaviourLabel = new JLabel(R.getRepeatModeLabelText());
        endBehaviourLabel.setToolTipText(R.getRepeatModeShortDescription());

        repeatModeComboBox = new JComboBox<>(AbstractAnimator.RepeatMode.values());
        repeatModeComboBox.setSelectedIndex(fsPanel.getRepeatMode().ordinal());
        repeatModeComboBox.setToolTipText(R.getRepeatModeShortDescription());

        // Transforms
        scaleText = new JLabel(R.getScaleText(fsPanel.getScale()));
        scaleIncButton = new JButton(uia(ActionInfo.SCALE_UP));
        scaleDecButton = new JButton(uia(ActionInfo.SCALE_DOWN));

        leftButton = new JButton(uia(ActionInfo.DRAG_LEFT));
        rightButton = new JButton(uia(ActionInfo.DRAG_RIGHT));
        upButton = new JButton(uia(ActionInfo.DRAG_UP));
        downButton = new JButton(uia(ActionInfo.DRAG_DOWN));

        // Toggle Controls
        final Action controlUia = uia(ActionInfo.TOGGLE_CONTROLS)
                .setName(R.getToggleControlsText(DEFAULT_CONTROLS_VISIBLE))
                .setShortDescription(R.getToggleControlsShortDescription(DEFAULT_CONTROLS_VISIBLE))
                .setSelected(DEFAULT_CONTROLS_VISIBLE);

        toggleControlsButton = new JButton(controlUia);

        /* Menu */

        menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // Functions Menu
        menuFunctions = new JMenu("Functions");
        menuBar.add(menuFunctions);

        menuPrograms = new JMenu("Program");
        menuFunctions.add(menuPrograms);
        menuPrograms.add(uia(ActionInfo.LOAD_EXTERNAL_PROGRAMMATIC_FUNCTION));
        menuPrograms.addSeparator();
        menuPrograms.add(uia(ActionInfo.CLEAR_INTERNAL_PROGRAMMATIC_FUNCTIONS));
        menuPrograms.add(uia(ActionInfo.CLEAR_EXTERNAL_PROGRAMMATIC_FUNCTIONS));
        menuPrograms.addSeparator();
        menuPrograms.add(uia(ActionInfo.RESET_PROGRAMMATIC_FUNCTIONS));

        // Path Functions Menu
        menuPathFunctions = new JMenu("Path");
        menuFunctions.add(menuPathFunctions);
        menuPathFunctions.add(uia(ActionInfo.LOAD_EXTERNAL_PATH_FUNCTIONS));
        menuPathFunctions.add(uia(ActionInfo.LOAD_EXTERNAL_PATH_FUNCTIONS_FROM_DIR));
        menuPathFunctions.addSeparator();
        menuPathFunctions.add(uia(ActionInfo.CLEAR_INTERNAL_PATH_FUNCTIONS));
        menuPathFunctions.add(uia(ActionInfo.CLEAR_EXTERNAL_PATH_FUNCTIONS));
        menuPathFunctions.addSeparator();
        menuPathFunctions.add(uia(ActionInfo.RESET_PATH_FUNCTIONS));

        // Rotor States Menu
        menuRotorStates = Ui.createRotorStatesMenu(this::uia);
        menuBar.add(menuRotorStates);

        // Function State menu
        menuFunctionState = new JMenu("Function State");
        menuBar.add(menuFunctionState);
        menuFunctionState.add(uia(ActionInfo.SAVE_FUNCTION_STATE_TO_FILE));
        menuFunctionState.add(uia(ActionInfo.LOAD_FUNCTION_STATE_FROM_FILE));
        menuFunctionState.addSeparator();
        menuFunctionState.add(uia(ActionInfo.CLEAR_FUNCTIONS_WITHOUT_DEFINITION));

        // Transform Menu
        menuTransform = new JMenu("Transform");
        menuBar.add(menuTransform);
        menuTransform.add(uia(ActionInfo.CONFIGURE_ROTOR_FREQUENCY_PROVIDER));
        menuTransform.add(uia(ActionInfo.SHOW_FT_UI));

        // View menu
        menuView = new JMenu("View");
        menuBar.add(menuView);
        menuView.add(uia(ActionInfo.TOGGLE_MENUBAR));
        menuView.add(uia(ActionInfo.TOGGLE_CONTROLS));
        menuView.addSeparator();
        menuView.add(uia(ActionInfo.TOGGLE_FULLSCREEN));

        // Theme
        menuBar.add(Ui.createThemeSelectorMenu());

        // Ui components that depend on function
        funcDependentComps = new JComponent[] {
                rotorCountSlider,
                speedSlider,
        };




        // Layout
//        controlPanel = new JPanel();
//        controlPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
////        controlPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 10, 5));
////        controlPanel.setLayout(new GridLayout(6, 1, 4, 4));     // todo rows
//
//        final MigLayout mig = new MigLayout("gapy 5", "", "");
//        controlPanel.setLayout(mig);
//
//        final JPanel actionsPanel = new JPanel(new GridLayout(2, 3, 4, 4));
//
//        actionsPanel.add(playToggle);
//        actionsPanel.add(resetButton);
//        actionsPanel.add(waveToggle);
//        actionsPanel.add(invertXCheck);
//        actionsPanel.add(invertYCheck);
//        actionsPanel.add(graphInCenterCheck);
//        controlPanel.add(actionsPanel, "spanx, center");
//
////        final JPanel slidersPanel = new JPanel(new GridLayout(2, 2, 5, 2));
////        rotorCountText.setHorizontalAlignment(SwingConstants.CENTER);
////        speedText.setHorizontalAlignment(SwingConstants.CENTER);
////
////        slidersPanel.add(rotorCountText);
////        slidersPanel.add(speedText);
////        slidersPanel.add(rotorCountSlider);
////        slidersPanel.add(speedSlider);
////        controlPanel.add(slidersPanel);
//
//        final JPanel rotorCountSliderPanel = new JPanel(new GridLayout(2, 1, 4, 1));
//        rotorCountText.setHorizontalAlignment(SwingConstants.CENTER);
//        rotorCountSliderPanel.add(rotorCountText);
//        rotorCountSliderPanel.add(rotorCountSlider);
//        controlPanel.add(rotorCountSliderPanel, "spanx, growx, wrap");
//
//        final JPanel speedSliderPanel = new JPanel(new GridLayout(2, 1, 4, 1));
//        speedText.setHorizontalAlignment(SwingConstants.CENTER);
//        speedSliderPanel.add(speedText);
//        speedSliderPanel.add(speedSlider);
//        controlPanel.add(speedSliderPanel, "spanx, growx, wrap");
//
//        final JPanel scalePanel = new JPanel(new GridLayout(1, 2, 4, 2));
//        final JPanel scaleButtonsPanel = new JPanel(new GridLayout(1, 2, 3, 2));
//        scaleButtonsPanel.add(scaleIncButton);
//        scaleButtonsPanel.add(scaleDecButton);
//
//        scaleText.setHorizontalAlignment(SwingConstants.CENTER);
//        scalePanel.add(scaleText);
//        scalePanel.add(scaleButtonsPanel);
//        controlPanel.add(scalePanel, "spanx, center, wrap");
//
//        final JPanel navigationPanel = new JPanel(new GridLayout(2, 1, 3, 2));
//        final JPanel navigationPanelInternal = new JPanel(new GridLayout(1, 3, 3, 2));
//        upButton.setHorizontalAlignment(SwingConstants.CENTER);
//        navigationPanelInternal.add(leftButton);
//        navigationPanelInternal.add(downButton);
//        navigationPanelInternal.add(rightButton);
//        navigationPanel.add(upButton);
//        navigationPanel.add(navigationPanelInternal);
//        controlPanel.add(navigationPanel, "spanx, center, wrap");
//
//        controlPanel.add(resetScaleAndDragButton, "spanx, growx, wrap");
//
//        setBounds(Ui.windowBoundsCenterScreen(INITIAL_WIDTH, INITIAL_HEIGHT));
//        setMinimumSize(MINIMUM_SIZE);
//        setLayout(new BorderLayout(0, 0));
//        add(controlPanel, BorderLayout.EAST);
//        add(panel, BorderLayout.CENTER);


        controlPanel = new JPanel();
        controlPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 8, 5));
        controlScrollPane = new JScrollPane(controlPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        final JPanel funcPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 2));
        funcPanel.add(functionLabel);
        funcPanel.add(functionComboBox);

        final JPanel mainPanel = new JPanel(new GridLayout(2, 1, 2, 6));
        ftUiButton.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(funcPanel);
        mainPanel.add(ftUiButton);
        controlPanel.add(mainPanel);

        final JPanel funcOpsPanel = new JPanel(new GridLayout(2, 1, 2, 4));
        funcOpsPanel.add(pointsJoinCheck);
        funcOpsPanel.add(hueCycleCheckBox);
        controlPanel.add(funcOpsPanel);

        final JPanel buttonsPanel = new JPanel(new GridLayout(2, 1, 2, 4));
        buttonsPanel.add(playButton);
        buttonsPanel.add(resetButton);
        controlPanel.add(buttonsPanel);

        final JPanel endComboPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 2));
        endComboPanel.add(endBehaviourLabel);
        endComboPanel.add(repeatModeComboBox);

        final JPanel inversionPanel = new JPanel(new GridLayout(1, 2, 2, 4));
        inversionPanel.add(invertXCheck);
        inversionPanel.add(invertYCheck);

        final JPanel endInversionPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        endInversionPanel.add(endComboPanel);
        endInversionPanel.add(inversionPanel);
        controlPanel.add(endInversionPanel);

        final JPanel waveOpsPanel = new JPanel(new GridLayout(3, 1, 4, 1));
        waveOpsPanel.add(waveCheck);
        waveOpsPanel.add(graphInCenterCheck);
        waveOpsPanel.add(autoTrackInCenterCheckBox);
        controlPanel.add(waveOpsPanel);

        final JPanel slidersPanel = new JPanel(new GridLayout(2, 2, 5, 2));
        rotorCountText.setHorizontalAlignment(SwingConstants.CENTER);
        speedText.setHorizontalAlignment(SwingConstants.CENTER);

        slidersPanel.add(rotorCountText);
        slidersPanel.add(speedText);
        slidersPanel.add(rotorCountSlider);
        slidersPanel.add(speedSlider);
        controlPanel.add(slidersPanel);

        final JPanel scalePanel = new JPanel(new GridLayout(2, 1, 5, 2));
        final JPanel scaleButtonsPanel = new JPanel(new GridLayout(1, 2, 3, 2));
        scaleButtonsPanel.add(scaleIncButton);
        scaleButtonsPanel.add(scaleDecButton);

        scaleText.setHorizontalAlignment(SwingConstants.CENTER);
        scalePanel.add(scaleText);
        scalePanel.add(scaleButtonsPanel);
        controlPanel.add(scalePanel);

        final JPanel navigationPanel = new JPanel(new GridLayout(2, 1, 3, 4));
        final JPanel navigationPanelInternal = new JPanel(new GridLayout(1, 3, 3, 2));
        upButton.setHorizontalAlignment(SwingConstants.CENTER);
        navigationPanelInternal.add(leftButton);
        navigationPanelInternal.add(downButton);
        navigationPanelInternal.add(rightButton);
        navigationPanel.add(upButton);
        navigationPanel.add(navigationPanelInternal);
        controlPanel.add(navigationPanel);

        final JPanel otherButtonsPanel = new JPanel(new GridLayout(2, 1, 3, 2));
        otherButtonsPanel.add(resetScaleAndDragButton);
        otherButtonsPanel.add(toggleControlsButton);

        controlPanel.add(otherButtonsPanel);

        setBounds(Ui.windowBoundsCenterScreen(INITIAL_WIDTH, INITIAL_HEIGHT));
        setMinimumSize(MINIMUM_SIZE);
        setLayout(new BorderLayout(0, 0));
        add(controlScrollPane, BorderLayout.SOUTH);
        add(fsPanel, BorderLayout.CENTER);

        // Listeners
        fsPanel.ensurePanelListener(this);
        fsPanel.getRotorStateManager().ensureListener(this);

        rotorCountSlider.addChangeListener(ev -> {
            final int val = rotorCountSlider.getValue();
            if (ignoreAdjustingRotorCountValue() && rotorCountSlider.getValueIsAdjusting()) {
                updateRotorCountText(val);
            } else {
                setRotorCount(val);
            }
        });

        speedSlider.addChangeListener(ev -> {
            final int val = speedSlider.getValue();
            if (ignoreAdjustingSpeedValue() && speedSlider.getValueIsAdjusting()) {
                updateSpeedText(val);
            } else {
                setSpeedPercent(val);
            }
        });

        functionComboBox.addActionListener(e -> setFunctionProvider(functionComboBox.getSelectedIndex()));
        repeatModeComboBox.addActionListener(e -> setRepeatMode((AbstractAnimator.RepeatMode) repeatModeComboBox.getSelectedItem()));

        // Run
        functionProviders.addListDataListener(mFunctionProvidersListener);
        setFunctionProvider(initialProviderIndex);     // First function provider
        setupActionKeyBindings(getRootPane(), JComponent.WHEN_IN_FOCUSED_WINDOW, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        addMouseListener(mMouseHandler);
        addWindowListener(mWindowHandler);
        addWindowStateListener(mWindowHandler);
        addWindowFocusListener(mWindowHandler);

        fsPanel.addMouseListener(mMouseHandler);

        final Image appIcon = R.createAppIcon();
        if (appIcon != null) {
            setIconImage(appIcon);
        }

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);     // TODO
        setFocusable(true);
        setResizable(true);

        // Start Looper
        looper.start();
        setVisible(true);

        EventQueue.invokeLater(() -> {
            considerLoadDefaultExternalPathFunctionsOnStartAsync();

            updateOpsState();
            syncScaleAndDrag();
            setControlsVisibleInternal(DEFAULT_CONTROLS_VISIBLE);
            setMenuBarVisibleInternal(DEFAULT_MENUBAR_VISIBLE);
            setFullscreenInternal(mFullscreen);     // sync
            requestFocusInWindow();
            update();
            
            test();
        });
    }


    private void test() {
//        double step = 1;
//        double freq = 0.1;
//        int cycles = 4;
//
//        double[] samples = new double[(int) (cycles * 360 / freq)];
//        for (int i=0; i < samples.length; i++) {
//            samples[i] = Math.sin(Math.toRadians(i * step) * freq);
//        }
//
//        final DiscreteSignal signal = new DiscreteSignal(0, step, samples);
////        functionProviders.ensureAddSelect(new SimpleFunctionProvider(new FunctionMeta(FunctionType.INTERNAL_PROGRAM, "Test Discrete Signal"), signal));
//
//        Log.d(TAG, "FT at " + freq + ": " + ComplexUtil.fourierSeriesCoefficient(signal, freq));
    }

    @NotNull
    public FourierSeriesPanel getFourierSeriesPanel() {
        return fsPanel;
    }

    private void update() {
        revalidate();
        repaint();
    }

    @Override
    public JFrame getFrame() {
        return this;
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

    private void setFullscreenInternal(boolean fullscreen) {
        getGraphicsConfiguration().getDevice().setFullScreenWindow(fullscreen? FourierUi.this: null);
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

    protected void onControlsVisibilityChanged(boolean controlsVisible) {
        uia(ActionInfo.TOGGLE_CONTROLS)
                .setName(R.getToggleControlsText(controlsVisible))
                .setShortDescription(R.getToggleControlsShortDescription(controlsVisible))
                .setSelected(controlsVisible);

        update();
    }

    public final boolean areControlsVisible() {
        return controlScrollPane.isVisible();
    }

    private void setControlsVisibleInternal(boolean visible) {
        controlScrollPane.setVisible(visible);
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


    protected void onMenuBarVisibilityChanged(boolean visible) {
        uia(ActionInfo.TOGGLE_MENUBAR)
                .setName(R.getToggleMenuBarText(visible))
                .setShortDescription(R.getToggleMenuBarShortDescription(visible))
                .setSelected(visible);

        update();
    }

    public final boolean isMenuBarVisible() {
        return menuBar.isVisible();
    }

    private void setMenuBarVisibleInternal(boolean visible) {
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

    public void setPlay(boolean play) {
        fsPanel.setPlay(play);

        uia(ActionInfo.TOGGLE_PLAY_PAUSE)
                .setName(R.getPlayPauseText(play))
                .setShortDescription(R.getPlayPauseShortDescription(play))
                .setSelected(!play);
    }

    public boolean isPlaying() {
        return fsPanel.isPlaying();
    }

    public boolean togglePlay() {
        return fsPanel.togglePlay();
    }

    public void updateRotorCountText(int count) {
        rotorCountText.setText(R.getRotorCountText(count));
    }

    public void updateRotorCountUi(int count) {
        if (rotorCountSlider.getValue() != count) {
            rotorCountSlider.setValue(count);
        }

        updateRotorCountText(count);
    }

    public boolean ignoreAdjustingRotorCountValue() {
        return true;
    }

    public void setRotorCount(int count) {
        fsPanel.setRotorCount(count);
        updateRotorCountUi(fsPanel.getConstrainedRotorCount());
    }

    public boolean ignoreAdjustingSpeedValue() {
        return false;
//        return panel.isPointsJoiningEnabled();
    }

    public void updateSpeedText(int percent) {
        speedText.setText(R.getSpeedPercentText(percent));
    }

    public void updateSpeedUi(int percent) {
        if (speedSlider.getValue() != percent) {
            speedSlider.setValue(percent);
        }

        updateSpeedText(percent);
    }

    private void setSpeedPercent(int percent, boolean fromPanel) {
        if (!fromPanel) {
            fsPanel.setDomainAnimationSpeedPercent(percent);
            percent = fsPanel.getDomainAnimationSpeedPercent();
        }

        updateSpeedUi(percent);
    }

    public final void setSpeedPercent(int percent) {
        setSpeedPercent(percent, false);
    }

    private void updateOpsState() {
        uia(ActionInfo.TOGGLE_GRAPH_CENTER)
                .setEnabled(!fsPanel.isDrawingWave());
//        graphInCenterCheck.setVisible(graph);

        uia(ActionInfo.TOGGLE_AUTO_TRACK)
                .setEnabled(fsPanel.isGraphingInCenter());

//        autoTrackInCneterCheckBox.setVisible(graphInCenter);
    }

    public void setDrawingAsWave(boolean drawingAsWave) {
        fsPanel.setDrawAsWave(drawingAsWave);

        uia(ActionInfo.TOGGLE_WAVE)
                .setName(R.getWaveToggleText(drawingAsWave))
                .setShortDescription(R.getWaveToggleShortDescription(drawingAsWave))
                .setSelected(drawingAsWave);

        updateOpsState();
    }

    public boolean isDrawingAsWave() {
        return fsPanel.isDrawingWave();
    }

    public boolean toggleDrawASWave() {
        final boolean wave = !isDrawingAsWave();
        setDrawingAsWave(wave);
        return wave;
    }

    public void setInvertX(boolean invertX) {
        fsPanel.setInvertX(invertX);
        uia(ActionInfo.INVERT_X).setSelected(invertX);
    }

    public void setInvertY(boolean invertY) {
        fsPanel.setInvertY(invertY);
        uia(ActionInfo.INVERT_Y).setSelected(invertY);
    }

    public void setGraphInCenter(boolean graphInCenter) {
        fsPanel.setGraphInCenter(graphInCenter);
        uia(ActionInfo.TOGGLE_GRAPH_CENTER).setSelected(graphInCenter);
        updateOpsState();
    }

    public void setPointsJoiningEnabled(boolean enabled) {
        fsPanel.setJoinPointsEnabled(enabled);
        uia(ActionInfo.TOGGLE_POINTS_JOIN).setSelected(enabled);
    }

    public void setHueCycleEnabled(boolean enabled) {
        fsPanel.setHueCycleEnabled(enabled);
        uia(ActionInfo.TOGGLE_HUE_CYCLE).setSelected(enabled);
    }

    public void setAutoTrackInCenter(boolean autoTrackInCenter) {
        fsPanel.setAutoTrackInCenter(autoTrackInCenter);
        uia(ActionInfo.TOGGLE_AUTO_TRACK).setSelected(autoTrackInCenter);
    }

    private void setRepeatMode(@Nullable AbstractAnimator.RepeatMode repeatMode) {
        fsPanel.setRepeatMode(repeatMode);
        final AbstractAnimator.RepeatMode rm = fsPanel.getRepeatMode();

        if (repeatModeComboBox.getSelectedItem() != rm) {
            repeatModeComboBox.setSelectedItem(rm);
        }
    }


    /* ............................  Function Providers  .................................. */

    public void syncFunctionProviders() {
        final FunctionProviders.Stats stats = functionProviders.getStats();
        final Map<FunctionType, Integer> countMap = stats.countMap();
        final RotorStateManager manager = fsPanel.getRotorStateManager();

        final boolean allNoop = countMap.isEmpty() || (countMap.size() == 1 && countMap.containsKey(FunctionType.NO_OP));
        final boolean curNoop = manager.isNoOp();

        syncFunctionDependentOps(!(allNoop || curNoop), manager.getRotorCount() > 0);
        uia(ActionInfo.CLEAR_FUNCTIONS_WITHOUT_DEFINITION).setEnabled(stats.noDefinitionFunctionsCount() > 0);
        uia(ActionInfo.CLEAR_INTERNAL_PROGRAMMATIC_FUNCTIONS).setEnabled(countMap.containsKey(FunctionType.INTERNAL_PROGRAM));
        uia(ActionInfo.CLEAR_EXTERNAL_PROGRAMMATIC_FUNCTIONS).setEnabled(countMap.containsKey(FunctionType.EXTERNAL_PROGRAM));
        uia(ActionInfo.CLEAR_INTERNAL_PATH_FUNCTIONS).setEnabled(countMap.containsKey(FunctionType.INTERNAL_PATH));
        uia(ActionInfo.CLEAR_EXTERNAL_PATH_FUNCTIONS).setEnabled(countMap.containsKey(FunctionType.EXTERNAL_PATH));

        final boolean hasAnyRotorStates = !(allNoop || curNoop) && manager.getAllLoadedRotorStatesCount() > 0;
        final UiAction uiaSaveCsv = uia(ActionInfo.SAVE_ALL_ROTOR_STATES_TO_CSV);
        uiaSaveCsv.setEnabled(uiaSaveCsv.isEnabled() && hasAnyRotorStates);

        final UiAction uiaClearManager = uia(ActionInfo.CLEAR_AND_RESET_ROTOR_STATE_MANAGER);
        uiaClearManager.setEnabled(uiaClearManager.isEnabled() && hasAnyRotorStates);

        functionProviders.add(0, Providers.NoopProvider.getSingleton(), true);
        if (functionProviders.getSize() == 1) {
            functionProviders.setSelectedIndex(0);      // noop
        }
    }

//    public int getFunctionProvidersCount() {
//        return functionComboBox.getItemCount();
//    }
//
//    public boolean isFunctionProviderIndexInvalid(int index) {
//        return index < 0 && index >= getFunctionProvidersCount();
//    }
//
//    public void checkThrowIndex(int index) throws IndexOutOfBoundsException {
//        if (isFunctionProviderIndexInvalid(index))
//            throw new IndexOutOfBoundsException(index);
//    }
//
//    @NotNull
//    public FunctionProviderI getFunctionProviderAt(int index) throws IndexOutOfBoundsException {
//        checkThrowIndex(index);
//        return functionComboBox.getItemAt(index);
//    }
//
//    public int getFunctionProviderIndex(@NotNull FunctionProviderI fp) {
//        for (int i=0; i < getFunctionProvidersCount(); i++) {
//            if (fp.equals(getFunctionProviderAt(i))) {
//                return i;
//            }
//        }
//
//        return -1;
//    }
//
//    public boolean containsFunctionProvider(@NotNull FunctionProviderI fp) {
//        return getFunctionProviderIndex(fp) != -1;
//    }
//
//
//    public void addFunctionProvider(@NotNull FunctionProviderI fp) {
//        functionComboBox.addItem(fp);
//    }
//
//    public void insertFunctionProviderAt(int index, @NotNull FunctionProviderI fp) {
//        functionComboBox.insertItemAt(fp, index);
//    }
//
//    public boolean ensureAddFunctionProvider(@NotNull FunctionProviderI fp) {
//        if (containsFunctionProvider(fp))
//            return false;
//
//        addFunctionProvider(fp);
//        return true;
//    }
//
//    public boolean ensureInsertFunctionProvider(int index, @NotNull FunctionProviderI fp) {
//        if (containsFunctionProvider(fp))
//            return false;
//
//        insertFunctionProviderAt(index, fp);
//        return true;
//    }
//
//
//    public void addFunctionProviders(@NotNull Collection<FunctionProviderI> providers) {
//        providers.forEach(this::addFunctionProvider);
//    }
//
//    public void insertFunctionProviders(int index, @NotNull Collection<FunctionProviderI> providers) {
//        functionComboBox.get
//        providers.forEach(this::addFunctionProvider);
//    }
//
//
//
//    private void removeFunctionProviderAtInternal(int index) {
//        functionComboBox.removeItemAt(index);
//    }
//
//    public boolean removeFunctionProviderAt(int index) {
//        if (isFunctionProviderIndexInvalid(index))
//            return false;
//        removeFunctionProviderAtInternal(index);
//        return true;
//    }
//
//    @NotNull
//    public List<FunctionProviderI> getAllFunctionProviders() {
//        final List<FunctionProviderI> providers = new ArrayList<>(getFunctionProvidersCount() + 2);
//
//        for (int i=0; i < getFunctionProvidersCount(); i++) {
//            providers.add(getFunctionProviderAt(i));
//        }
//
//        return providers;
//    }
//
//    @NotNull
//    public List<FunctionProviderI> getFunctionProviders(@Nullable Predicate<FunctionProviderI> filter) {
//        if (filter == null) {
//            return getAllFunctionProviders();
//        }
//
//        final List<FunctionProviderI> providers = new ArrayList<>();
//
//        for (int i=0; i < getFunctionProvidersCount(); i++) {
//            FunctionProviderI fp = getFunctionProviderAt(i);
//            if (filter.test(fp)) {
//                providers.add(fp);
//            }
//        }
//
//        return providers;
//    }
//
//    public int removeFunctionProviders(@NotNull Predicate<FunctionProviderI> filter) {
//        int modCount = 0;
//
//        for (int i=0; i < getFunctionProvidersCount(); ) {
//            final FunctionProviderI fp = getFunctionProviderAt(i);
//            if (filter.test(fp)) {
//                removeFunctionProviderAtInternal(i);
//                modCount++;
//            } else {
//                i++;
//            }
//        }
//
//        return modCount;
//    }
//




    @Nullable
    private FunctionProviderI mCurProvider;

    public boolean setFunctionProvider(@Nullable FunctionProviderI provider) {
        if (provider == null) {
            provider = Providers.NoopProvider.getSingleton();
        }

        if (mCurProvider == provider)
            return true;

        Runnable post = null;
        boolean done = false;

        try {
            final ComplexDomainFunctionI func = provider.requireFunction();
            setRotorStateManager(new StandardRotorStateManager(func, provider.getFunctionMeta()));
            mCurProvider = provider;
            done = true;
//            setPlay(true);
        } catch (Providers.NoOpProviderException ignored) {
            setPlay(false);
            setRotorStateManager(new RotorStateManager.NoOp());
            mCurProvider = provider;
            done = true;
        } catch (Throwable t) {
            final String msg = "Failed to create function <" + provider + ">";
            Log.e(TAG, msg, t);
            post = () -> showErrorMessageDialog(msg + "\nError Code: " + t.getMessage(), null);

//            if (mCurProvider == null) {
//                final ComplexDomainFunctionI fallbackFunc = Providers.FALLBACK_PROVIDER.getFunction();
//                assert fallbackFunc != null: "Fallback provider is broken!!";
//                setFunction(fallbackFunc);
//                mCurProvider = Providers.FALLBACK_PROVIDER;
//                setPlay(true);
//            }

            // Just stop when failed
            setPlay(false);
            setRotorStateManager(new RotorStateManager.NoOp());
            mCurProvider = Providers.NoopProvider.getSingleton();
        }

        // ensure and select this provider
        functionProviders.ensureAddSelect(mCurProvider);

        if (post != null) {
            EventQueue.invokeLater(post);
        }

        return done;
    }

    public boolean setFunctionProvider(int index) {
        return setFunctionProvider(functionProviders.getElementAt(index));
    }

    public void setScale(double scale, boolean fromPanel) {
        if (!fromPanel) {
            fsPanel.setScale(scale);
        }

        syncScaleAndDrag();
    }

    public void setScale(double scale) {
        setScale(scale, false);
    }

    public boolean incrementScaleByUnit() {
        return fsPanel.incrementScaleByUnit();
    }

    public boolean decrementScaleByUnit() {
        return fsPanel.decrementScaleByUnit();
    }

    public void syncScaleAndDrag() {
        final double scale = fsPanel.getScale();
        scaleText.setText(R.getScaleText(scale));
        uia(ActionInfo.SCALE_UP).setEnabled(scale < fsPanel.getMaximumScale());
        uia(ActionInfo.SCALE_DOWN).setEnabled(scale > fsPanel.getMinimumScale());

        final boolean hasScaleOrDrag = !fsPanel.getRotorStateManager().isNoOp() && fsPanel.hasScaleOrDrag();

        uia(ActionInfo.RESET_SCALE_DRAG).setEnabled(hasScaleOrDrag);

//        resetScaleAndDragButton.setEnabled(hasScaleOrDrag);
//        resetScaleAndDragButton.setVisible(hasScaleOrDrag);
        update();
    }


    public void reset(boolean scaleAndDrag) {
        fsPanel.reset(scaleAndDrag);
    }

    public void resetScaleAndDrag() {
        fsPanel.resetScaleAndDrag();
    }


    @Override
    public void onIsPlayingChanged(boolean playing) {
        setPlay(playing);
    }

    @Override
    public void onDrawingAsWaveChanged(boolean drawingAsWave) {
        setDrawingAsWave(drawingAsWave);
    }

    @Override
    public void onYInvertedChanged(boolean yInverted) {
        setInvertY(yInverted);
    }

    @Override
    public void onXInvertedChanged(boolean xInverted) {
        setInvertX(xInverted);
    }

    @Override
    public void onGraphInCenterChanged(boolean graphInCenter) {
        setGraphInCenter(graphInCenter);
    }

    @Override
    public void onAutoTrackInCenterChanged(boolean autoTrackInCenter) {
        setAutoTrackInCenter(autoTrackInCenter);
    }


    private void syncFunctionDependentOps(boolean hasFunction, boolean hasRotors) {
        final boolean hasBoth = hasFunction && hasRotors;

        for (JComponent c: funcDependentComps) {
            if (c == rotorCountSlider) {
                c.setEnabled(hasFunction);
                continue;
            }

            c.setEnabled(hasBoth);
        }

        ActionInfo.sharedValues()
                .stream()
                .filter(a -> a.functionDependent)
                .forEach(a -> uia(a).setEnabled(a.rotorsDependent? hasBoth: hasFunction));
    }

    private void syncFunctionDependentOps() {
        final RotorStateManager manager = fsPanel.getRotorStateManager();

        final boolean hasFunction = !(manager.isNoOp());
        final boolean hasRotors = manager.getRotorCount() > 0;
        syncFunctionDependentOps(hasFunction, hasRotors);
    }



    private void syncTitle() {
        setTitle(Ui.getWindowTitle(Ui.MAIN_TITLE, fsPanel.getRotorStateManager()));
    }


    @Override
    public void onRotorStateManagerChanged(@Nullable RotorStateManager old, @NotNull RotorStateManager sm) {
        if (old != null)
            old.removeListener(this);
        sm.ensureListener(this);
        syncTitle();

        setHueCycleEnabled(fsPanel.isHueCycleEnabled());
        syncFunctionProviders();
        setPlay(AUTO_PLAY_ON_ROTOR_STATE_MANAGER_CHANGE);
    }

    @Override
    public void onDomainAnimationSpeedChanged(int percent) {
        setSpeedPercent(percent, true);
    }

    @Override
    public void onScaleChanged(double scale) {
        setScale(scale, true);
    }

    @Override
    public void onScalePivotChanged(@Nullable Point2D scalePivot) {
        syncScaleAndDrag();
    }

    @Override
    public void onDragChanged(@Nullable Size drag) {
        syncScaleAndDrag();
    }

    @Override
    public void onRepeatModeChanged(AbstractAnimator.@NotNull RepeatMode repeatMode) {
       setRepeatMode(repeatMode);
    }

//    @Override
//    public void onPointsJoiningEnabledChanged(boolean pointsJoiningEnabled) {
//        setPointsJoiningEnabled(pointsJoiningEnabled);
//    }

    public void setRotorStateManager(@NotNull RotorStateManager rotorStateManager) {
        fsPanel.setRotorStateManager(rotorStateManager);      // TODO: load interceptor
    }

    //    public void setFunction(@NotNull ComplexDomainFunctionI f, int initialRotorCount) {
//        setRotorStateManager(new StandardRotorStateManager(f, meta, initialRotorCount));
//    }
//
//    public void setFunction(@NotNull ComplexDomainFunctionI f) {
//        setFunction(f, -1);
//    }


    protected boolean shouldLoadDefaultExternalPathFunctionsOnStart() {
        return R.LOAD_EXTERNAL_PATH_FUNCTIONS_ON_START;
    }

    private void considerLoadDefaultExternalPathFunctionsOnStartAsync() {
        if (!shouldLoadDefaultExternalPathFunctionsOnStart())
            return;

        // todo show message loading with cancel option
        final Canceller c = R.loadExternalPathFunctionsAsync(R.DIR_EXTERNAL_PATH_FUNCTIONS, R.DEFAULT_VALIDATE_EXTERNAL_FILES, res -> {
            if (res != null) {
                functionProviders.addAll(res.getFunctionProviders());
            }
        });
    }


    private boolean confirmModifNoDefinitionFunctions() {
        final int option = JOptionPane.showConfirmDialog(this,
                """
                        This function has NO DEFINITION (created using IFT over existing Rotor States)
                        Any modification like loading more rotor states or changing Frequency Provider is HIGHLY EXPENSIVE and INACCURATE
                        Do you wish to continue? (can take several minutes)""",
                "Confirm Modify Function",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);

        return (option == JOptionPane.OK_OPTION);
    }

    @Override
    public boolean onInterceptRotorsLoad(@NotNull RotorStateManager manager, int loadCount) {
        final boolean noDefinition = !manager.getFunctionMeta().hasBaseDefinition();
        if (noDefinition) {
            final int initialCount = manager.getFunctionMeta().initialRotorCount();
            if (initialCount < loadCount) {
                return !confirmModifNoDefinitionFunctions();
            }
        }

        return RotorStateManager.Listener.super.onInterceptRotorsLoad(manager, loadCount);
    }

    @Override
    public void onRotorsLoadIntercepted(@NotNull RotorStateManager manager, int loadCount) {

    }

    @Override
    public void onRotorsLoadingChanged(@NotNull RotorStateManager manager, boolean isLoading) {
        syncFunctionDependentOps();
        syncTitle();
    }

    @Override
    public void onRotorsLoadFinished(@NotNull RotorStateManager manager, int count, boolean cancelled) {
    }

    @Override
    public void onRotorsCountChanged(@NotNull RotorStateManager manager, int prevCount, int newCount) {
        updateRotorCountUi(fsPanel.getConstrainedRotorCount());
    }

    @Override
    public boolean onInterceptRotorFrequencyProvider(@NotNull RotorStateManager manager, @Nullable RotorFrequencyProviderI old, @Nullable RotorFrequencyProviderI _new) {
        final boolean noDefinition = !manager.getFunctionMeta().hasBaseDefinition();
        if (noDefinition && !Objects.equals(old, _new)) {
            return !confirmModifNoDefinitionFunctions();
        }

        return RotorStateManager.Listener.super.onInterceptRotorFrequencyProvider(manager, old, _new);
    }

    @Override
    public void onRotorsFrequencyProviderIntercepted(@NotNull RotorStateManager manager, @Nullable RotorFrequencyProviderI rotorFrequencyProvider) {

    }

    @Override
    public void onRotorsFrequencyProviderChanged(@NotNull RotorStateManager manager, @Nullable RotorFrequencyProviderI old, @Nullable RotorFrequencyProviderI _new) {

    }


    public void askSaveFunctionStateToFIle() {
        Ui.askSaveFunctionStateToFIle(FourierUi.this, fsPanel.getRotorStateManager());
    }

    public void askLoadFunctionStateFromFile() {
        Ui.askLoadFunctionStateFromFile(FourierUi.this, functionProviders::ensureAddSelect);
    }


    public void askLoadExternalPathFunctions() {
        Ui.askLoadExternalPathFunctions(FourierUi.this, providers -> {
            functionProviders.addAll(providers);
            if (providers.size() == 1) {
                functionProviders.setSelectedItem(providers.get(0));
            }
        });
    }

    public void askLoadExternalPathFunctionsFromDir() {
        Ui.askLoadExternalPathFunctionsFromDir(FourierUi.this, functionProviders::addAll);
    }


    public final void resetPathFunctions() {
        functionProviders.removeIf(FunctionProviderI.forType(FunctionType.EXTERNAL_PATH));
        functionProviders.addAll(Providers.INTERNAL_PATH, true);
        considerLoadDefaultExternalPathFunctionsOnStartAsync();
    }

    public final void confirmResetPathFunctions() {
        String msg = "This will execute following sequence\n\n1. Reset internal Path Functions\n2. Remove all externally loaded Path Functions";
        if (shouldLoadDefaultExternalPathFunctionsOnStart()) {
            msg += "\n3. Load external path functions from folder " + R.DIR_EXTERNAL_PATH_FUNCTIONS.getFileName();
        }

        final int option = JOptionPane.showConfirmDialog(this, msg, MAIN_TITLE, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            resetPathFunctions();
        }
    }


    public void askLoadExternalProgrammaticFunctions() {
        Ui.askLoadExternalProgrammaticFunctions(FourierUi.this, functionProviders::ensureAddSelect);
    }

    public final void resetProgrammaticFunctions() {
        functionProviders.removeIf(FunctionProviderI.forType(FunctionType.EXTERNAL_PROGRAM));
        functionProviders.addAll(Providers.INTERNAL_PROGRAMS, true);
    }

    public final void confirmResetProgrammaticFunctions() {
        String msg = "This will execute following sequence\n\n1. Reset internal programmatic Functions\n2. Remove all externally loaded Programmatic Functions";

        final int option = JOptionPane.showConfirmDialog(this, msg, MAIN_TITLE, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            resetProgrammaticFunctions();
        }
    }


    public final void confirmRemoveAllFunctionProviders(@NotNull FunctionType type) {
        final String msg = "This will remove all " + type.displayName + " functions. Do you wish to continue?";
        final int option = JOptionPane.showConfirmDialog(this, msg, MAIN_TITLE, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            final int removed = functionProviders.removeIf(FunctionProviderI.forType(type));

            final String removedMsg = (removed > 0? String.valueOf(removed): "No") + " " + type.displayName + " function" + (removed > 1? "s":"") + " removed";
            showInfoMessageDialog(removedMsg, null);
        }
    }

    public final void confirmRemoveFunctionProvidersWithoutDefinition() {
        final String msg = "This will remove all functions without internal definition. Do you wish to continue?";
        final int option = JOptionPane.showConfirmDialog(this, msg, MAIN_TITLE, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            final int removed = functionProviders.removeIf(fp -> !fp.getFunctionMeta().hasBaseDefinition());

            final String removedMsg = (removed > 0? String.valueOf(removed): "No") + " function" + (removed > 1? "s":"") + " removed";
            showInfoMessageDialog(removedMsg, null);
        }
    }


    public void askConfigureFrequencyProvider() {
        Ui.askConfigureFrequencyProvider(FourierUi.this, fsPanel.getRotorStateManager());
    }

    public void askClearAndResetRotorStateManager() {
        Ui.askClearAndResetRotorStateManager(FourierUi.this, fsPanel.getRotorStateManager());
    }

    public void askLoadExternalRotorStatesFromCSV() {
        Ui.askLoadExternalRotorStatesFromCSV(FourierUi.this, fsPanel.getRotorStateManager());
    }

    public void askSaveRotorStatesToCSV() {
        Ui.askSaveRotorStatesToCSV(FourierUi.this, fsPanel.getRotorStateManager());
    }


    @Nullable
    private FTUi prevFtUi;

    public void showFtUi() {
        final FTUi prev = prevFtUi;
        if (prev != null && prev.isDisplayable() && prev.getFtWinderPanel().getRotorStateManager().getFunction() == fsPanel.getRotorStateManager().getFunction()) {
            Ui.close(prev);
        }

        final boolean isFullscreen = isFullscreen();
        if (isFullscreen) {
            setFullscreen(false);
        }

        final FTUi ui = Ui.showFtUi(FourierUi.this, fsPanel.getRotorStateManager());
        prevFtUi = ui;
        if (ui != null) {
            setPlay(false);
            if (isFullscreen) {
                ui.setFullscreen(true);
            }
        }
    }


    public void cancelRunningTasks() {
        if (fsPanel.getRotorStateManager().isLoading()) {
            fsPanel.getRotorStateManager().cancelLoad(true);
            return;
        }

        fsPanel.stop();
    }

    private class FunctionProvidersListener implements ListDataListener {

        @Override
        public void intervalAdded(ListDataEvent e) {
            syncFunctionProviders();
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            syncFunctionProviders();
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            syncFunctionProviders();
        }
    }


    private class WindowHandler implements WindowListener, WindowStateListener, WindowFocusListener {

        @Override
        public void windowOpened(WindowEvent e) {
            App.onWindowOpen(FourierUi.this);
        }

        @Override
        public void windowClosing(WindowEvent e) {
            App.onWindowClose(FourierUi.this);
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

        @Override
        public void windowGainedFocus(WindowEvent e) {

        }

        @Override
        public void windowLostFocus(WindowEvent e) {

        }
    }

    private class MouseHandler implements MouseListener {

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
    }



    /* Actions */

    public class ActionHandler implements UiAction.Listener {

        @Override
        public void onActionPropertyChange(@NotNull UiAction action, @NotNull PropertyChangeEvent e) {
        }

        @Override
        public void onAction(@NotNull UiAction action, @NotNull ActionEvent e) {
            switch (action.info) {
                case CANCEL_RUNNING_TASKS -> cancelRunningTasks();
                case DRAG_UP -> fsPanel.dragYByUnit(false);
                case DRAG_DOWN -> fsPanel.dragYByUnit(true);
                case DRAG_LEFT -> fsPanel.dragXByUnit(false);
                case DRAG_RIGHT -> fsPanel.dragXByUnit(true);
                case SCALE_UP -> fsPanel.incrementScaleByUnit();
                case SCALE_DOWN -> fsPanel.decrementScaleByUnit();
                case PLAY -> setPlay(true);
                case PAUSE -> setPlay(false);
                case STOP -> fsPanel.stop();
                case TOGGLE_PLAY_PAUSE -> togglePlay();
                case TOGGLE_POINTS_JOIN -> fsPanel.togglePointsJoining();
                case TOGGLE_HUE_CYCLE -> fsPanel.toggleHueCycle();
                case TOGGLE_WAVE -> fsPanel.toggleDrawAsWave();
                case INVERT_X -> fsPanel.toggleInvertX();
                case INVERT_Y -> fsPanel.toggleInvertY();
                case TOGGLE_GRAPH_CENTER -> fsPanel.toggleGraphInCenter();
                case TOGGLE_AUTO_TRACK -> fsPanel.toggleAutoTrackInCenter();
                case RESET_MAIN -> fsPanel.reset(false);
                case RESET_SCALE -> fsPanel.resetScale(true);
                case RESET_SCALE_DRAG -> resetScaleAndDrag();
                case RESET_FULL -> fsPanel.reset(true);
                case TOGGLE_FULLSCREEN -> toggleFullscreen();
                case TOGGLE_CONTROLS -> toggleControlsVisibility();
                case TOGGLE_MENUBAR -> toggleMenuBarVisible();
                case SAVE_FUNCTION_STATE_TO_FILE -> askSaveFunctionStateToFIle();
                case LOAD_FUNCTION_STATE_FROM_FILE -> askLoadFunctionStateFromFile();
                case CLEAR_FUNCTIONS_WITHOUT_DEFINITION -> confirmRemoveFunctionProvidersWithoutDefinition();
                case LOAD_EXTERNAL_PATH_FUNCTIONS -> askLoadExternalPathFunctions();
                case LOAD_EXTERNAL_PATH_FUNCTIONS_FROM_DIR -> askLoadExternalPathFunctionsFromDir();
                case CLEAR_INTERNAL_PATH_FUNCTIONS -> confirmRemoveAllFunctionProviders(FunctionType.INTERNAL_PATH);
                case CLEAR_EXTERNAL_PATH_FUNCTIONS -> confirmRemoveAllFunctionProviders(FunctionType.EXTERNAL_PATH);
                case RESET_PATH_FUNCTIONS -> confirmResetPathFunctions();
                case LOAD_EXTERNAL_PROGRAMMATIC_FUNCTION -> askLoadExternalProgrammaticFunctions();
                case CLEAR_INTERNAL_PROGRAMMATIC_FUNCTIONS -> confirmRemoveAllFunctionProviders(FunctionType.INTERNAL_PROGRAM);
                case CLEAR_EXTERNAL_PROGRAMMATIC_FUNCTIONS -> confirmRemoveAllFunctionProviders(FunctionType.EXTERNAL_PROGRAM);
                case RESET_PROGRAMMATIC_FUNCTIONS -> confirmResetProgrammaticFunctions();
                case CONFIGURE_ROTOR_FREQUENCY_PROVIDER -> askConfigureFrequencyProvider();
                case CLEAR_AND_RESET_ROTOR_STATE_MANAGER -> askClearAndResetRotorStateManager();
                case LOAD_EXTERNAL_ROTOR_STATES_FROM_CSV -> askLoadExternalRotorStatesFromCSV();
                case SAVE_ALL_ROTOR_STATES_TO_CSV -> askSaveRotorStatesToCSV();
                case SHOW_FT_UI -> showFtUi();
            }
        }
    }


    private void setupActionKeyBindings(@NotNull Collection<InputMap> inputMaps, @NotNull ActionMap actionMap) {
        ActionInfo.sharedValues().forEach(info -> {
            if (info.keyStroke != null) {
                inputMaps.forEach(im -> im.put(info.keyStroke, info));
            }

            actionMap.put(info, uia(info));
        });
    }

    private void setupActionKeyBindings(@NotNull JComponent component, int @NotNull ... inputMapConditions) {
        final List<InputMap> maps = new LinkedList<>();
        for (int i: inputMapConditions) {
            maps.add(component.getInputMap(i));
        }

        setupActionKeyBindings(maps, component.getActionMap());
    }




    @Nullable
    private static EnumMap<ActionInfo, UiAction> sActionMap;

    @NotNull
    public static UiAction getUia(@NotNull ActionInfo info) {
        UiAction uia = null;
        if (sActionMap == null) {
            sActionMap = new EnumMap<>(ActionInfo.class);
        } else {
            uia = sActionMap.get(info);
        }

        if (uia == null) {
            uia = new UiAction(info);
            sActionMap.put(info, uia);
        }

        return uia;
    }

    @NotNull
    public UiAction uia(@NotNull ActionInfo info) {
        final UiAction action = getUia(info);
        action.ensureListener(mActionHandler);
        return action;
    }

}
