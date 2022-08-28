package main.ui;

import main.models.function.provider.FunctionProviderI;
import main.models.function.provider.Providers;
import main.util.Log;
import main.R;
import main.models.rotor.RotorStateManager;
import main.models.Size;
import main.models.rotor.StandardRotorStateManager;
import main.models.function.ComplexDomainFunctionI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import main.util.Ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class FourierUi extends JFrame implements FourierPanel.PanelListener, Ui {

    public static final String TAG = "FourierUi";

    public static final boolean DEFAULT_FULLSCREEN = false;
    public static final boolean DEFAULT_CONTROLS_VISIBLE = true;
    public static final boolean AUTO_PLAY_ON_ROTOR_STATE_MANAGER_CHANGE = true;

    private static final Dimension MINIMUM_SIZE = new Dimension(400, 400);

    public static final int INITIAL_WIDTH = SCREEN_SIZE.width - 200;
    public static final int INITIAL_HEIGHT = SCREEN_SIZE.height - 200;

    final FourierPanel panel;
    private boolean mFullscreen = DEFAULT_FULLSCREEN;

    final JPanel controlPanel;
    final JScrollPane controlScrollPane;
    final JLabel rotorCountText;
    final JSlider rotorCountSlider;
    final JLabel speedText;
    final JSlider speedSlider;

    final JLabel functionLabel;
    final JComboBox<FunctionProviderI> functionComboBox;

    final JToggleButton playToggle;
    final JButton resetButton;
    final JButton resetScaleAndDragButton;
    final JCheckBox waveCheck;
    final JCheckBox invertXCheck;
    final JCheckBox invertYCheck;
    final JCheckBox graphInCenterCheck;
    final JCheckBox pointsJoinCheck;
    final JCheckBox hueCycleCheckBox;
    final JCheckBox autoTrackInCneterCheckBox;

    final JLabel endBehaviourLabel;
    final JComboBox<FourierPanel.EndBehaviour> endBehaviourComboBox;

    final JLabel scaleText;
    final JButton scaleIncButton;
    final JButton scaleDecButton;

    final JButton leftButton;
    final JButton rightButton;
    final JButton upButton;
    final JButton downButton;

    final JButton toggleControlsButton;
    private final JComponent[] funcDependentComps;              // Ui components that depend on function

    public FourierUi() {
        this(null, 0 /* First */);
    }

    public FourierUi(@Nullable String title, int initialProviderIndex) {
        super(title == null || title.isEmpty()? MAIN_TITLE : title);

        panel = new FourierPanel(new RotorStateManager.NoOp());

        // Function
        functionLabel = new JLabel(R.getFunctionProviderLabelText());
        functionLabel.setToolTipText(R.getFunctionProviderTooltipText());

        functionComboBox = new JComboBox<>(Providers.ALL_PROVIDERS.toArray(new FunctionProviderI[0]));
        functionComboBox.setToolTipText(R.getFunctionProviderTooltipText());

        pointsJoinCheck = new JCheckBox(R.getPointsJoiningText());
        pointsJoinCheck.setToolTipText(R.getPointsJoiningTooltipText());
        pointsJoinCheck.setSelected(panel.isPointsJoiningEnabled());

        hueCycleCheckBox = new JCheckBox(R.getHueCycleText());
        hueCycleCheckBox.setToolTipText(R.getHueCycleTooltipText());
        hueCycleCheckBox.setSelected(panel.hasColorOverrides());

        // Rotor Count
        final int rotorCount = panel.getConstrainedRotorCount();
        rotorCountText = new JLabel(R.getRotorCountText(rotorCount));

        rotorCountSlider = new JSlider(SwingConstants.HORIZONTAL, FourierPanel.MIN_ROTOR_COUNT, FourierPanel.MAX_ROTOR_COUNT, rotorCount);
        rotorCountSlider.setToolTipText(R.getRotorCountSliderTooltipText());
        rotorCountSlider.setLabelTable(rotorCountSlider.createStandardLabels(FourierPanel.MAX_ROTOR_COUNT - FourierPanel.MIN_ROTOR_COUNT, FourierPanel.MIN_ROTOR_COUNT));
        rotorCountSlider.setPaintLabels(true);

        // Speed
        final int speed = panel.getDomainTravelSpeedPercent();
        speedText = new JLabel(R.getSpeedPercentText(speed));

        speedSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, speed);
        speedSlider.setToolTipText(R.getSpeedSliderTooltipText());
        speedSlider.setLabelTable(speedSlider.createStandardLabels(25, 0));
        speedSlider.setPaintLabels(true);



        // Play/Pause Toggle
        final boolean playing = panel.isPlaying();
        playToggle = new JToggleButton(R.getPlayPauseText(playing), !playing);
        playToggle.setToolTipText(R.getPlayPauseTooltipText(playing));

        // Reset
        resetButton = new JButton(R.getResetText());
        resetButton.setToolTipText(R.getResetTooltipText());

        resetScaleAndDragButton = new JButton(R.getResetScaleAndDragText());
        resetScaleAndDragButton.setToolTipText(R.getResetScaleAndDragTooltipText());
        syncResetScaleAndDragButton();


        // Ops
        final boolean drawingWave = panel.isDrawingWave();
        waveCheck = new JCheckBox(R.getWaveToggleText(drawingWave));
        waveCheck.setToolTipText(R.getWaveToggleTooltipText(drawingWave));
        waveCheck.setSelected(drawingWave);

        graphInCenterCheck = new JCheckBox(R.getGraphInCenterText());
        graphInCenterCheck.setToolTipText(R.getGraphInCenterTooltipText());
        graphInCenterCheck.setSelected(panel.isGraphCenterEnabled());

        invertXCheck = new JCheckBox(R.getInvertXText());
        invertXCheck.setToolTipText(R.getInvertXTooltipText());
        invertXCheck.setSelected(panel.isXInverted());

        invertYCheck = new JCheckBox(R.getInvertYText());
        invertYCheck.setToolTipText(R.getInvertYTooltipText());
        invertYCheck.setSelected(panel.isYInverted());

        autoTrackInCneterCheckBox = new JCheckBox(R.getAutoTrackInCenterText());
        autoTrackInCneterCheckBox.setToolTipText(R.getAutoTrackInCenterTooltipText());
        autoTrackInCneterCheckBox.setSelected(panel.isAutoTrackInCenterEnabled());


        endBehaviourLabel = new JLabel(R.getEndBehaviourLabelText());
        endBehaviourLabel.setToolTipText(R.getEndBehaviourTooltipText());
        endBehaviourComboBox = new JComboBox<>(FourierPanel.EndBehaviour.sharedValues());
        endBehaviourComboBox.setSelectedIndex(panel.getEndBehaviour().ordinal());
        endBehaviourComboBox.setToolTipText(R.getEndBehaviourTooltipText());

        // Transforms
        scaleText = new JLabel(R.getScaleText(panel.getScale()));
        scaleIncButton = new JButton("+");
        scaleDecButton = new JButton("-");

        leftButton = new JButton("<");
        rightButton = new JButton(">");
        upButton = new JButton("\u02C4");
        downButton = new JButton("\u02C5");


        // Toggle Controls
        toggleControlsButton = new JButton(R.getToggleControlsText(DEFAULT_CONTROLS_VISIBLE));
        toggleControlsButton.setToolTipText(R.getToggleControlsTooltipText(DEFAULT_CONTROLS_VISIBLE));

        // Ui components that depend on function
        funcDependentComps = new JComponent[]{
                playToggle,
                resetButton,
                hueCycleCheckBox,
                rotorCountSlider,
                rotorCountText,
                speedSlider,
                speedText,
                scaleIncButton,
                scaleDecButton,
                scaleText,
                upButton,
                downButton,
                leftButton,
                rightButton,
                resetScaleAndDragButton
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

        final JPanel funcOpsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 2));
        funcOpsPanel.add(pointsJoinCheck);
        funcOpsPanel.add(hueCycleCheckBox);

        final JPanel mainPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        mainPanel.add(funcPanel);
        mainPanel.add(funcOpsPanel);
        controlPanel.add(mainPanel);

        final JPanel buttonsPanel = new JPanel(new GridLayout(2, 1, 2, 4));
        buttonsPanel.add(playToggle);
        buttonsPanel.add(resetButton);
        controlPanel.add(buttonsPanel);


        final JPanel endComboPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 2));
        endComboPanel.add(endBehaviourLabel);
        endComboPanel.add(endBehaviourComboBox);

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
        waveOpsPanel.add(autoTrackInCneterCheckBox);
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

//        controlPanel.add(functionComboBox);     // todo
//        controlPanel.add(hueCycleCheckBox);

        setBounds(Ui.windowBoundsCenterScreen(INITIAL_WIDTH, INITIAL_HEIGHT));
        setMinimumSize(MINIMUM_SIZE);
        setLayout(new BorderLayout(0, 0));
        add(controlScrollPane, BorderLayout.SOUTH);
        add(panel, BorderLayout.CENTER);


        // Listeners
        panel.addPanelListener(this);

        rotorCountSlider.addChangeListener(ev -> {
            final int val = rotorCountSlider.getValue();
            if (rotorCountSlider.getValueIsAdjusting()) {
                updateRotorCountText(val);
            } else {
                setRotorCount(val);
            }
        });
        speedSlider.addChangeListener(ev -> setSpeedPercent(speedSlider.getValue(), false));
        playToggle.addItemListener(e -> setPlay(!playToggle.isSelected()));
        resetButton.addActionListener(e -> reset(false));
        resetScaleAndDragButton.addActionListener(e -> resetScaleAndDrag());

        functionComboBox.addActionListener(e -> setFunctionProvider(functionComboBox.getSelectedIndex()));

        waveCheck.addItemListener(e -> setDrawingAsWave(waveCheck.isSelected()));
        invertXCheck.addItemListener(e -> setInvertX(invertXCheck.isSelected()));
        invertYCheck.addItemListener(e -> setInvertY(invertYCheck.isSelected()));
        graphInCenterCheck.addItemListener(e -> setGraphInCenter(graphInCenterCheck.isSelected()));
        pointsJoinCheck.addItemListener(e -> setPointsJoiningEnabled(pointsJoinCheck.isSelected()));
        hueCycleCheckBox.addItemListener(e -> setHueCycleEnabled(hueCycleCheckBox.isSelected()));
        autoTrackInCneterCheckBox.addItemListener(e -> setAutoTrackInCenter(autoTrackInCneterCheckBox.isSelected()));
        endBehaviourComboBox.addActionListener(e -> setEndBehaviour(FourierPanel.EndBehaviour.sharedValues()[endBehaviourComboBox.getSelectedIndex()]));
        scaleIncButton.addActionListener(e -> incrementScaleByUnit());
        scaleDecButton.addActionListener(e -> decrementScaleByUnit());

        leftButton.addActionListener(e -> panel.dragXByUnit(false));
        rightButton.addActionListener(e -> panel.dragXByUnit(true));
        upButton.addActionListener(e -> panel.dragYByUnit(false));
        downButton.addActionListener(e -> panel.dragYByUnit(true));

        toggleControlsButton.addActionListener(e -> toggleControlsVisibility());

        // Run
        updateOpsState();
        setupActionKeyBindings(getRootPane(), JComponent.WHEN_IN_FOCUSED_WINDOW, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        addMouseListener(new MouseHandler());
//        setPlay(true);

        final Image appIcon = R.createAppIcon();
        if (appIcon != null) {
            setIconImage(appIcon);
        }

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setFocusable(true);
        setVisible(true);
        setResizable(true);

        setFunctionProvider(initialProviderIndex);     // First function provider

        EventQueue.invokeLater(() -> {
            setControlsVisibleInternal(DEFAULT_CONTROLS_VISIBLE);
            setFullscreenInternal(mFullscreen);     // sync
            requestFocusInWindow();
            update();
        });
    }


    private void update() {
        revalidate();
        repaint();
    }

    protected void onFullscreenChanged(boolean fullscreen) {
        update();
    }

    public final boolean isFullscreen() {
        return mFullscreen;
    }

    private void setFullscreenInternal(boolean fullscreen) {
        getGraphicsConfiguration().getDevice().setFullScreenWindow(fullscreen? FourierUi.this: null);
    }

    public final boolean setFullscreen(boolean fullscreen) {
        if (mFullscreen == fullscreen)
            return false;

        setFullscreenInternal(fullscreen);
        mFullscreen = fullscreen;
        onFullscreenChanged(fullscreen);
        return true;
    }

    public final void toggleFullscreen() {
        setFullscreen(!mFullscreen);
    }



    protected void onControlsVisibilityChanged(boolean controlsVisible) {
        toggleControlsButton.setText(R.getToggleControlsText(controlsVisible));
        toggleControlsButton.setToolTipText(R.getToggleControlsTooltipText(controlsVisible));
        update();
    }

    public final boolean areControlsVisible() {
        return controlScrollPane.isVisible();
    }

    private void setControlsVisibleInternal(boolean visible) {
        controlScrollPane.setVisible(visible);
        onControlsVisibilityChanged(visible);
    }

    public final boolean setControlsVisible(boolean visible) {
        if (visible == areControlsVisible())
            return false;

        setControlsVisibleInternal(visible);
        return true;
    }

    public final void toggleControlsVisibility() {
        setControlsVisibleInternal(!areControlsVisible());
    }




    public void setPlay(boolean play) {
        panel.setPlay(play);

        playToggle.setToolTipText(R.getPlayPauseTooltipText(play));
        playToggle.setText(R.getPlayPauseText(play));

        if (playToggle.isSelected() == play) {
            playToggle.setSelected(!play);
        }
    }

    public boolean isPlaying() {
        return panel.isPlaying();
    }

    public boolean togglePlay() {
        return panel.togglePlay();
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

    public void setRotorCount(int count) {
        panel.setRotorCount(count);
        updateRotorCountUi(panel.getConstrainedRotorCount());
    }

    private void setSpeedPercent(int percent, boolean fromPanel) {
        if (!fromPanel) {
            panel.setDomainTravelSpeedPercentage(percent);
            percent = panel.getDomainTravelSpeedPercent();
        }

        if (speedSlider.getValue() != percent) {
            speedSlider.setValue(percent);
        }

        speedText.setText(R.getSpeedPercentText(percent));
    }

    public final void setSpeedPercent(int percent) {
        setSpeedPercent(percent, false);
    }

    private void updateOpsState() {
        final boolean graph = !panel.isDrawingWave();
        graphInCenterCheck.setEnabled(graph);
//        graphInCenterCheck.setVisible(graph);

        final boolean graphInCenter = panel.isGraphingInCenter();
        autoTrackInCneterCheckBox.setEnabled(graphInCenter);
//        autoTrackInCneterCheckBox.setVisible(graphInCenter);
    }

    public void setDrawingAsWave(boolean drawingAsWave) {
        panel.setDrawAsWave(drawingAsWave);
        waveCheck.setText(R.getWaveToggleText(drawingAsWave));
        waveCheck.setToolTipText(R.getWaveToggleTooltipText(drawingAsWave));

        updateOpsState();
        if (waveCheck.isSelected() != drawingAsWave) {
            waveCheck.setSelected(drawingAsWave);
        }
    }

    public boolean isDrawingAsWave() {
        return panel.isDrawingWave();
    }

    public boolean toggleDrawASWave() {
        final boolean wave = !isDrawingAsWave();
        setDrawingAsWave(wave);
        return wave;
    }

    public void setInvertX(boolean invertX) {
        panel.setInvertX(invertX);

        if (invertXCheck.isSelected() != invertX) {
            invertXCheck.setSelected(invertX);
        }
    }

    public void setInvertY(boolean invertY) {
        panel.setInvertY(invertY);

        if (invertYCheck.isSelected() != invertY) {
            invertYCheck.setSelected(invertY);
        }
    }

    public void setGraphInCenter(boolean graphInCenter) {
        panel.setGraphInCenter(graphInCenter);
        updateOpsState();

        if (graphInCenterCheck.isSelected() != graphInCenter) {
            graphInCenterCheck.setSelected(graphInCenter);
        }
    }

    public void setPointsJoiningEnabled(boolean enabled) {
        panel.setJoinPointsEnabled(enabled);

        if (pointsJoinCheck.isSelected() != enabled) {
            pointsJoinCheck.setSelected(enabled);
        }
    }

    public void setHueCycleEnabled(boolean enabled) {
        panel.setHueCycleEnabled(enabled);

        if (hueCycleCheckBox.isSelected() != enabled) {
            hueCycleCheckBox.setSelected(enabled);
        }
    }

    public void setAutoTrackInCenter(boolean autoTrackInCenter) {
        panel.setAutoTrackInCenter(autoTrackInCenter);

        if (autoTrackInCneterCheckBox.isSelected() != autoTrackInCenter) {
            autoTrackInCneterCheckBox.setSelected(autoTrackInCenter);
        }
    }

    public void setEndBehaviour(@NotNull FourierPanel.EndBehaviour endBehaviour) {
        panel.setEndBehaviour(endBehaviour);

        if (endBehaviourComboBox.getSelectedIndex() != endBehaviour.ordinal()) {
            endBehaviourComboBox.setSelectedIndex(endBehaviour.ordinal());
        }
    }

    @Nullable
    private FunctionProviderI mCurProvider;

    public boolean setFunctionProvider(@NotNull FunctionProviderI provider) {
        if (mCurProvider == provider)
            return true;

        Runnable post = null;
        boolean done = false;

        try {
            final ComplexDomainFunctionI func = provider.requireFunction();
            setFunction(func);
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
            post = () -> showErrorDialog(msg + "\nError Code: " + t.getMessage());

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


        // Sync combo box
        if (functionComboBox.getSelectedItem() != mCurProvider) {
            functionComboBox.setSelectedItem(mCurProvider);
        }

        if (post != null) {
            EventQueue.invokeLater(post);
        }

        return done;
    }

    public boolean setFunctionProvider(int index) {
        if (index < 0 || index >= functionComboBox.getItemCount())
            return false;

        return setFunctionProvider(functionComboBox.getItemAt(index));
    }


    public void setScale(double scale, boolean fromPanel) {
        if (!fromPanel) {
            panel.setScale(scale);
        }

        scaleText.setText(R.getScaleText(scale));
        scale = panel.getScale();
        scaleIncButton.setEnabled(scale < panel.getMaximumScale());
        scaleDecButton.setEnabled(scale > panel.getMinimumScale());
        syncResetScaleAndDragButton();
    }

    public void setScale(double scale) {
        setScale(scale, false);
    }

    public boolean incrementScaleByUnit() {
        return panel.incrementScaleByUnit();
    }

    public boolean decrementScaleByUnit() {
        return panel.decrementScaleByUnit();
    }

    public void syncResetScaleAndDragButton() {
        final boolean hasScaleOrDrag = !panel.getRotorStateManager().isNoOp() && panel.hasScaleOrDrag();
        resetScaleAndDragButton.setEnabled(hasScaleOrDrag);
        resetScaleAndDragButton.setVisible(hasScaleOrDrag);
        update();
    }


    public void reset(boolean scaleAndDrag) {
        panel.reset(scaleAndDrag);
    }

    public void resetScaleAndDrag() {
        panel.resetScaleAndDrag();
    }

    public void showMessageDialog(@NotNull Object msg, int type) {
        JOptionPane.showMessageDialog(this, msg, Ui.MAIN_TITLE, type);
    }

    public void showErrorDialog(@NotNull Object msg) {
        showMessageDialog(msg, JOptionPane.ERROR_MESSAGE);
    }

    public void showInfoDialog(@NotNull Object msg) {
        showMessageDialog(msg, JOptionPane.INFORMATION_MESSAGE);
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

    @Override
    public void onRotorCountChanged(int rotorCount) {
//        setRotorCount(rotorCount);
        updateRotorCountUi(panel.getConstrainedRotorCount());
    }


    private void setFuncDependentComponentsEnabled(boolean enabled) {
        for (JComponent c: funcDependentComps) {
            c.setEnabled(enabled);
        }
    }

    @Override
    public void onRotorStateManagerChanged(@Nullable RotorStateManager old, @NotNull RotorStateManager s) {
        final boolean good = !s.isNoOp();
        setFuncDependentComponentsEnabled(good);
        setPlay(good && AUTO_PLAY_ON_ROTOR_STATE_MANAGER_CHANGE);
    }

    @Override
    public void onRotorsLoadingChanged(boolean isLoading) {
        setFuncDependentComponentsEnabled(!isLoading);
    }

    @Override
    public void onDomainTravelSpeedChanged(int percent) {
        setSpeedPercent(percent, true);
    }

    @Override
    public void onScaleChanged(double scale) {
        setScale(scale, true);
    }

    @Override
    public void onScalePivotChanged(@Nullable Point2D scalePivot) {
        syncResetScaleAndDragButton();
    }

    @Override
    public void onDragChanged(@Nullable Size drag) {
        syncResetScaleAndDragButton();
    }


    public void setRotorStateManager(@NotNull RotorStateManager rotorStateManager) {
        panel.setRotorStateManager(rotorStateManager);
    }

    public void setFunction(@NotNull ComplexDomainFunctionI f, int initialRotorCount) {
        setRotorStateManager(new StandardRotorStateManager(f, initialRotorCount));
    }

    public void setFunction(@NotNull ComplexDomainFunctionI f) {
        setFunction(f, -1);
    }


    public final void considerDumpRotorStatesToFile() {
        final RotorStateManager sm = panel.getRotorStateManager();
        final String err;

        if (sm.isNoOp()) {
            err = "No function selected yet!";
        } else if (sm.isLoading()) {
            err = "Rotor States are still loading!";
        } else if (sm.getRotorCount() < 1) {
            err = "Nothing loaded yet!";
        } else {
            err = null;
        }

        if (R.notEmpty(err)) {          // Invalid save request
            showErrorDialog("INVALID SAVE REQUEST\nError: " + err);
            return;
        }

        sm.dumpRotorStatesToFileAsync(mCurProvider != null? mCurProvider.getDisplayTitle(): null, (Path file) -> {
            if (file == null) {         // Failed
                showErrorDialog("ROTOR STATES SAVE FAILED\nSee log for error details...");
            } else {
                showInfoDialog("Rotor States saved to\n" + file.toString());
            }
        });
    }

    public final void considerLoadRotorStatesFromFile() {
        R.ensureRotorStatesDumpDir();

        FileDialog dialog = new FileDialog(this, "Load Rotor States from file");
        dialog.setMode(FileDialog.LOAD);
        dialog.setDirectory(R.DIR_ROTOR_STATE_DUMPS.toString());
        dialog.setFile("*" + R.ROTOR_STATES_DUMP_FILE_EXT);
        dialog.setVisible(true);

        final String fileName = dialog.getFile();
        if (R.notEmpty(fileName)) {
            Path file = Path.of(dialog.getDirectory(), dialog.getFile());
            Log.d(TAG, "FIle -> " + file);

            RotorStateManager.loadFunctionFromRotorStatesFileAsync(file, fp -> {
                if (fp != null) {
                    functionComboBox.addItem(fp);
                    functionComboBox.setSelectedItem(fp);
                    showInfoDialog("Synthetic Rotor States Function loaded\nFunction: " + fp.getDisplayTitle());
                } else {
                    showErrorDialog("Failed to load Rotor States!");
                }
            });
        }
    }


    /* Actions */

    public enum Action {
        DRAG_UP(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK)),
        DRAG_DOWN(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK)),
        DRAG_LEFT(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK)),
        DRAG_RIGHT(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK)),

        SCALE_UP(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK)),
        SCALE_DOWN(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK)),

        PLAY(null),
        PAUSE(KeyStroke.getKeyStroke(KeyEvent.VK_PAUSE, 0)),
        STOP(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)),
        TOGGLE_PLAY_PAUSE(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0)),

        TOGGLE_WAVE(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0)),
        TOGGLE_GRAPH_CENTER(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0)),
        TOGGLE_AUTO_TRACK(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0)),
        RESET_MAIN(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0)),
        RESET_SCALE(null),
        RESET_SCALE_DRAG(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.SHIFT_DOWN_MASK)),
        RESET_FULL(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK)),

        TOGGLE_FULLSCREEN(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK)),
        TOGGLE_CONTROLS(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK)),

        DUMP_ROTOR_STATES_TO_FILE(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK)),
        LOAD_ROTOR_STATES_FROM_FILE(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));

        @Nullable
        public final KeyStroke keyStroke;

        Action(@Nullable KeyStroke keyStroke) {
            this.keyStroke = keyStroke;
        }
    }


    public class ActionHandler extends AbstractAction {

        @NotNull
        private final FourierUi.Action action;

        private ActionHandler(@NotNull FourierUi.Action action) {
            this.action = action;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (action) {
                case DRAG_UP -> panel.dragYByUnit(false);
                case DRAG_DOWN -> panel.dragYByUnit(true);
                case DRAG_LEFT -> panel.dragXByUnit(false);
                case DRAG_RIGHT -> panel.dragXByUnit(true);
                case SCALE_UP -> panel.incrementScaleByUnit();
                case SCALE_DOWN -> panel.decrementScaleByUnit();
                case PLAY -> panel.setPlay(true);
                case PAUSE -> panel.setPlay(false);
                case STOP -> panel.stop();
                case TOGGLE_PLAY_PAUSE -> togglePlay();
                case TOGGLE_WAVE -> panel.toggleDrawAsWave();
                case TOGGLE_GRAPH_CENTER -> panel.toggleGraphInCenter();
                case TOGGLE_AUTO_TRACK -> panel.toggleAutoTrackInCenter();
                case RESET_MAIN -> panel.reset(false);
                case RESET_SCALE -> panel.resetScale(true);
                case RESET_SCALE_DRAG -> resetScaleAndDrag();
                case RESET_FULL -> panel.reset(true);
                case TOGGLE_FULLSCREEN -> toggleFullscreen();
                case TOGGLE_CONTROLS -> toggleControlsVisibility();
                case DUMP_ROTOR_STATES_TO_FILE -> considerDumpRotorStatesToFile();
                case LOAD_ROTOR_STATES_FROM_FILE -> considerLoadRotorStatesFromFile();
            }
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


    private void setupActionKeyBindings(@NotNull Collection<InputMap> inputMaps, @NotNull ActionMap actionMap) {
        for (FourierUi.Action action: FourierUi.Action.values()) {
            if (action.keyStroke != null) {
                for (InputMap inMap: inputMaps) {
                    inMap.put(action.keyStroke, action);
                }

                actionMap.put(action, new ActionHandler(action));
            }
        }
    }

    private void setupActionKeyBindings(@NotNull JComponent component, @NotNull int... inputMapConditions) {
        final List<InputMap> maps = new LinkedList<>();
        for (int i: inputMapConditions) {
            maps.add(component.getInputMap(i));
        }

        setupActionKeyBindings(maps, component.getActionMap());
    }


}
