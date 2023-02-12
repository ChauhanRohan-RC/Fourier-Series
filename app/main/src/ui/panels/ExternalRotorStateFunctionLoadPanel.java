package ui.panels;

import app.R;
import async.Canceller;
import async.Consumer;
import async.TaskConsumer;
import function.RotorStatesFunction;
import misc.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import provider.FunctionMeta;
import provider.FunctionProviderI;
import provider.FunctionType;
import provider.SimpleFunctionProvider;
import rotor.FunctionState;
import rotor.RotorState;
import ui.util.Ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Loads a new {@link function.RotorStatesFunction} from CSV file
 *
 * @see ExternalRotorStatesLoadPanel
 * */
public class ExternalRotorStateFunctionLoadPanel extends JPanel {

    public static final String TAG = "ExternalRotorStateFunctionLoadPanel";

    private static final Dimension SIZE_FILE_ENTRY = new Dimension(300, 30);
    private static final Dimension SIZE_NUMBER_ENTRY = new Dimension(100, 30);
    private static final int VERTICAL_SPACE = 10;

    private final Ui parent;

    private final JLabel nameLabel;
    private final JTextField nameEntry;

    private final JLabel domainStartLabel;
    private final JTextField domainStartEntry;
    private final JLabel domainEndLabel;
    private final JTextField domainEndEntry;

    private final JTextField fileEntry;
    private final JButton browseButton;

    private final JLabel computeModeLabel;
    private final JComboBox<RotorStatesFunction.ComputeMode> computeModeComboBox;

    private final JCheckBox checkBox;

    public ExternalRotorStateFunctionLoadPanel(@NotNull Ui parent) {
        this.parent = parent;

        nameLabel = new JLabel("Function Name");
        nameLabel.setToolTipText("Enter function name or leave blank for default");
        nameEntry = new JTextField();
        nameEntry.setToolTipText(nameLabel.getToolTipText());

        domainStartLabel = new JLabel("Domain Start");
        domainStartEntry = new JTextField();
        domainStartEntry.setText(String.format("%.4f", RotorStatesFunction.DEFAULT_DOMAIN_START));
        domainStartEntry.setPreferredSize(SIZE_NUMBER_ENTRY.getSize());

        domainEndLabel = new JLabel("Domain End");
        domainEndEntry = new JTextField();
        domainEndEntry.setText(String.format("%.4f", RotorStatesFunction.DEFAULT_DOMAIN_END));
        domainEndEntry.setPreferredSize(SIZE_NUMBER_ENTRY.getSize());

        fileEntry = new JTextField();
        fileEntry.setPreferredSize(SIZE_FILE_ENTRY.getSize());

        browseButton = new JButton("Browse");
        browseButton.addActionListener(a -> browse());

        computeModeLabel = new JLabel("Function Create Mode");
        computeModeLabel.setToolTipText("Configure how frequencies are used by Inverse Fourier Transform to create function");
        computeModeComboBox = new JComboBox<>(RotorStatesFunction.ComputeMode.values());
        computeModeComboBox.setSelectedItem(RotorStatesFunction.DEFAULT_COMPUTE_MODE);
        computeModeComboBox.setToolTipText(computeModeLabel.getToolTipText());

        checkBox = new JCheckBox("Other frequencies supported");
        checkBox.setToolTipText("defines whether frequencies other than those defined in the chosen file are supported");
        checkBox.setSelected(RotorStatesFunction.DEFAULT_OTHER_FREQUENCIES_SUPPOrTED);
        checkBox.setHorizontalAlignment(SwingConstants.LEFT);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Name Entry
        final JPanel namePanel = new JPanel(new BorderLayout(10, 0));
        namePanel.add(nameLabel, BorderLayout.WEST);
        namePanel.add(nameEntry, BorderLayout.CENTER);
        namePanel.setBorder(BorderFactory.createTitledBorder("Meta Information"));
        addComp(namePanel);

        // Function Config
        final JPanel configPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        final JPanel domainEntryPanel = new JPanel(new GridLayout(1, 2));
        final JPanel domainStartPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        final JPanel domainEndPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        domainStartPanel.add(domainStartLabel);
        domainStartPanel.add(domainStartEntry);
        domainEndPanel.add(domainEndLabel);
        domainEndPanel.add(domainEndEntry);

        domainEntryPanel.add(domainStartPanel);
        domainEntryPanel.add(domainEndPanel);
        configPanel.add(domainEntryPanel);

        final JPanel computeModePanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        computeModePanel.setAlignmentX(LEFT_ALIGNMENT);
        computeModePanel.add(computeModeLabel);
        computeModePanel.add(computeModeComboBox);
        configPanel.add(computeModePanel);

        configPanel.setBorder(BorderFactory.createTitledBorder("Function Configuration"));
        configPanel.setAlignmentX(LEFT_ALIGNMENT);
        addComp(configPanel);

        // File Entry
        final JPanel fileEntryPanel = new JPanel(new BorderLayout(10, 10));
        fileEntryPanel.add(browseButton, BorderLayout.EAST);
        fileEntryPanel.add(fileEntry, BorderLayout.CENTER);

        fileEntryPanel.setBorder(BorderFactory.createTitledBorder("Select File to load Rotor States"));
        fileEntryPanel.setAlignmentX(LEFT_ALIGNMENT);
        addComp(fileEntryPanel);

        addComp(checkBox, 4);
    }

    private void addComp(@NotNull Component component) {
        addComp(component, VERTICAL_SPACE);
    }

    private void addComp(@NotNull Component component, int vgap) {
        if (component instanceof JComponent jc) {
            jc.setAlignmentX(LEFT_ALIGNMENT);
        }

        if (getComponentCount() > 0 && vgap > 0) {
            add(Box.createVerticalStrut(vgap));
        }

        add(component);
    }


    private void browse() {
        final ChooserConfig config = ChooserConfig.openFileSingle()
                .setDialogTitle("Load Rotor State Function")
                .setStartDir(R.DIR_FUNCTION_STATE_SAVES)
                .setUseAcceptAllFIleFilter(false)
                .setFileHidingEnabled(false)
                .setChoosableFileFilters(R.EXT_ROTOR_STATES_CSV_FILE_FILTER)
                .setApproveButtonText("Load")
                .setApproveButtonTooltipText("Load Rotor State Function from CSV file")
                .build();

        final File[] files = config.showFIleChooser(parent.getFrame());
        if (files == null || files.length == 0 || files[0] == null)
            return;

        final File file = files[0];
        fileEntry.setText(file.getPath());
    }


    public void showDialog(@NotNull Consumer<FunctionProviderI> successCallback) {
        final String dialogTitle = "Load Rotor State Function";
        final int op = JOptionPane.showConfirmDialog(parent.getFrame(), this, dialogTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (op != JOptionPane.OK_OPTION)
            return;

        final String path = fileEntry.getText();
        if (Format.isEmpty(path)) {
            parent.showErrorMessageDialog("No FIle Selected", dialogTitle);
            return;
        }

        final Path file = Path.of(path);
        if (!Files.isRegularFile(file)) {
            parent.showErrorMessageDialog("Selected file does not exist", dialogTitle);
            return;
        }

        // todo: show snackbar loading
        final Canceller c = FunctionState.readRotorStatesFromCSVAsync(file, new TaskConsumer<>() {
            @Override
            public void onFailed(@Nullable Throwable t) {
                Log.e(TAG, "failed to load rotor states from file: " + file, t);
                String err = "failed to load Rotor States";
                err += "\nFile: " + file;
                err += "\nError: " + (t != null ? t.getClass().getSimpleName() + " -> " + t.getMessage() : "Unknown");

                parent.showErrorMessageDialog(err, dialogTitle);
            }

            @Override
            public void onCancelled(@Nullable java.util.List<RotorState> dataProcessedYet) {
                TaskConsumer.super.onCancelled(dataProcessedYet);
            }

            @Override
            public void consume(List<RotorState> states) {
                if (CollectionUtil.isEmpty(states)) {
                    parent.showErrorMessageDialog("File does not contain any Rotor States\nFile: " + file, dialogTitle);
                    return;
                }

                Double domainStart = null;
                Double domainEnd = null;
                String warnMsg = null;

                try {
                    final String startStr = domainStartEntry.getText();
                    if (Format.notEmpty(startStr)) {
                        domainStart = Double.parseDouble(startStr);
                    }

                    final String endStr = domainEndEntry.getText();
                    if (Format.notEmpty(endStr)) {
                        domainEnd = Double.parseDouble(endStr);
                    }
                } catch (NumberFormatException numExc) {
                    warnMsg = "Domain Start and Domain End can only be Numbers. Falling back to default values...";
                    Log.e(TAG, warnMsg, numExc);
                }

                final RotorStatesFunction function = new RotorStatesFunction(domainStart, domainEnd, states);
                function.setFrequenciesExceptExplicitSupported(checkBox.isSelected());

                final Object cm = computeModeComboBox.getSelectedItem();
                if (cm instanceof RotorStatesFunction.ComputeMode computeMode) {
                    function.setComputeMode(computeMode);
                    Log.d(TAG, "Compute mode: " + computeMode);
                } else {
                    final RotorStatesFunction.ComputeMode defCm = RotorStatesFunction.DEFAULT_COMPUTE_MODE;
                    function.setComputeMode(defCm);
                    final String w = "Unknown RotorStateFunction Compute Mode: " + cm + ", falling to default mode (" + defCm + ")";
                    if (warnMsg == null) {
                        warnMsg = w;
                    } else {
                        warnMsg += '\n' + w;
                    }

                    Log.w(TAG, w);
                }

                String name = nameEntry.getText();
                if (Format.isEmpty(name)) {
                    name = FileUtil.getName(file.getFileName().toString());
                }

                final FunctionMeta meta = new FunctionMeta(
                        FunctionType.EXTERNAL_ROTOR_STATE,
                        R.createExternalRotorStateFunctionDisplayName(name),
                        null,
                        states.size(),
                        function.hasBaseFunction(),
                        states
                );

                final FunctionProviderI functionProvider = new SimpleFunctionProvider(meta, function);
                successCallback.consume(functionProvider);

                if (Format.notEmpty(warnMsg)) {
                    parent.showWarnMessageDialog(warnMsg, dialogTitle);
                }
            }
        });
    }
}

