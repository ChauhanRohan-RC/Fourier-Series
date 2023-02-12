package ui.panels;

import app.Colors;
import misc.Format;
import misc.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rotor.RotorStateManager;
import rotor.frequency.*;
import ui.util.Ui;
import async.Function;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class FrequencyProviderSelectorPanel extends JPanel {

    public static final String TAG = "FrequencyProviderSelectorPanel";
    private static final Dimension MIN_SIZE = new Dimension(400, 260);

    private static class Entry extends JPanel {

        private static final Dimension ENTRY_DIMENSION = new Dimension(90, 25);

        final JLabel label;
        final JTextField entry;

        public Entry(String label, @Nullable String startValue) {
            this.label = new JLabel(label);
            this.entry = new JTextField(startValue);
            this.entry.setPreferredSize(ENTRY_DIMENSION.getSize());
            this.entry.setFont(this.entry.getFont().deriveFont(10.0f));

            setLayout(new FlowLayout(FlowLayout.LEADING, 5, 4));
            add(this.label);
            add(this.entry);
        }

        public void setLabel(String label) {
            this.label.setText(label);
        }

        public void setValue(String value) {
            this.entry.setText(value);
        }

        public String getValue() {
            return this.entry.getText();
        }

        @Nullable
        public <T> T getValue(@NotNull Function<String, T> creator) {
            final String text = entry.getText();

            try {
                return creator.apply(text);
            } catch (Throwable t) {
                Log.e(TAG, "failed to parse value", t);
            }

            return null;
        }

        @Nullable
        public Double getASDouble() throws NumberFormatException {
            return getValue(Double::parseDouble);
        }
    }


    private static class ItemPanel extends JPanel {

        final JRadioButton radioButton;
        final JPanel opsPanel;

        public ItemPanel(String title) {
            radioButton = new JRadioButton(title);
            radioButton.setHorizontalAlignment(SwingConstants.LEADING);
            radioButton.setFont(radioButton.getFont().deriveFont(12.0f));

            opsPanel = new JPanel();
            opsPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 1));

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            addC(radioButton);
            addC(opsPanel);

            radioButton.getModel().addItemListener(e -> sync());
        }

        private void addC(@NotNull Component component, int hgap, int vgap) {
            if (vgap > 0) {
                add(Box.createVerticalStrut(vgap));
            }

            if (hgap > 0) {
                final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
                panel.setAlignmentX(LEFT_ALIGNMENT);
                panel.add(Box.createHorizontalStrut(hgap));
                panel.add(component);
                add(panel);
            } else {
                if (component instanceof JComponent jc) {
                    jc.setAlignmentX(LEFT_ALIGNMENT);
                }

                add(component);
            }
        }

        private void addC(@NotNull Component component) {
            addC(component, 3, 1);
        }

        private void setSelected(boolean selected) {
            setOpsEnabled(selected);

            if (selected) {
                setBorder(BorderFactory.createLineBorder(Colors.ACCENT_FG_LIGHT, 1, true));
            } else {
                setBorder(null);
            }

            invalidate();
            repaint();
        }

        public void sync() {
            setSelected(radioButton.isSelected());
        }

        public void setTitle(String title) {
            radioButton.setText(title);
        }

        public String getTitle() {
            return radioButton.getText();
        }

        public void markDefault() {
            final String title = getTitle();
            if (Format.notEmpty(title)) {
                String toAdd = " (Default)";
                if (!title.endsWith(toAdd)) {
                    setTitle(title + toAdd);
                }
            }
        }

        protected void setOpsEnabled(boolean enabled) {
            opsPanel.setEnabled(enabled);
            opsPanel.setVisible(enabled);

            for (int i=0; i < opsPanel.getComponentCount(); i++) {
                opsPanel.getComponent(i).setEnabled(enabled);
            }
        }
    }

    private static class IndexFpPanel extends ItemPanel {

        final Entry indexMultiplier;

        public IndexFpPanel() {
            super("Indices");
            indexMultiplier = new Entry("Indices Multiplier", String.valueOf(IndexFrequencyProvider.DEFAULT_INDEX_MULTIPLIER));
            radioButton.setActionCommand(FP_INDEX);
            radioButton.setToolTipText("Rotor Frequency = Rotor Index * Multiplier");

            opsPanel.add(indexMultiplier);
        }
    }

    private static class CenteringFpPanel extends ItemPanel {

        final Entry freqMultiplier;

        public CenteringFpPanel() {
            super("Centering");
            freqMultiplier = new Entry("Frequency Multiplier", String.valueOf(CenteringFrequencyProvider.DEFAULT_FREQUENCY_MULTIPLIER));
            radioButton.setActionCommand(FP_CENTERING);
            radioButton.setToolTipText("Rotor Frequencies are in range (-count/2, count/2) where count = no of rotors");

            opsPanel.add(freqMultiplier);
        }
    }

    private static class FixedStartFpPanel extends ItemPanel {

        final Entry start;
        final Entry step;

        public FixedStartFpPanel() {
            super("Fixed Start");
            start = new Entry("Start Frequency", String.valueOf(FixedStartFrequencyProvider.DEFAULT_FREQUENCY_START));
            step = new Entry("Increment", String.valueOf(FixedStartFrequencyProvider.DEFAULT_FREQUENCY_STEP));
            radioButton.setActionCommand(FP_FIXED_START);
            radioButton.setToolTipText("Rotor Frequency = Start Frequency + (Rotor Index * Increment)");

            opsPanel.add(start);
            opsPanel.add(step);
        }
    }

    private static class BoundFpPanel extends ItemPanel {

        final Entry start;
        final Entry end;

        public BoundFpPanel() {
            super("Bounded");
            start = new Entry("Start Frequency", String.valueOf(BoundedFrequencyProvider.DEFAULT_START));
            end = new Entry("End Frequency", BoundedFrequencyProvider.DEFAULT_END != null? String.valueOf(BoundedFrequencyProvider.DEFAULT_END): null);
            radioButton.setActionCommand(FP_BOUNDED);
            radioButton.setToolTipText("Rotor Frequencies are in range (Start Frequency, End Frequency). Omit End frequency to use default");

            opsPanel.add(start);
            opsPanel.add(end);
        }
    }

    private static class ExplicitFpPanel extends ItemPanel {

        @NotNull
        private final ExplicitFrequencyProvider fp;

        public ExplicitFpPanel(@NotNull ExplicitFrequencyProvider fp) {
            super("Explicit (Internal)");
            this.fp = fp;
            radioButton.setActionCommand(FP_EXPLICIT);
            radioButton.setToolTipText("frequencies Internally defined by the function. At other frequencies, transform might break");

            setOpsEnabled(false);
        }
    }

    private static class FundamentalFpPanel extends ItemPanel {

        final JCheckBox centeringCheck;

        public FundamentalFpPanel() {
            super("Fundamental");
            centeringCheck = new JCheckBox("Centering");
            centeringCheck.setSelected(FundamentalFrequencyProvider.DEFAULT_CENTERING);
            centeringCheck.setToolTipText("center around Fundamental Frequency by including negative frequencies");

            radioButton.setActionCommand(FP_FUNDAMENTAL);
            radioButton.setToolTipText("Rotor Frequencies are multiples of Fundamental Frequency = 1 / Domain Range");

            opsPanel.add(centeringCheck);
        }
    }


    private static final String FP_INDEX = "index";
    private static final String FP_CENTERING = "centering";
    private static final String FP_FIXED_START = "fixed_start";
    private static final String FP_BOUNDED = "bounded";
    private static final String FP_EXPLICIT = "explicit";
    private static final String FP_FUNDAMENTAL = "fundamental";

    private static final Map<Class<? extends RotorFrequencyProviderI>, String> CLASS_TO_TYPE;

    static {
        CLASS_TO_TYPE = new HashMap<>();
        CLASS_TO_TYPE.put(IndexFrequencyProvider.class, FP_INDEX);
        CLASS_TO_TYPE.put(CenteringFrequencyProvider.class, FP_CENTERING);
        CLASS_TO_TYPE.put(FixedStartFrequencyProvider.class, FP_FIXED_START);
        CLASS_TO_TYPE.put(BoundedFrequencyProvider.class, FP_BOUNDED);
        CLASS_TO_TYPE.put(ExplicitFrequencyProvider.class, FP_EXPLICIT);
        CLASS_TO_TYPE.put(FundamentalFrequencyProvider.class, FP_FUNDAMENTAL);
    }

    @NotNull
    private static String getType(@NotNull RotorFrequencyProviderI fp) {
        final Class<?> clazz = fp.getClass();
        final String type = CLASS_TO_TYPE.get(clazz);
        if (type == null)
            throw new AssertionError("Unknown type of " + RotorFrequencyProviderI.class.getSimpleName() + ": " + clazz.getName());
        return type;
    }


    private final JScrollPane scrollPane;
    private final JPanel root;
    private final ButtonGroup radioGroup;

    private final FundamentalFpPanel fundamentalFpPanel;
    private final IndexFpPanel indexFpPanel;
    private final CenteringFpPanel centeringFpPanel;
    private final FixedStartFpPanel fixedStartFpPanel;
    private final BoundFpPanel boundFpPanel;
    @Nullable
    private final ExplicitFpPanel explicitFpPanel;

    public FrequencyProviderSelectorPanel(@NotNull RotorStateManager manager) {
        this(manager.getManagerRotorFrequencyProviderOrDefault(),
                manager.getManagerDefaultRotorFrequencyProvider(),
                manager.getFunction().getExplicitFrequencyProvider());
    }

    public FrequencyProviderSelectorPanel(@Nullable RotorFrequencyProviderI currentProvider,
                                          @NotNull RotorFrequencyProviderI defaultProvider) {
        this(currentProvider, defaultProvider, null);
    }

    public FrequencyProviderSelectorPanel(@Nullable RotorFrequencyProviderI currentProvider,
                                          @NotNull RotorFrequencyProviderI defaultProvider,
                                          @Nullable ExplicitFrequencyProvider explicitProvider) {

        if (explicitProvider == null) {
            if (currentProvider instanceof ExplicitFrequencyProvider efp) {
                explicitProvider = efp;
            } else if (defaultProvider instanceof ExplicitFrequencyProvider efp) {
                explicitProvider = efp;
            }
        }

        fundamentalFpPanel = new FundamentalFpPanel();
        indexFpPanel = new IndexFpPanel();
        centeringFpPanel = new CenteringFpPanel();
        fixedStartFpPanel = new FixedStartFpPanel();
        boundFpPanel = new BoundFpPanel();
        if (explicitProvider != null) {
            explicitFpPanel = new ExplicitFpPanel(explicitProvider);
        } else {
            explicitFpPanel = null;
        }

        radioGroup = new ButtonGroup();
        radioGroup.add(fundamentalFpPanel.radioButton);
        radioGroup.add(indexFpPanel.radioButton);
        radioGroup.add(centeringFpPanel.radioButton);
        radioGroup.add(fixedStartFpPanel.radioButton);
        radioGroup.add(boundFpPanel.radioButton);
        if (explicitFpPanel != null) {
            radioGroup.add(explicitFpPanel.radioButton);
        }

        root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(null);
        addToRoot(fundamentalFpPanel);
        addToRoot(indexFpPanel);
        addToRoot(centeringFpPanel);
        addToRoot(fixedStartFpPanel);
        addToRoot(boundFpPanel);
        if (explicitFpPanel != null) {
            addToRoot(explicitFpPanel);
        }

        scrollPane = new JScrollPane(root, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(MIN_SIZE.getSize());
        scrollPane.setBorder(null);
        scrollPane.setViewportBorder(null);
        setLayout(new GridBagLayout());

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.PAGE_START;
        add(scrollPane, gbc);

        final String defaultType = getType(defaultProvider);
        final String curType;
        RotorFrequencyProviderI fp = currentProvider;
        if (fp == null) {
            fp = defaultProvider;
            curType = defaultType;
        } else {
            curType = getType(fp);
        }

        getItemPanel(defaultType).markDefault();
        getItemPanel(curType).radioButton.setSelected(true);
        sync(fp, curType);

        if (!defaultType.equals(curType)) {
            sync(defaultProvider, defaultType);
        }

        fundamentalFpPanel.sync();
        indexFpPanel.sync();
        centeringFpPanel.sync();
        fixedStartFpPanel.sync();
        boundFpPanel.sync();
        if (explicitFpPanel != null) {
            explicitFpPanel.sync();
        }

        Ui.extractDialog(scrollPane, d -> {
            d.setResizable(true);
            d.setMinimumSize(MIN_SIZE.getSize());
        }, false);
    }

    private void addToRoot(@NotNull Component component, int vgap) {
        if (vgap > 0 && root.getComponentCount() > 0) {
            root.add(Box.createVerticalStrut(vgap));
        }

        if (component instanceof JComponent jc) {
            jc.setAlignmentX(LEFT_ALIGNMENT);
        }

        root.add(component);
    }

    private void addToRoot(@NotNull Component component) {
        addToRoot(component, 4);
    }


    private void sync(@NotNull RotorFrequencyProviderI fp) {
        sync(fp, getType(fp));
    }

    private void sync(@NotNull RotorFrequencyProviderI fp, @NotNull String type) {
        switch (type) {
            case FP_INDEX -> indexFpPanel.indexMultiplier.setValue(String.valueOf(((IndexFrequencyProvider) fp).getIndexMultiplier()));
            case FP_CENTERING -> centeringFpPanel.freqMultiplier.setValue(String.valueOf(((CenteringFrequencyProvider) fp).getFrequencyMultiplier()));
            case FP_FIXED_START -> {
                final FixedStartFrequencyProvider freq = (FixedStartFrequencyProvider) fp;
                fixedStartFpPanel.start.setValue(String.valueOf(freq.getStartFrequency()));
                fixedStartFpPanel.step.setValue(String.valueOf(freq.getFrequencyStep()));
            } case FP_BOUNDED -> {
                final BoundedFrequencyProvider freq = (BoundedFrequencyProvider) fp;
                boundFpPanel.start.setValue(String.valueOf(freq.getFrequencyStart()));

                final Double end = freq.getFrequencyEnd();
                boundFpPanel.end.setValue(end != null? String.valueOf(end): null);
            } case FP_EXPLICIT -> {
            } case FP_FUNDAMENTAL -> fundamentalFpPanel.centeringCheck.setSelected(((FundamentalFrequencyProvider) fp).isCentering());
        }
    }

    @NotNull
    private ItemPanel getItemPanel(@NotNull String type) {
        return switch (type) {
            case FP_FUNDAMENTAL -> fundamentalFpPanel;
            case FP_INDEX -> indexFpPanel;
            case FP_CENTERING -> centeringFpPanel;
            case FP_FIXED_START -> fixedStartFpPanel;
            case FP_BOUNDED -> boundFpPanel;
            case FP_EXPLICIT -> {
                if (explicitFpPanel != null) {
                    yield explicitFpPanel;
                }

                throw new AssertionError("No Explicit Frequency Provider defined");
            }

            default -> throw new AssertionError("Unknown Frequency Provider type: " + type);
        };
    }

    @Nullable
    public RotorFrequencyProviderI showDialog(@Nullable Component parent, @NotNull RotorStateManager manager, boolean setProvider) {
        final String title = "Configure Frequency Provider";

        if (manager.isNoOp()) {
            Ui.showErrorMessageDialog(parent,"No function selected yet\nSelect a function to configure frequency provider", title);
            return null;
        }

        final int option = JOptionPane.showConfirmDialog(parent, this, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) {
            return null;
        }

        final String type = radioGroup.getSelection().getActionCommand();
        if (Format.isEmpty(type))
            return null;

        String err = null;
        String warn = null;
        RotorFrequencyProviderI frequencyProvider = null;

        try {
            switch (type) {
                case FP_INDEX -> {
                    final String value = indexFpPanel.indexMultiplier.getValue();
                    Double multiplier = null;
                    if (Format.notEmpty(value)) {
                        multiplier = Double.parseDouble(value);
                    }

                    final IndexFrequencyProvider fp = new IndexFrequencyProvider();
                    if (multiplier != null) {
                        if (multiplier == 0) {
                            warn = "Index multiplier cannot be 0. Falling back to default value";
                        } else {
                            fp.setIndexMultiplier(multiplier);
                        }
                    }

                    frequencyProvider = fp;
                }  case FP_CENTERING -> {
                    final String value = centeringFpPanel.freqMultiplier.getValue();
                    Double multiplier = null;
                    if (Format.notEmpty(value)) {
                        multiplier = Double.parseDouble(value);
                    }

                    final CenteringFrequencyProvider fp = new CenteringFrequencyProvider();
                    if (multiplier != null) {
                        if (multiplier == 0) {
                            warn = "Frequency multiplier cannot be 0. Falling back to default value";
                        } else {
                            fp.setFrequencyMultiplier(multiplier);
                        }
                    }

                    frequencyProvider = fp;
                } case FP_FIXED_START -> {
                    final String startStr = fixedStartFpPanel.start.getValue();
                    Double start = null;
                    if (Format.notEmpty(startStr)) {
                        start = Double.parseDouble(startStr);
                    }

                    final String stepStr = fixedStartFpPanel.step.getValue();
                    Double step = null;
                    if (Format.notEmpty(stepStr)) {
                        step = Double.parseDouble(stepStr);
                        if (step == 0) {
                            warn = "Frequency Step cannot be 0. Falling back to default value";
                            step = null;
                        }
                    }

                    final FixedStartFrequencyProvider fp = new FixedStartFrequencyProvider();
                    if (start != null) {
                        fp.setStartFrequency(start);
                    }

                    if (step != null) {
                        fp.setFrequencyStep(step);
                    }

                    frequencyProvider = fp;
                } case FP_BOUNDED -> {
                    final String startStr = boundFpPanel.start.getValue();
                    final double start;
                    if (Format.notEmpty(startStr)) {
                        start = Double.parseDouble(startStr);
                    } else {
                        start = BoundedFrequencyProvider.DEFAULT_START;
                    }

                    final String endStr = boundFpPanel.end.getValue();
                    Double end = null;
                    if (Format.notEmpty(endStr)) {
                        end = Double.parseDouble(endStr);
                        if (end.equals(start)) {
                            warn = "Start and End frequencies cannot be same. Falling back to default End Frequency";
                            end = null;
                        }
                    }

                    final BoundedFrequencyProvider fp = new BoundedFrequencyProvider();
                    fp.setFrequencyStart(start);
                    fp.setFrequencyEnd(end);
                    frequencyProvider = fp;
                } case FP_EXPLICIT -> {
                    if (explicitFpPanel == null) {
                        throw new AssertionError("No Explicit Frequency Provider defined!");
                    }

                    frequencyProvider = explicitFpPanel.fp;
                } case FP_FUNDAMENTAL -> {
                    final boolean centering = fundamentalFpPanel.centeringCheck.isSelected();
                    final FundamentalFrequencyProvider fp = new FundamentalFrequencyProvider(manager.getFunction().getDomainRange());
                    fp.setCentering(centering);
                    frequencyProvider = fp;
                }

                default -> throw new AssertionError("Invalid Frequency Provider Type: " + type);
            }
        } catch (NumberFormatException numExc) {
            Log.e(TAG, "Invalid input", numExc);
            err = "Invalid Input Format";
        } catch (Throwable t) {
            Log.e(TAG, "Error in creating frequency provider", t);
            err = "Failed to configure Frequency Provider\nError: " + t.getClass().getSimpleName() + " -> " + t.getMessage();
        }

        if (frequencyProvider == null) {
            if (Format.isEmpty(err)) {
                err = "Failed to configure Frequency Provider\nError: Unknown";
            }

            Ui.showErrorMessageDialog(parent, err, title);
            return null;
        }

        if (setProvider) {
            manager.setRotorFrequencyProvider(frequencyProvider);
        }

        if (Format.notEmpty(warn)) {
            final String finalWarn = warn;
            EventQueue.invokeLater(() -> Ui.showWarnMessageDialog(parent, finalWarn, title));
        }

        return frequencyProvider;
    }

}
