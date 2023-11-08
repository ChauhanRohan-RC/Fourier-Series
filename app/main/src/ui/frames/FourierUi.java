package ui.frames;

import animation.animator.AbstractAnimator;
import app.R;
import function.definition.ComplexDomainFunctionI;
import misc.Log;
import models.FunctionGraphMode;
import models.Size;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import provider.*;
import rotor.RotorStateManager;
import rotor.StandardRotorStateManager;
import rotor.frequency.RotorFrequencyProviderI;
import ui.action.ActionInfo;
import ui.action.UiAction;
import ui.MusicPlayer;
import ui.AuxSoundsPlayer;
import ui.panels.FourierSeriesPanel;
import ui.util.Ui;
import async.Canceller;
import util.PathFunctionManager;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.util.*;

public class FourierUi extends BaseFrame implements RotorStateManager.Listener, FourierSeriesPanel.PanelListener {

    public static final String TAG = "FourierUi";

    public static final boolean DEFAULT_FULLSCREEN = false;
    public static final boolean DEFAULT_CONTROLS_VISIBLE = true;
    public static final boolean DEFAULT_MENUBAR_VISIBLE = true;
    public static final boolean AUTO_PLAY_ON_ROTOR_STATE_MANAGER_CHANGE = true;

    public static final Dimension MINIMUM_SIZE = new Dimension(400, 400);

    public static final int INITIAL_WIDTH = SCREEN_SIZE.width - 200;
    public static final int INITIAL_HEIGHT = SCREEN_SIZE.height - 200;


    private final ListDataListener mFunctionProvideListener = new ListDataListener() {

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
    };


    @NotNull
    private final Timer looper;
    final FourierSeriesPanel fsPanel;

    final JPanel controlPanel;
    final JScrollPane controlScrollPane;
    final JLabel rotorCountText;
    final JSlider rotorCountSlider;
    final JLabel speedText;
    final JSlider speedSlider;

    @NotNull
    final FunctionProviderList functionProviders;
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
//    final JMenu menuView;

    @NotNull
    private final Object token = new Object();

    public FourierUi() {
        this(null, 0 /* First */);
    }

    public FourierUi(@Nullable String title, int initialProviderIndex) {
        super();
        setTitle(title == null || title.isEmpty()? R.TITLE_MAIN : title);

        looper = Ui.createLooper(null);

        fsPanel = new FourierSeriesPanel(new RotorStateManager.NoOp());
        looper.addActionListener(Ui.actionListener(fsPanel));

        // Function
        functionProviders = new FunctionProviderList(Providers.ALL_INTERNAL_FUNCTIONS, -1);

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

        functionComboBox.addActionListener(e -> selectFunctionProvider(functionComboBox.getSelectedIndex()));
        repeatModeComboBox.addActionListener(e -> setRepeatMode((AbstractAnimator.RepeatMode) repeatModeComboBox.getSelectedItem()));


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
        menuPathFunctions.add(uia(ActionInfo.LAUNCH_PATH_DRAWING_UI));
        menuPathFunctions.add(uia(ActionInfo.LAUNCH_PATH_DRAWING_UI_EXPORT_CURRENT_FUNCTION));
        menuPathFunctions.addSeparator();
        menuPathFunctions.add(uia(ActionInfo.LOAD_EXTERNAL_PATH_FUNCTIONS));
        menuPathFunctions.add(uia(ActionInfo.LOAD_EXTERNAL_PATH_FUNCTIONS_FROM_DIR));
        menuPathFunctions.add(uia(ActionInfo.CONVERT_SVG_TO_PATH_DATA));
        menuPathFunctions.addSeparator();
        menuPathFunctions.add(uia(ActionInfo.CLEAR_INTERNAL_PATH_FUNCTIONS));
        menuPathFunctions.add(uia(ActionInfo.CLEAR_EXTERNAL_PATH_FUNCTIONS));
        menuPathFunctions.addSeparator();
        menuPathFunctions.add(uia(ActionInfo.RESET_PATH_FUNCTIONS));

        // Rotor States Menu
        menuRotorStates = Ui.createRotorStatesMenu(this::uia, true);
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
//        menuView = new JMenu("View");
//        menuBar.add(menuView);
//        menuView.add(uia(ActionInfo.TOGGLE_MENUBAR));
//        menuView.add(uia(ActionInfo.TOGGLE_CONTROLS));
//        menuView.addSeparator();
//        menuView.add(uia(ActionInfo.TOGGLE_FULLSCREEN));
        menuBar.add(createViewMenu());

        // Music Menu
        menuBar.add(MusicPlayer.getSingleton().createPlaybackMenu());

        // Settings
        menuBar.add(Ui.createSettingsMenu(this));

        // Ui components that depend on function
        funcDependentComps = new JComponent[] {
                rotorCountSlider,
                speedSlider,
        };

        // Run
        functionProviders.addListDataListener(mFunctionProvideListener);
        selectFunctionProvider(initialProviderIndex);     // First function provider
        setupActionKeyBindings(getRootPane(), null, JComponent.WHEN_IN_FOCUSED_WINDOW, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        fsPanel.addMouseListener(this);

        final Image appIcon = R.createAppIcon();
        if (appIcon != null) {
            setIconImage(appIcon);
        }

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
            setFullscreenInternal(DEFAULT_FULLSCREEN);                // sync
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

    @Override
    public @Nullable Component getControlsComponent() {
        return controlScrollPane;
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
        final FunctionProviderList.Stats stats = functionProviders.getStats();
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
        uia(ActionInfo.LAUNCH_PATH_DRAWING_UI_EXPORT_CURRENT_FUNCTION).setEnabled(!curNoop);

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


    public int getFunctionProviderCount() {
        return functionProviders.getSize();
    }

    @NotNull
    public FunctionProviderI getFunctionProviderAt(int index) throws IndexOutOfBoundsException {
        return functionProviders.get(index);
    }

    @NotNull
    public FunctionProviderList.Stats getFunctionProviderStats() {
        return functionProviders.getStats();
    }

    @Nullable
    private FunctionProviderI mCurrentFp;

    public boolean addSelectFunctionProvider(@Nullable FunctionProviderI fp) {
        if (fp == null)
            fp = Providers.NoopProvider.getSingleton();

        if (mCurrentFp == fp)
            return true;

        Runnable post = null;
        boolean done = false;

        try {
            final ComplexDomainFunctionI func = fp.requireFunction();
            setRotorStateManager(new StandardRotorStateManager(func, fp.getFunctionMeta()));
            mCurrentFp = fp;
            done = true;
        } catch (Throwable t) {
            setPlay(false);   // Just stop when failed
            setRotorStateManager(new RotorStateManager.NoOp());
            mCurrentFp = Providers.NoopProvider.getSingleton();

            if (t instanceof Providers.NoOpProviderException) {
                done = true;            // Noop provider
            } else {
                final String msg = "Failed to create function <" + fp + ">";
                Log.e(TAG, msg, t);
                post = () -> showErrorMessageDialog(String.format("%s\nError: %s -> %s", msg, t.getClass().getSimpleName(), t.getMessage()), "Function Provider");
            }
        }

        // ensure and select this fp
        functionProviders.ensureAddSelect(mCurrentFp);

        if (post != null) {
            EventQueue.invokeLater(post);
        }

        return done;
    }

    public boolean selectFunctionProvider(int index) {
        return addSelectFunctionProvider(functionProviders.getElementAt(index));
    }

    public void addFunctionProvider(int index, @NotNull FunctionProviderI fp) {
        functionProviders.add(index, fp, true);
    }

    public void addFunctionProvider(@NotNull FunctionProviderI fp) {
        functionProviders.add(fp, true);
    }

    @NotNull
    public FunctionProviderI removeFunctionProvider(int index) {
        return functionProviders.remove(index);
    }

    public boolean removeFunctionProvider(@NotNull Object fp) {
        return functionProviders.remove(fp);
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

        final boolean pre = !fsPanel.getRotorStateManager().isNoOp();
        final boolean hasScale = fsPanel.hasScale(), hasDrag = fsPanel.hasDrag();

        uia(ActionInfo.RESET_SCALE).setEnabled(pre && hasScale);
        uia(ActionInfo.RESET_DRAG).setEnabled(pre && hasDrag);
        uia(ActionInfo.RESET_SCALE_DRAG).setEnabled(pre && (hasScale || hasDrag));

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
        MusicPlayer.getSingleton().requestPlayPause(token, playing);
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
        setTitle(Ui.getWindowTitle(R.TITLE_MAIN, fsPanel.getRotorStateManager()));
    }


    @Override
    public void onRotorStateManagerChanged(@Nullable RotorStateManager old, @NotNull RotorStateManager sm) {
        if (old != null)
            old.removeListener(this);
        sm.ensureListener(this);
        syncTitle();
        setHueCycleEnabled(fsPanel.isHueCycleEnabled());
        syncFunctionProviders();
        setDrawingAsWave(sm.getFunction().getDefaultGraphMode() != FunctionGraphMode.OUTPUT_SPACE);
        setPlay(AUTO_PLAY_ON_ROTOR_STATE_MANAGER_CHANGE);

        if (old != null && sm != old && !sm.isNoOp()) {
            AuxSoundsPlayer.getSingleton().playBeep();
        }
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
        fsPanel.setRotorStateManager(rotorStateManager);
    }

    //    public void setFunction(@NotNull ComplexDomainFunctionI f, int defaultInitialRotorCount) {
//        setRotorStateManager(new StandardRotorStateManager(f, meta, defaultInitialRotorCount));
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
        final Canceller c = PathFunctionManager.loadExternalPathFunctionsAsync(R.DIR_EXTERNAL_PATH_FUNCTIONS, res -> {
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
            final boolean otherSupported = manager.getFunction().frequenciesExceptExplicitSupported();
            if (otherSupported) {
                return !confirmModifNoDefinitionFunctions();        // frequencies can still change on low load
//                final int initialCount = manager.getFunctionMeta().defaultInitialRotorCount();
//                if (initialCount < loadCount) {
//                    return !confirmModifNoDefinitionFunctions();
//                }
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

        if (isLoading && !isPlaying()) {
            MusicPlayer.getSingleton().requestPlay(token);
        }
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
            final boolean otherSupported = manager.getFunction().frequenciesExceptExplicitSupported();
            if (otherSupported) {
                return !confirmModifNoDefinitionFunctions();
            }
        }

        return RotorStateManager.Listener.super.onInterceptRotorFrequencyProvider(manager, old, _new);
    }

    @Override
    public void onRotorsFrequencyProviderIntercepted(@NotNull RotorStateManager manager, @Nullable RotorFrequencyProviderI rotorFrequencyProvider) {

    }

    @Override
    public void onRotorsFrequencyProviderChanged(@NotNull RotorStateManager manager, @Nullable RotorFrequencyProviderI old, @Nullable RotorFrequencyProviderI _new) {

    }

    @Override
    public void onLookAndFeelChanged(@NotNull String className) {
        super.onLookAndFeelChanged(className);
    }

    @Override
    public void onFTIntegrationIntervalCountChanged(int fourierTransformSimpson13NDefault) {
        super.onFTIntegrationIntervalCountChanged(fourierTransformSimpson13NDefault);
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

    public void askExtractPathDataFromSVG() {
        Ui.askExtractPathDataFromSVG(FourierUi.this, null);
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

        final int option = JOptionPane.showConfirmDialog(this, msg, R.TITLE_MAIN, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
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

        final int option = JOptionPane.showConfirmDialog(this, msg, R.TITLE_MAIN, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            resetProgrammaticFunctions();
        }
    }


    public final void confirmRemoveAllFunctionProviders(@NotNull FunctionType type) {
        final String msg = "This will remove all " + type.displayName + " functions. Do you wish to continue?";
        final int option = JOptionPane.showConfirmDialog(this, msg, R.TITLE_MAIN, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            final int removed = functionProviders.removeIf(FunctionProviderI.forType(type));

            final String removedMsg = (removed > 0? String.valueOf(removed): "No") + " " + type.displayName + "function" + (removed > 1? "s":"") + " removed";
            showInfoMessageDialog(removedMsg, null);
        }
    }

    public final void confirmRemoveFunctionProvidersWithoutDefinition() {
        final String msg = "This will remove all functions without internal definition. Do you wish to continue?";
        final int option = JOptionPane.showConfirmDialog(this, msg, R.TITLE_MAIN, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            final int removed = functionProviders.removeIf(fp -> !fp.getFunctionMeta().hasBaseDefinition());

            final String removedMsg = (removed > 0? String.valueOf(removed): "No") + "function" + (removed > 1? "s":"") + " removed";
            showInfoMessageDialog(removedMsg, null);
        }
    }


    public void askConfigureFrequencyProvider() {
        Ui.askConfigureFrequencyProvider(FourierUi.this, fsPanel.getRotorStateManager());
    }

    public void askClearAndResetRotorStateManager(boolean reload) {
        Ui.askClearAndResetRotorStateManager(FourierUi.this, fsPanel.getRotorStateManager(), reload);
    }

    public void askLoadExternalRotorStatesFromCSV() {
        Ui.askLoadExternalRotorStatesFromCSV(FourierUi.this, fsPanel.getRotorStateManager());
    }

    public void askLoadExternalRotorStateFunctionFromCSV() {
        Ui.askLoadExternalRotorStateFunctionFromCSV(FourierUi.this, functionProviders::ensureAddSelect);
    }


    public void askSaveRotorStatesToCSV() {
        Ui.askSaveRotorStatesToCSV(FourierUi.this, fsPanel.getRotorStateManager());
    }


    @Override
    public void windowOpened(WindowEvent e) {
        AuxSoundsPlayer.getSingleton().playWindowOpen();
        super.windowOpened(e);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        MusicPlayer.getSingleton().requestPause(token);
        AuxSoundsPlayer.getSingleton().playWindowClose();
        super.windowClosing(e);
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

        final FTUi ui = Ui.launchFtUi(FourierUi.this, fsPanel.getRotorStateManager());
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



    /* Mouse Path Ui */

    private static int sCustomDrawingCounter;

    public static int nextCustomDrawingNumber() {
        return ++sCustomDrawingCounter;
    }

//    public boolean addPathFunction(@NotNull List<Path2D> paths) {
//        if (paths.isEmpty())
//            return false;
//
//        final Path2D path = PathUtil.mergePaths(paths, false);
//        if (path == null)
//            return false;
//
//        try {
//            final PathFunctionMerger merger = PathFunctionMerger.create(path);
//            merger.setDomainAnimDurationScale(0.4f);
//
//            final String name = JOptionPane.showInputDialog(this, "Enter a name for the Drawing");
//
//            final FunctionMeta meta = new FunctionMeta(FunctionType.EXTERNAL_PATH, Format.isEmpty(name)? String.format("Drawing %d", nextCustomDrawingNumber()): name);
//            final FunctionProviderI fp = new SimpleFunctionProvider(meta, merger);
//            functionProviders.ensureAddSelect(fp);
//            return true;
//        } catch (Throwable t) {
//            Log.e(TAG, "failed dto create Path Function from paths " + paths, t);
//        }
//
//        return false;
//    }
//
//    @Override
//    public void onMousePathUiFinished(@NotNull List<Path2D> paths) {
//        addPathFunction(paths);
//    }


    private void launchMousePathUi(boolean exportCurrentFunction) {
        if (exportCurrentFunction) {
            Ui.launchMousePathUiAndExport(fsPanel.getRotorStateManager().getFunction(), true, null);
        } else {
            Ui.launchMousePathUi(null);
        }
    }

    /* Mouse Listener */

    @Override
    public void mouseClicked(MouseEvent e) {
       super.mouseClicked(e);
    }


    /* Actions */

    @Override
    public void onActionPropertyChange(@NotNull UiAction action, @NotNull PropertyChangeEvent e) {
    }

    @Override
    public boolean onAction(@NotNull UiAction action, @NotNull ActionEvent e) {
        if (super.onAction(action, e))
            return true;

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
            case RESET_DRAG-> fsPanel.resetDrag(true);
            case RESET_SCALE_DRAG -> resetScaleAndDrag();
            case RESET_FULL -> fsPanel.reset(true);
            case SAVE_FUNCTION_STATE_TO_FILE -> askSaveFunctionStateToFIle();
            case LOAD_FUNCTION_STATE_FROM_FILE -> askLoadFunctionStateFromFile();
            case CLEAR_FUNCTIONS_WITHOUT_DEFINITION -> confirmRemoveFunctionProvidersWithoutDefinition();
            case LAUNCH_PATH_DRAWING_UI -> launchMousePathUi(false);
            case LAUNCH_PATH_DRAWING_UI_EXPORT_CURRENT_FUNCTION -> launchMousePathUi(true);
            case LOAD_EXTERNAL_PATH_FUNCTIONS -> askLoadExternalPathFunctions();
            case LOAD_EXTERNAL_PATH_FUNCTIONS_FROM_DIR -> askLoadExternalPathFunctionsFromDir();
            case CONVERT_SVG_TO_PATH_DATA -> askExtractPathDataFromSVG();
            case CLEAR_INTERNAL_PATH_FUNCTIONS -> confirmRemoveAllFunctionProviders(FunctionType.INTERNAL_PATH);
            case CLEAR_EXTERNAL_PATH_FUNCTIONS -> confirmRemoveAllFunctionProviders(FunctionType.EXTERNAL_PATH);
            case RESET_PATH_FUNCTIONS -> confirmResetPathFunctions();
            case LOAD_EXTERNAL_PROGRAMMATIC_FUNCTION -> askLoadExternalProgrammaticFunctions();
            case CLEAR_INTERNAL_PROGRAMMATIC_FUNCTIONS -> confirmRemoveAllFunctionProviders(FunctionType.INTERNAL_PROGRAM);
            case CLEAR_EXTERNAL_PROGRAMMATIC_FUNCTIONS -> confirmRemoveAllFunctionProviders(FunctionType.EXTERNAL_PROGRAM);
            case RESET_PROGRAMMATIC_FUNCTIONS -> confirmResetProgrammaticFunctions();
            case CONFIGURE_ROTOR_FREQUENCY_PROVIDER -> askConfigureFrequencyProvider();
            case CLEAR_AND_RESET_ROTOR_STATE_MANAGER -> askClearAndResetRotorStateManager(false);
            case CLEAR_AND_RELOAD_ROTOR_STATE_MANAGER -> askClearAndResetRotorStateManager(true);
            case LOAD_EXTERNAL_ROTOR_STATES_FROM_CSV -> askLoadExternalRotorStatesFromCSV();
            case LOAD_EXTERNAL_ROTOR_STATE_FUNCTION_FROM_CSV -> askLoadExternalRotorStateFunctionFromCSV();
            case SAVE_ALL_ROTOR_STATES_TO_CSV -> askSaveRotorStatesToCSV();
            case SHOW_FT_UI -> showFtUi();
            default -> {
                return false;
            }
        }

        return true;
    }

}
