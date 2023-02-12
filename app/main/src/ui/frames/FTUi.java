package ui.frames;

import action.BaseAction;
import animation.animator.AbstractAnimator;
import app.R;
import async.Async;
import async.Consumer;
import async.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rotor.RotorState;
import rotor.RotorStateManager;
import rotor.frequency.RotorFrequencyProviderI;
import ui.action.ActionInfo;
import ui.action.UiAction;
import ui.MusicPlayer;
import ui.AuxSoundsPlayer;
import ui.panels.FTGraphPanel;
import ui.panels.FTWinderPanel;
import ui.panels.FunctionGraphPanel;
import ui.util.Ui;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.util.EnumMap;
import java.util.Hashtable;
import java.util.Map;

public class FTUi extends BaseFrame {

    public static final String TAG = "FtUI";

    private static final Hashtable<Integer, JComponent> EMPTY_LABEL_TABLE = new Hashtable<>();

    public static final boolean DEFAULT_FULLSCREEN = false;
    public static final boolean DEFAULT_CONTROLS_VISIBLE = true;
    public static final boolean DEFAULT_MENUBAR_VISIBLE = true;

    public static final Dimension MINIMUM_SIZE = new Dimension(400, 400);

    public static final int INITIAL_WIDTH = SCREEN_SIZE.width - 200;
    public static final int INITIAL_HEIGHT = SCREEN_SIZE.height - 200;

    private final FTWinderPanel.Listener winderListener = new FTWinderPanel.Listener() {
        @Override
        public void onRotorsCountChanged(@NotNull FTWinderPanel panel, int rotorsCount) {
            setRotorCount(panel.getRotorCount(), true);
        }

        @Override
        public void onIsLoadingChanged(@NotNull FTWinderPanel panel, boolean isLoading) {
            syncTitle();

            if (isLoading && !isPlaying()) {
                MusicPlayer.getSingleton().requestPlay(token);
            }
        }

        @Override
        public void onIsPlayingChanged(@NotNull FTWinderPanel panel, boolean playing) {
            setPlay(panel.isPlaying());
            MusicPlayer.getSingleton().requestPlayPause(token, playing);
        }

        @Override
        public void onRotorsAnimationSpeedChanged(@NotNull FTWinderPanel panel, int speedPercent) {
            setSpeedPercent(speedPercent, true);
        }

        @Override
        public void onRotorsAnimationRepeatModeChanged(@NotNull FTWinderPanel panel, AbstractAnimator.@NotNull RepeatMode repeatMode) {
            setRepeatMode(repeatMode, true);
        }

        @Override
        public void onCurrentRotorChanged(@NotNull FTWinderPanel panel, int currentRotorIndex) {
            if (curRotorSlider.getValueIsAdjusting()) {
                updateCurrentRotorUi(panel.getRotorCount(), curRotorSlider.getValue(), ignoreAdjustingCurrentRotorValue());
            } else {
                setCurrentRotor(panel.getCurrentRotorIndex(), true);
            }
        }

        @Override
        public void onRotorsFrequencyProviderChanged(@NotNull FTWinderPanel panel, @Nullable RotorFrequencyProviderI old, @Nullable RotorFrequencyProviderI _new) {

        }

        @Override
        public void onFlagsChanged(@NotNull FTWinderPanel panel, int oldFlags, int newFlags) {

        }
    };


    @NotNull
    private final Timer looper;
    @NotNull
    private final FunctionGraphPanel functionGraphPanel;
    @NotNull
    private final FTWinderPanel ftWinderPanel;
    @NotNull
    private final FTGraphPanel ftGraphPanel;

    final JPanel controlPanel;
    final JScrollPane controlScrollPane;
    final JLabel rotorCountText;
    final JSlider rotorCountSlider;
    final JLabel curRotorText;
    final JSlider curRotorSlider;
    final JLabel speedText;
    final JSlider speedSlider;

    final JButton playButton;
    final JButton resetButton;
    final JCheckBox pointsJoinCheck;

    final JLabel endBehaviourLabel;
    final JComboBox<AbstractAnimator.RepeatMode> repeatModeComboBox;

    final JButton toggleControlsButton;
//    final JButton configButton;

    /* Menu */
    final JMenuBar menuBar;
    final JMenu menuRotorStates;
    final JMenu menuFunctionState;
    final JMenu menuTransform;
//    final JMenu menuView;

    private final JComponent[] rotorDependentComps;              // Ui components that depend on Rotors

    @NotNull
    private final JSplitPane splitPaneGraphsVertical;
    @NotNull
    private final JSplitPane splitPaneWinderGraphsHorizontal;
//    @NotNull
//    private final JSplitPane splitPaneContentControlsVertical;

    private boolean ignoreNextCurrentRotorSliderEvent;

    @NotNull
    private final PanelViewController viewControllerWinder;
    @NotNull
    private final PanelViewController viewControllerFunctionGraph;
    @NotNull
    private final PanelViewController viewControllerFtGraph;

    @NotNull
    private final Object token = new Object();

    private static final float DEFAULT_WEIGHT_FUNCTION_GRAPH_PANEL = 0.5f;
    private static final float DEFAULT_WEIGHT_WINDER_PANEL = 0.4f;
//    private static final float DEFAULT_WEIGHT_CONTROL_PANEL = 0.15f;


    public FTUi(@NotNull RotorStateManager manager) {
        super();
        setTitle(R.TITLE_FT);
        looper = Ui.createLooper(null);

        ftWinderPanel = new FTWinderPanel(manager);
        functionGraphPanel = new FunctionGraphPanel(manager.getFunction(), manager.getFunctionMeta());
        ftGraphPanel = new FTGraphPanel(ftWinderPanel);

        // looper
        looper.addActionListener(Ui.actionListener(ftWinderPanel));
        syncTitle();

        // split panes
        splitPaneGraphsVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, functionGraphPanel, ftGraphPanel);
        splitPaneGraphsVertical.setContinuousLayout(true);
        splitPaneGraphsVertical.setResizeWeight(0.5f);
        splitPaneGraphsVertical.setDividerSize(3);

        splitPaneWinderGraphsHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, ftWinderPanel, splitPaneGraphsVertical);
        splitPaneWinderGraphsHorizontal.setContinuousLayout(true);
        splitPaneWinderGraphsHorizontal.setResizeWeight(0.65f);
        splitPaneWinderGraphsHorizontal.setDividerSize(3);

        controlPanel = new JPanel();
        controlPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
        controlScrollPane = new JScrollPane(controlPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);


        // controls
        pointsJoinCheck = new JCheckBox(ftWinderPanel.getTogglePointsJoinAction());

        // Rotor Count
        final int rotorCount = ftWinderPanel.getConstrainedRotorCount();
        final int curRotorIndex= ftWinderPanel.getCurrentRotorIndex();
        rotorCountText = new JLabel(R.getRotorCountText(rotorCount));

        rotorCountSlider = new JSlider(SwingConstants.HORIZONTAL, FTWinderPanel.ROTOR_COUNT_MIN, FTWinderPanel.ROTOR_COUNT_MAX, rotorCount);
        rotorCountSlider.setToolTipText(R.getRotorCountSliderShortDescription());
        rotorCountSlider.setLabelTable(rotorCountSlider.createStandardLabels(FTWinderPanel.ROTOR_COUNT_SLIDER_LABEL_INCREMENT, FTWinderPanel.ROTOR_COUNT_MIN));
        rotorCountSlider.setPaintLabels(true);

        // Current Rotor
        curRotorText = new JLabel();
        curRotorSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 0, 0);
        curRotorSlider.setToolTipText(R.getCurrentRotorSliderShortDescription());
        updateCurrentRotorUi(rotorCount, curRotorIndex, false);

        // Speed
        final int speed = ftWinderPanel.getRotorsAnimationSpeedPercent();
        speedText = new JLabel(R.getSpeedPercentText(speed));

        speedSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, speed);
        speedSlider.setToolTipText(R.getSpeedSliderShortDescription());
        speedSlider.setLabelTable(speedSlider.createStandardLabels(25, 0));
        speedSlider.setPaintLabels(true);

        // Play/Pause Toggle
        final boolean playing = ftWinderPanel.isPlaying();
        final Action togglePlayAction = uia(ActionInfo.TOGGLE_PLAY_PAUSE)
                .setName(R.getPlayPauseText(playing))
                .setShortDescription(R.getPlayPauseShortDescription(playing))
                .setSelected(!playing);

        playButton = new JButton(togglePlayAction);

        // Reset
        resetButton = new JButton(uia(ActionInfo.RESET_MAIN));

        // Repeat
        endBehaviourLabel = new JLabel(R.getRepeatModeLabelText());
        endBehaviourLabel.setToolTipText(R.getRepeatModeShortDescription());

        repeatModeComboBox = new JComboBox<>(AbstractAnimator.RepeatMode.values());
        repeatModeComboBox.setSelectedItem(ftWinderPanel.getRepeatMode());
        repeatModeComboBox.setToolTipText(R.getRepeatModeShortDescription());


        // others
        final Action controlUia = uia(ActionInfo.TOGGLE_CONTROLS)
                .setName(R.getToggleControlsText(DEFAULT_CONTROLS_VISIBLE))
                .setShortDescription(R.getToggleControlsShortDescription(DEFAULT_CONTROLS_VISIBLE))
                .setSelected(DEFAULT_CONTROLS_VISIBLE);

        toggleControlsButton = new JButton(controlUia);
//        configButton = new JButton(uia(ActionInfo.CONFIGURATIONS));

//         Layout
        final JPanel buttonsPanel = new JPanel(new GridLayout(2, 1, 2, 4));
        buttonsPanel.add(playButton);
        buttonsPanel.add(resetButton);
        controlPanel.add(buttonsPanel);

        final JPanel funcOpsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 2));
        funcOpsPanel.add(pointsJoinCheck);

        final JPanel endComboPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 2));
        endComboPanel.add(endBehaviourLabel);
        endComboPanel.add(repeatModeComboBox);

        final JPanel endOpsPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        endOpsPanel.add(endComboPanel);
        endOpsPanel.add(funcOpsPanel);
        controlPanel.add(endOpsPanel);

        final JPanel slidersPanel = new JPanel(new GridLayout(2, 3, 5, 2));
        rotorCountText.setHorizontalAlignment(SwingConstants.CENTER);
        curRotorText.setHorizontalAlignment(SwingConstants.CENTER);
        speedText.setHorizontalAlignment(SwingConstants.CENTER);

        slidersPanel.add(rotorCountText);
        slidersPanel.add(curRotorText);
        slidersPanel.add(speedText);
        slidersPanel.add(rotorCountSlider);
        slidersPanel.add(curRotorSlider);
        slidersPanel.add(speedSlider);
        controlPanel.add(slidersPanel);

        toggleControlsButton.setVerticalAlignment(SwingConstants.CENTER);
        controlPanel.add(toggleControlsButton);

        setBounds(Ui.windowBoundsCenterScreen(INITIAL_WIDTH, INITIAL_HEIGHT));
        setMinimumSize(MINIMUM_SIZE);
        setLayout(new BorderLayout());
//        add(controlScrollPane, BorderLayout.SOUTH);
//        add(splitTwo, BorderLayout.CENTER);
//        add(splitPaneContentControlsVertical, BorderLayout.CENTER);
        add(controlScrollPane, BorderLayout.SOUTH);
        add(splitPaneWinderGraphsHorizontal, BorderLayout.CENTER);

        // Listeners
        ftWinderPanel.ensureListener(winderListener);

        rotorCountSlider.addChangeListener(ev -> {
            final int val = rotorCountSlider.getValue();
            if (ignoreAdjustingRotorCountValue() && rotorCountSlider.getValueIsAdjusting()) {
                updateRotorCountText(val);
            } else {
                setRotorCount(val);
            }
        });

        curRotorSlider.addChangeListener(ev -> {
            final int val = curRotorSlider.getValue();
            if (ignoreAdjustingCurrentRotorValue() && curRotorSlider.getValueIsAdjusting()) {
                updateCurrentRotorText(val);
            } else {
                if (ignoreNextCurrentRotorSliderEvent) {
                    ignoreNextCurrentRotorSliderEvent = false;
                    return;
                }

                setCurrentRotor(val, false);
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


        /* Menu */
        viewControllerWinder = new PanelViewController(this::winderPanelExpandState, this::setWInderPanelExpanded);
        viewControllerFunctionGraph = new PanelViewController(this::functionGraphExpandState, this::setFunctionGraphExpanded);
        viewControllerFtGraph = new PanelViewController(this::fTGraphExpandState, this::setFTGraphExpanded);

        menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // Rotor States Menu
        menuRotorStates = Ui.createRotorStatesMenu(this::uia, false);
        menuBar.add(menuRotorStates);

        // Function State menu
        menuFunctionState = new JMenu("Function State");
        menuBar.add(menuFunctionState);
        menuFunctionState.add(uia(ActionInfo.SAVE_FUNCTION_STATE_TO_FILE));

        // Transform Menu (Should not have view controllers)
        menuTransform = new TransformMenu();
        menuBar.add(menuTransform);

        // View menu (With view controllers)
        ftWinderPanel.addExtraMenuBinder(viewControllerWinder::addAsSeparateViewMenu);
        functionGraphPanel.addExtraMenuBinder(viewControllerFunctionGraph::addAsSeparateViewMenu);
        ftGraphPanel.addExtraMenuBinder(viewControllerFtGraph::addAsSeparateViewMenu);

        final JMenu winderViewMenu = new JMenu(uia(ActionInfo.CONFIGURE_FT_WINDER_PANEL));
        final JMenu funcViewMenu = new JMenu(uia(ActionInfo.CONFIGURE_FUNCTION_GRAPH));
        final JMenu ftViewMenu = new JMenu(uia(ActionInfo.CONFIGURE_FT_GRAPH));
        viewControllerWinder.addTo(winderViewMenu);
        viewControllerFunctionGraph.addTo(funcViewMenu);
        viewControllerFtGraph.addTo(ftViewMenu);

        final JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);

        final JMenu graphViewMenu = new JMenu("Graphs");
        graphViewMenu.add(funcViewMenu);
        graphViewMenu.add(ftViewMenu);
        graphViewMenu.add(winderViewMenu);
        viewMenu.add(graphViewMenu);

        viewMenu.addSeparator();
        createViewMenu(viewMenu);

        final JMenuItem resetView = new JMenuItem("Reset View");
        resetView.addActionListener(e -> resetView());
        viewMenu.addSeparator();
        viewMenu.add(resetView);

        // Music Menu
        menuBar.add(MusicPlayer.getSingleton().createPlaybackMenu());

        // Settings
        menuBar.add(Ui.createSettingsMenu(this));

        // Ui components that depend on function
        rotorDependentComps = new JComponent[] {
                rotorCountSlider,
                curRotorSlider,
                speedSlider,
        };

        // Run
        setupActionKeyBindings(getRootPane(), null, JComponent.WHEN_IN_FOCUSED_WINDOW, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ftWinderPanel.addMouseListener(this);

//        functionGraphPanel.addMouseListener(this);
//        ftGraphPanel.addMouseListener(this);

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
            functionGraphPanel.drawChart();
            setPlay(true);

            setFunctionGraphWeight(DEFAULT_WEIGHT_FUNCTION_GRAPH_PANEL, false);
            setWInderPanelWeight(DEFAULT_WEIGHT_WINDER_PANEL, false);

            setControlsVisibleInternal(DEFAULT_CONTROLS_VISIBLE);
            setMenuBarVisibleInternal(DEFAULT_MENUBAR_VISIBLE);
            setFullscreenInternal(DEFAULT_FULLSCREEN);     // sync

            requestFocusInWindow();
            update();
        });
    }

    private void syncTitle() {
        setTitle(Ui.getWindowTitle(R.TITLE_FT, ftWinderPanel.getRotorStateManager()));
    }


    @Override
    public @Nullable Component getControlsComponent() {
        return controlScrollPane;
    }

    @NotNull
    public FTWinderPanel getFtWinderPanel() {
        return ftWinderPanel;
    }

    @NotNull
    public FunctionGraphPanel getFunctionGraphPanel() {
        return functionGraphPanel;
    }

    @NotNull
    public FTGraphPanel getFtGraphPanel() {
        return ftGraphPanel;
    }



    private static float getRelativeDividerLocation(@NotNull JSplitPane pane) {
        final int loc = pane.getDividerLocation();

        final int size = (pane.getOrientation() == JSplitPane.VERTICAL_SPLIT? pane.getHeight(): pane.getWidth()) - pane.getDividerSize();
        if (size < 1) {
            return 0;
        }

        return ((float) loc) / size;
    }



    private void syncViewControllers() {
        viewControllerWinder.sync();
        viewControllerFunctionGraph.sync();
        viewControllerFtGraph.sync();
    }

    protected void onSplitPaneWeightsChanged(boolean update) {
        syncViewControllers();
        if (update) {
            update();
        }
    }

    public float getWinderPanelWeight() {
        return getRelativeDividerLocation(splitPaneWinderGraphsHorizontal);
    }

    public void setWInderPanelWeight(float weight, boolean update) {
        splitPaneWinderGraphsHorizontal.setDividerLocation(weight);
        onSplitPaneWeightsChanged(update);
    }


    public float getFunctionGraphWeight() {
        return getRelativeDividerLocation(splitPaneGraphsVertical);
    }

    public void setFunctionGraphWeight(float weight, boolean update) {
        splitPaneGraphsVertical.setDividerLocation(weight);
        onSplitPaneWeightsChanged(update);
    }

    public void resetSplitWeights(boolean update) {
        setFunctionGraphWeight(DEFAULT_WEIGHT_FUNCTION_GRAPH_PANEL, false);
        setWInderPanelWeight(DEFAULT_WEIGHT_WINDER_PANEL, false);

        if (update) {
            update();
        }
    }

    public void resetView() {
        resetSplitWeights(true);
    }




    public enum ExpandState {
        EXPANDED("Expanded"),
        COLLAPSED("Collapsed"),
        DEFAULT("Normal");

        public final String title;

        ExpandState(String title) {
            this.title = title;
        }
    }

    private static final float EXPANDED_WEIGHT_TOLERANCE = 0.05f;

    @NotNull
    public static ExpandState expandState(float weight) {
        if (weight <= EXPANDED_WEIGHT_TOLERANCE)
            return ExpandState.COLLAPSED;

        if (weight >= (1 - EXPANDED_WEIGHT_TOLERANCE))
            return ExpandState.EXPANDED;

        return ExpandState.DEFAULT;
    }


    @NotNull
    public ExpandState winderPanelExpandState() {
        return expandState(getWinderPanelWeight());
    }

    public void setWInderPanelExpanded(@NotNull ExpandState expandState) {
        final float w = switch (expandState) {
            case EXPANDED -> 1;
            case COLLAPSED -> 0;
            case DEFAULT -> DEFAULT_WEIGHT_WINDER_PANEL;
        };

        setWInderPanelWeight(w, true);
    }

    @NotNull
    public ExpandState functionGraphExpandState() {
        if (winderPanelExpandState() == ExpandState.EXPANDED)
            return ExpandState.COLLAPSED;

        return expandState(getFunctionGraphWeight());
    }

    public void setFunctionGraphExpanded(@NotNull ExpandState state) {
        final float w1, w2;

        switch (state) {
            case EXPANDED -> {
                w1 = 0; w2 = 1;
            } case COLLAPSED -> {
                w1 = -1; w2 = 0;
            } default -> {
                w1 = DEFAULT_WEIGHT_WINDER_PANEL;
                w2 = DEFAULT_WEIGHT_FUNCTION_GRAPH_PANEL;
            }
        }

        if (w1 != -1) {
            setWInderPanelWeight(w1, false);
        }

        setFunctionGraphWeight(w2, false);
        update();
    }

    @NotNull
    public ExpandState fTGraphExpandState() {
        if (winderPanelExpandState() == ExpandState.EXPANDED)
            return ExpandState.COLLAPSED;

        return expandState(1 - getFunctionGraphWeight());
    }

    public void setFTGraphExpanded(@NotNull ExpandState state) {
        final float w1, w2;

        switch (state) {
            case EXPANDED -> {
                w1 = 0; w2 = 0;
            } case COLLAPSED -> {
                w1 = -1; w2 = 1;
            } default -> {
                w1 = DEFAULT_WEIGHT_WINDER_PANEL;
                w2 = DEFAULT_WEIGHT_FUNCTION_GRAPH_PANEL;
            }
        }

        if (w1 != -1) {
            setWInderPanelWeight(w1, false);
        }

        setFunctionGraphWeight(w2, false);
        update();
    }



    private void syncRotorDependentOps(boolean canLoadRotors, boolean hasRotors) {
        final boolean hasBoth = canLoadRotors && hasRotors;
        for (JComponent c: rotorDependentComps) {
            if (c == rotorCountSlider) {
                c.setEnabled(canLoadRotors);
                continue;
            }

            c.setEnabled(hasBoth);
        }

        ActionInfo.sharedValues()
                .stream()
                .filter(a -> a.functionDependent)
                .forEach(a -> uia(a).setEnabled(a.rotorsDependent? hasBoth: canLoadRotors));
    }

    private void syncRotorDependentOps() {
        final RotorStateManager manager = ftWinderPanel.getRotorStateManager();
        syncRotorDependentOps(!manager.isNoOp(), manager.getRotorCount() > 0);
    }


    private void syncWithRotors() {
        syncRotorDependentOps();
        updateCurrentRotorUi(true);
    }


    public void setPlay(boolean play) {
        ftWinderPanel.setPlay(play);

        uia(ActionInfo.TOGGLE_PLAY_PAUSE)
                .setName(R.getPlayPauseText(play))
                .setShortDescription(R.getPlayPauseShortDescription(play))
                .setSelected(!play);
    }

    public boolean isPlaying() {
        return ftWinderPanel.isPlaying();
    }

    public boolean togglePlay() {
        return ftWinderPanel.togglePlay();
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

    private void setRotorCount(int count, boolean fromPanel) {
        if (!fromPanel) {
            ftWinderPanel.setRotorCount(count);
            count = ftWinderPanel.getConstrainedRotorCount();
        }

        updateRotorCountUi(count);
        syncWithRotors();
    }

    public void setRotorCount(int count) {
        setRotorCount(count, false);
    }




    public boolean ignoreAdjustingCurrentRotorValue() {
        return false;
    }

    private void updateCurrentRotorText(int currentRotorIndex) {
        final RotorState state = currentRotorIndex != -1? ftWinderPanel.getRotorStateManager().getRotorState(currentRotorIndex): null;
        curRotorText.setText(R.getCurrentRotorText(currentRotorIndex, state != null? state.getFrequency(): null));
    }

    private void updateCurrentRotorUi(int rotorCount, int currentRotorIndex, boolean ignoreEvent) {
        if (currentRotorIndex < 0 || currentRotorIndex >= rotorCount) {
            currentRotorIndex = -1;
        }

        final int max = rotorCount < 2 ? 0 : rotorCount - 1;
        if (curRotorSlider.getMaximum() != max) {
            if (max > 0) {
                curRotorSlider.setLabelTable(curRotorSlider.createStandardLabels(max, 0));
                curRotorSlider.setPaintLabels(true);
            } else {
                curRotorSlider.setPaintLabels(false);
                curRotorSlider.setLabelTable(EMPTY_LABEL_TABLE);
            }

            curRotorSlider.setMaximum(max);
        }

        final int toSet = currentRotorIndex != -1? currentRotorIndex : 0;
        if (curRotorSlider.getValue() != toSet) {
            if (ignoreEvent)
                ignoreNextCurrentRotorSliderEvent = true;
            curRotorSlider.setValue(toSet);
            if (ignoreEvent)
                ignoreNextCurrentRotorSliderEvent = false;
        }

        updateCurrentRotorText(currentRotorIndex);
        if (max == 0) {
            curRotorSlider.setEnabled(false);
        }
    }

    private void updateCurrentRotorUi(boolean ignoreEvent) {
        updateCurrentRotorUi(ftWinderPanel.getRotorCount(), ftWinderPanel.getCurrentRotorIndex(), ignoreEvent);
    }

    private void setCurrentRotor(int rotorIndex, boolean fromPanel) {
        if (!fromPanel) {
            ftWinderPanel.seekToRotorIndex(rotorIndex);
            rotorIndex = ftWinderPanel.getCurrentRotorIndex();
        }

//        boolean ignore = true;
//        if (curRotorSlider.getValueIsAdjusting()) {
//            rotorIndex = curRotorSlider.getValue();
//            ignore = ignoreAdjustingCurrentRotorValue() || !fromPanel;
//        } else {
//            curIndex = ftWinderPanel.getCurrentRotorIndex();
//        }

        updateCurrentRotorUi(ftWinderPanel.getRotorCount(), rotorIndex, true);
    }

    public void setCurrentRotor(int rotorIndex) {
        setCurrentRotor(rotorIndex, false);
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
            ftWinderPanel.setRotorsAnimationSpeedPercent(percent);
            percent = ftWinderPanel.getRotorsAnimationSpeedPercent();
        }

        updateSpeedUi(percent);
    }

    public final void setSpeedPercent(int percent) {
        setSpeedPercent(percent, false);
    }



    private void setRepeatMode(@Nullable AbstractAnimator.RepeatMode repeatMode, boolean fromPanel) {
        if (!fromPanel) {
            ftWinderPanel.setRepeatMode(repeatMode);
        }

        repeatMode = ftWinderPanel.getRepeatMode();
        if (repeatModeComboBox.getSelectedItem() != repeatMode) {
            repeatModeComboBox.setSelectedItem(repeatMode);
        }
    }

    public void setRepeatMode(@Nullable AbstractAnimator.RepeatMode repeatMode) {
        setRepeatMode(repeatMode, false);
    }


    public void askConfigureFrequencyProvider() {
        Ui.askConfigureFrequencyProvider(FTUi.this, ftWinderPanel.getRotorStateManager());
    }

    public void askSaveFunctionStateToFIle() {
        Ui.askSaveFunctionStateToFIle(FTUi.this, ftWinderPanel.getRotorStateManager());
    }

    public void askClearAndResetRotorStateManager(boolean reload) {
        Ui.askClearAndResetRotorStateManager(FTUi.this, ftWinderPanel.getRotorStateManager(), reload);
    }

    public void askLoadExternalRotorStatesFromCSV() {
        Ui.askLoadExternalRotorStatesFromCSV(FTUi.this, ftWinderPanel.getRotorStateManager());
    }

    public void askSaveRotorStatesToCSV() {
        Ui.askSaveRotorStatesToCSV(FTUi.this, ftWinderPanel.getRotorStateManager());
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

    public void cancelRunningTasks() {
        if (ftWinderPanel.getRotorStateManager().isLoading()) {
            ftWinderPanel.getRotorStateManager().cancelLoad(true);
            return;
        }

        ftWinderPanel.stop();
    }


    /* config menu */

    public void showConfigPopupMenu(Component component, int x, int y) {
        final JPopupMenu menu = menuTransform.getPopupMenu();
        menu.show(component, x, y);
        menu.getGraphics().dispose();
    }

    public void showConfigPopupMenu(@NotNull MouseEvent e) {
        showConfigPopupMenu(e.getComponent(), e.getX(), e.getY());
    }

    public void setShowConfigPopupMenu(boolean show) {
        if (show) {
            setMenuBarVisible(true);
        }

        menuTransform.setPopupMenuVisible(show);
//        showConfigPopupMenu(FTUi.this, getWidth() / 2, getHeight() / 2);
    }

    public void toggleConfigPopupMenu() {
        setShowConfigPopupMenu(!menuTransform.isPopupMenuVisible());
    }


    private class TransformMenu extends JMenu {

        private TransformMenu() {
            super("Transform");

            add(uia(ActionInfo.CONFIGURE_ROTOR_FREQUENCY_PROVIDER));
            addSeparator();
            add(functionGraphPanel.createMenu(uia(ActionInfo.CONFIGURE_FUNCTION_GRAPH)));
            add(ftGraphPanel.createMenu(uia(ActionInfo.CONFIGURE_FT_GRAPH)));
            add(ftWinderPanel.createMenu(uia(ActionInfo.CONFIGURE_FT_WINDER_PANEL)));
        }
    }

    @Override
    public void onLookAndFeelChanged(@NotNull String className) {
        super.onLookAndFeelChanged(className);
    }

    @Override
    public void onFTIntegrationIntervalCountChanged(int fourierTransformSimpson13NDefault) {
        super.onFTIntegrationIntervalCountChanged(fourierTransformSimpson13NDefault);
    }


    /* Mouse */

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
            case PLAY -> setPlay(true);
            case PAUSE -> setPlay(false);
            case STOP -> ftWinderPanel.stop();
            case TOGGLE_PLAY_PAUSE -> togglePlay();
            case RESET_MAIN, RESET_FULL -> ftWinderPanel.reset();
            case SAVE_FUNCTION_STATE_TO_FILE ->  askSaveFunctionStateToFIle();
            case CONFIGURE_ROTOR_FREQUENCY_PROVIDER -> askConfigureFrequencyProvider();
            case CLEAR_AND_RESET_ROTOR_STATE_MANAGER -> askClearAndResetRotorStateManager(false);
            case CLEAR_AND_RELOAD_ROTOR_STATE_MANAGER -> askClearAndResetRotorStateManager(true);
            case LOAD_EXTERNAL_ROTOR_STATES_FROM_CSV -> askLoadExternalRotorStatesFromCSV();
            case SAVE_ALL_ROTOR_STATES_TO_CSV -> askSaveRotorStatesToCSV();
            default -> {
                return false;
            }
        }

        return true;
    }



    private class PanelViewController {

        @NotNull
        private final Task<ExpandState> currentStateProvider;
        @NotNull
        private final Consumer<ExpandState> callback;

        @NotNull
        private final EnumMap<ExpandState, BaseAction> expandStateActions;

        @NotNull
        private final BaseAction presentAction;

        private PanelViewController(@NotNull Task<ExpandState> currentStateProvider, @NotNull Consumer<ExpandState> callback) {
            this.currentStateProvider = currentStateProvider;
            this.callback = callback;

            // Present
            presentAction = new BaseAction("Toggle Presentation") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    togglePresentPanel();
                }
            };

            // Expand State
            expandStateActions = new EnumMap<>(ExpandState.class);

            for (ExpandState state: ExpandState.values()) {
                expandStateActions.put(state, new BaseAction(state.title) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        callback.consume(state);
                    }
                });
            }

            sync();
        }


        @NotNull
        private JMenu addExpandStateMenu(@NotNull JMenu dest) {
            final ButtonGroup group = new ButtonGroup();

            for (Map.Entry<ExpandState, BaseAction> e: expandStateActions.entrySet()) {
                final JRadioButtonMenuItem item = new JRadioButtonMenuItem(e.getValue());
                group.add(item);
                dest.add(item);
            }

            return dest;
        }

        @NotNull
        private JMenu createExpandStateMenu() {
            return addExpandStateMenu(new JMenu("View Mode"));
        }

        private void addTo(@NotNull JMenu menu) {
            menu.add(presentAction);
            menu.addSeparator();
            addExpandStateMenu(menu);
        }

        private void addAsSeparateMenu(@NotNull JMenu menu, @NotNull String title) {
            final JMenu me = new JMenu(title);
            addTo(me);
            menu.add(me);
        }

        private void addAsSeparateViewMenu(@NotNull JMenu menu) {
            addAsSeparateMenu(menu, "View");
        }


        private void select(@NotNull ExpandState state) {
            expandStateActions.get(state).setSelected(true);
        }

        private boolean isPanelPresenting() {
            return currentStateProvider.begin() == ExpandState.EXPANDED && isPresenting();
        }

        private void setPresentPanel(boolean present) {
            if (present) {
                setPresentationModeEnabled(true);
                Async.uiPost(ExpandState.EXPANDED, callback, 5);
            } else {
                setPresentationModeEnabled(false);
                Async.uiPost(ExpandState.DEFAULT, callback, 5);
            }
        }

        private void togglePresentPanel() {
            setPresentPanel(!isPanelPresenting());
        }

        private void sync() {
            final boolean presenting = isPanelPresenting();
            if (presenting) {
                presentAction.setName("Exit Presentation");
                presentAction.setShortDescription("Go back to windowed mode");
            } else {
                presentAction.setName("Present");
                presentAction.setShortDescription("Present on fullscreen");
            }

            final ExpandState currentState = currentStateProvider.begin();
            if (currentState != null) {
                select(currentState);
            }

        }

    }
}
