package ui.frames;

import animation.animator.AbstractAnimator;
import app.R;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rotor.RotorState;
import rotor.RotorStateManager;
import rotor.frequency.RotorFrequencyProviderI;
import ui.action.ActionInfo;
import ui.action.UiAction;
import ui.panels.FTGraphPanel;
import ui.panels.FTWinderPanel;
import ui.panels.FunctionGraphPanel;
import ui.util.Ui;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.util.Hashtable;

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
        }

        @Override
        public void onIsPlayingChanged(@NotNull FTWinderPanel panel, boolean playing) {
            setPlay(panel.isPlaying());
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
        public void onPointsJoiningEnabledChanged(@NotNull FTWinderPanel panel, boolean pointsJoiningEnabled) {
            setPointsJoiningEnabled(panel.isPointsJoiningEnabled());
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
    final JMenu menuView;

    private final JComponent[] rotorDependentComps;              // Ui components that depend on Rotors
    @NotNull
    private final JSplitPane splitPane;

    private boolean ignoreNextCurrentRotorSliderEvent;

    public FTUi(@NotNull RotorStateManager manager) {
        super();
        setTitle(Ui.TITLE_FT);
        looper = Ui.createLooper(null);

        ftWinderPanel = new FTWinderPanel(manager);
        functionGraphPanel = new FunctionGraphPanel(manager.getFunction(), manager.getFunctionMeta());
        ftGraphPanel = new FTGraphPanel(ftWinderPanel);

        looper.addActionListener(Ui.actionListener(ftWinderPanel));
        syncTitle();

        // controls
        pointsJoinCheck = new JCheckBox(uia(ActionInfo.TOGGLE_POINTS_JOIN).setSelected(ftWinderPanel.isPointsJoiningEnabled()));

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

        /* Menu */

        menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // Rotor States Menu
        menuRotorStates = Ui.createRotorStatesMenu(this::uia);
        menuBar.add(menuRotorStates);

        // Function State menu
        menuFunctionState = new JMenu("Function State");
        menuBar.add(menuFunctionState);
        menuFunctionState.add(uia(ActionInfo.SAVE_FUNCTION_STATE_TO_FILE));

        // Transform Menu
        menuTransform = new TransformMenu();
        menuBar.add(menuTransform);

        // View menu
        menuView = new JMenu("View");
        menuBar.add(menuView);
        menuView.add(uia(ActionInfo.TOGGLE_MENUBAR));
        menuView.add(uia(ActionInfo.TOGGLE_CONTROLS));
        menuView.addSeparator();
        menuView.add(uia(ActionInfo.TOGGLE_FULLSCREEN));

        // Settings
        menuBar.add(Ui.createSettingsMenu(this));

        // Ui components that depend on function
        rotorDependentComps = new JComponent[] {
                rotorCountSlider,
                curRotorSlider,
                speedSlider,
        };

        // others
        final Action controlUia = uia(ActionInfo.TOGGLE_CONTROLS)
                .setName(R.getToggleControlsText(DEFAULT_CONTROLS_VISIBLE))
                .setShortDescription(R.getToggleControlsShortDescription(DEFAULT_CONTROLS_VISIBLE))
                .setSelected(DEFAULT_CONTROLS_VISIBLE);

        toggleControlsButton = new JButton(controlUia);
//        configButton = new JButton(uia(ActionInfo.CONFIGURATIONS));

//         Layout
        controlPanel = new JPanel();
        controlPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
        controlScrollPane = new JScrollPane(controlPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

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

        // finalizing layout
//        final JPanel winderWrapper = new JPanel(new BorderLayout());
//        winderWrapper.add(controlScrollPane, BorderLayout.SOUTH);
//        winderWrapper.add(ftWinderPanel, BorderLayout.CENTER);
//
//        setBounds(Ui.windowBoundsCenterScreen(INITIAL_WIDTH, INITIAL_HEIGHT));
//        setMinimumSize(MINIMUM_SIZE);
//        setLayout(new GridBagLayout());
//
//        final GridBagConstraints winderGbc = new GridBagConstraints();
//        winderGbc.gridx = 0;
//        winderGbc.gridy = 0;
//        winderGbc.gridwidth = 1;
//        winderGbc.gridheight = 2;
//        winderGbc.weightx = winderGbc.weighty = 1;
//        winderGbc.fill = GridBagConstraints.BOTH;
//        add(winderWrapper, winderGbc);
//
//        final GridBagConstraints functionGraphGbc = new GridBagConstraints();
//        functionGraphGbc.gridx = 1;
//        functionGraphGbc.gridy = 0;
//        functionGraphGbc.gridwidth = 1;
//        functionGraphGbc.gridheight = 1;
//        functionGraphGbc.weightx = functionGraphGbc.weighty = 1.25;
//        functionGraphGbc.fill = GridBagConstraints.BOTH;
//        add(functionGraphPanel, functionGraphGbc);
//
//        final GridBagConstraints ftGraphGbc = new GridBagConstraints();
//        ftGraphGbc.gridx = 1;
//        ftGraphGbc.gridy = 1;
//        ftGraphGbc.gridwidth = 1;
//        ftGraphGbc.gridheight = 1;
//        ftGraphGbc.weightx = ftGraphGbc.weighty = 1.25;
//        ftGraphGbc.fill = GridBagConstraints.BOTH;
//        add(ftGraphPanel, ftGraphGbc);


//        final JPanel main = new JPanel(new GridBagLayout());
//
//        final GridBagConstraints winderGbc = new GridBagConstraints();
//        winderGbc.gridx = 0;
//        winderGbc.gridy = 0;
//        winderGbc.gridwidth = 1;
//        winderGbc.gridheight = 2;
//        winderGbc.weightx = winderGbc.weighty = 1;
//        winderGbc.fill = GridBagConstraints.BOTH;
//        main.add(ftWinderPanel, winderGbc);
//
//        final GridBagConstraints functionGraphGbc = new GridBagConstraints();
//        functionGraphGbc.gridx = 1;
//        functionGraphGbc.gridy = 0;
//        functionGraphGbc.gridwidth = 1;
//        functionGraphGbc.gridheight = 1;
//        functionGraphGbc.weightx = functionGraphGbc.weighty = 1.25;
//        functionGraphGbc.fill = GridBagConstraints.BOTH;
//        main.add(functionGraphPanel, functionGraphGbc);
//
//        final GridBagConstraints ftGraphGbc = new GridBagConstraints();
//        ftGraphGbc.gridx = 1;
//        ftGraphGbc.gridy = 1;
//        ftGraphGbc.gridwidth = 1;
//        ftGraphGbc.gridheight = 1;
//        ftGraphGbc.weightx = ftGraphGbc.weighty = 1.25;
//        ftGraphGbc.fill = GridBagConstraints.BOTH;
//        main.add(ftGraphPanel, ftGraphGbc);
//
//        setBounds(Ui.windowBoundsCenterScreen(INITIAL_WIDTH, INITIAL_HEIGHT));
//        setMinimumSize(MINIMUM_SIZE);
//        setLayout(new BorderLayout());
//        add(controlScrollPane, BorderLayout.SOUTH);
//        add(main, BorderLayout.CENTER);


        final JSplitPane splitOne = new JSplitPane(JSplitPane.VERTICAL_SPLIT, functionGraphPanel, ftGraphPanel);
        splitOne.setResizeWeight(0.5);
        splitOne.setDividerSize(3);
        splitOne.setContinuousLayout(true);

        final JSplitPane splitTwo = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, ftWinderPanel, splitOne);
        splitTwo.setResizeWeight(0.75);
        splitTwo.setDividerSize(3);
        splitTwo.setContinuousLayout(true);

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitTwo, controlScrollPane);
        splitPane.setResizeWeight(0.85);
        splitPane.setDividerSize(3);
        splitPane.setContinuousLayout(true);

//        final GridBagConstraints winderGbc = new GridBagConstraints();
//        winderGbc.gridx = 0;
//        winderGbc.gridy = 0;
//        winderGbc.gridwidth = 1;
//        winderGbc.gridheight = 2;
//        winderGbc.weightx = winderGbc.weighty = 1;
//        winderGbc.fill = GridBagConstraints.BOTH;
//        main.add(ftWinderPanel, winderGbc);
//
//        final GridBagConstraints functionGraphGbc = new GridBagConstraints();
//        functionGraphGbc.gridx = 1;
//        functionGraphGbc.gridy = 0;
//        functionGraphGbc.gridwidth = 1;
//        functionGraphGbc.gridheight = 1;
//        functionGraphGbc.weightx = functionGraphGbc.weighty = 1.25;
//        functionGraphGbc.fill = GridBagConstraints.BOTH;
//        main.add(functionGraphPanel, functionGraphGbc);
//
//        final GridBagConstraints ftGraphGbc = new GridBagConstraints();
//        ftGraphGbc.gridx = 1;
//        ftGraphGbc.gridy = 1;
//        ftGraphGbc.gridwidth = 1;
//        ftGraphGbc.gridheight = 1;
//        ftGraphGbc.weightx = ftGraphGbc.weighty = 1.25;
//        ftGraphGbc.fill = GridBagConstraints.BOTH;
//        main.add(ftGraphPanel, ftGraphGbc);

        setBounds(Ui.windowBoundsCenterScreen(INITIAL_WIDTH, INITIAL_HEIGHT));
        setMinimumSize(MINIMUM_SIZE);
        setLayout(new BorderLayout());
//        add(controlScrollPane, BorderLayout.SOUTH);
//        add(splitTwo, BorderLayout.CENTER);
        add(splitPane, BorderLayout.CENTER);

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

        // Run
        setupActionKeyBindings(getRootPane(), JComponent.WHEN_IN_FOCUSED_WINDOW, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
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

            setControlsVisibleInternal(DEFAULT_CONTROLS_VISIBLE);
            setMenuBarVisibleInternal(DEFAULT_MENUBAR_VISIBLE);
            setFullscreenInternal(DEFAULT_FULLSCREEN);     // sync
            requestFocusInWindow();
            update();
        });
    }

    private void syncTitle() {
        setTitle(Ui.getWindowTitle(Ui.TITLE_FT, ftWinderPanel.getRotorStateManager()));
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


    protected void onControlsVisibilityChanged(boolean controlsVisible) {
        uia(ActionInfo.TOGGLE_CONTROLS)
                .setName(R.getToggleControlsText(controlsVisible))
                .setShortDescription(R.getToggleControlsShortDescription(controlsVisible))
                .setSelected(controlsVisible);

        update();
        splitPane.resetToPreferredSizes();
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


    public void setPointsJoiningEnabled(boolean enabled) {
        ftWinderPanel.setJoinPointsEnabled(enabled);
        uia(ActionInfo.TOGGLE_POINTS_JOIN).setSelected(enabled);
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
    public void onAction(@NotNull UiAction action, @NotNull ActionEvent e) {
        switch (action.info) {
            case CANCEL_RUNNING_TASKS -> cancelRunningTasks();
            case PLAY -> setPlay(true);
            case PAUSE -> setPlay(false);
            case STOP -> ftWinderPanel.stop();
            case TOGGLE_PLAY_PAUSE -> togglePlay();
            case TOGGLE_POINTS_JOIN -> ftWinderPanel.togglePointsJoining();
            case RESET_MAIN, RESET_FULL -> ftWinderPanel.reset();
            case TOGGLE_FULLSCREEN -> toggleFullscreen();
            case TOGGLE_CONTROLS -> toggleControlsVisibility();
            case TOGGLE_MENUBAR -> toggleMenuBarVisible();
            case SAVE_FUNCTION_STATE_TO_FILE ->  askSaveFunctionStateToFIle();
            case CONFIGURE_ROTOR_FREQUENCY_PROVIDER -> askConfigureFrequencyProvider();
            case CLEAR_AND_RESET_ROTOR_STATE_MANAGER -> askClearAndResetRotorStateManager(false);
            case CLEAR_AND_RELOAD_ROTOR_STATE_MANAGER -> askClearAndResetRotorStateManager(true);
            case LOAD_EXTERNAL_ROTOR_STATES_FROM_CSV -> askLoadExternalRotorStatesFromCSV();
            case SAVE_ALL_ROTOR_STATES_TO_CSV -> askSaveRotorStatesToCSV();
        }
    }

}
