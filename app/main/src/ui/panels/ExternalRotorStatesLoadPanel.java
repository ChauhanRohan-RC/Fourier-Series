package ui.panels;

import app.R;
import misc.ChooserConfig;
import misc.Format;
import misc.Log;
import misc.MathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rotor.FunctionState;
import rotor.RotorState;
import rotor.RotorStateManager;
import ui.util.Ui;
import async.Canceller;
import async.TaskConsumer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Loads Rotor States from a CSV file for an existing function
 *
 * @see ExternalRotorStateFunctionLoadPanel
 * */
public class ExternalRotorStatesLoadPanel extends JPanel {

    public static final String TAG = "ExternalRotorStatesLoadPanel";
    private static final Dimension ENTRY_DIMENSION = new Dimension(300, 30);

    private final Ui parent;
    private final JTextField fileEntry;
    private final JButton browseButton;
    private final JCheckBox checkBox;

    public ExternalRotorStatesLoadPanel(@NotNull Ui parent) {
        this.parent = parent;

        fileEntry = new JTextField();
        fileEntry.setPreferredSize(ENTRY_DIMENSION.getSize());

        browseButton = new JButton("Browse");
        browseButton.addActionListener(a -> browse());

        checkBox = new JCheckBox("Remove existing Rotor States");
        checkBox.setHorizontalAlignment(SwingConstants.LEFT);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        final JPanel entryPanel = new JPanel(new BorderLayout(10, 10));
        entryPanel.add(browseButton, BorderLayout.EAST);
        entryPanel.add(fileEntry, BorderLayout.CENTER);
        entryPanel.setBorder(BorderFactory.createTitledBorder("Select File to load Rotor States"));
        addComp(entryPanel);

        addComp(checkBox, 4);
    }

    private void addComp(@NotNull Component component) {
        addComp(component, 5);
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
                .setDialogTitle("Load Rotor States")
                .setStartDir(R.DIR_FUNCTION_STATE_SAVES)
                .setUseAcceptAllFIleFilter(false)
                .setFileHidingEnabled(false)
                .setChoosableFileFilters(R.EXT_ROTOR_STATES_CSV_FILE_FILTER)
                .setApproveButtonText("Load")
                .setApproveButtonTooltipText("Load Rotor States from file")
                .build();

        final File[] files = config.showFIleChooser(parent.getFrame());
        if (files == null || files.length == 0 || files[0] == null)
            return;

        final File file = files[0];
        fileEntry.setText(file.getPath());
    }


    public void showDialog(@NotNull RotorStateManager manager) {
        if (manager.isNoOp()) {
            parent.showWarnMessageDialog("No function selected yet", null);
            return;
        }

        final String dialogTitle = "Load Rotor States";
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
            public void consume(List<RotorState> data) {
                final boolean clear = checkBox.isSelected();
                final int prevCount = manager.getRotorCount();
                if (clear) {
                    manager.clearAndResetSync();
                }

                final int modCount = manager.addRotorStates(data);
                if (modCount > 0 && (clear || prevCount == 0)) {
                    manager.setRotorCountAsync(MathUtil.constraint(FourierSeriesPanel.ROTOR_COUNT_MIN, FourierSeriesPanel.ROTOR_COUNT_MAX, modCount));
                }

                final String msg = String.format("Rotor States loaded successfully\n\nFile: %s\nRotor States: %d\nPrevious States Deleted: %b", file, modCount, clear);
                parent.showInfoMessageDialog(msg, dialogTitle);
            }
        });
    }
}
