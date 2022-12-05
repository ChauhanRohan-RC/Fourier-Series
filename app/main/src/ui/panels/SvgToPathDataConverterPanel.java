package ui.panels;

import app.R;
import async.Canceller;
import async.Consumer;
import async.TaskConsumer;
import misc.ChooserConfig;
import misc.FileUtil;
import misc.Format;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ui.util.Ui;
import util.PathFunctionManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class SvgToPathDataConverterPanel extends JPanel {

    private static final Dimension SIZE_FILE_ENTRY = new Dimension(300, 30);

    private static final boolean DEFAULT_PRETTY_PRINT = true;

    private static final int VERTICAL_SPACE = 10;


    private final Ui parent;

    private final JTextField srcEntry;
    private final JButton srcBrowseButton;

    private final JTextField destEntry;
    private final JButton destBrowseButton;

    private final JCheckBox prettyCheckbox;

    public SvgToPathDataConverterPanel(@NotNull Ui parent) {
        this.parent = parent;
        srcEntry = new JTextField();
        srcEntry.setPreferredSize(SIZE_FILE_ENTRY.getSize());

        srcBrowseButton = new JButton("Browse");
        srcBrowseButton.addActionListener(a -> browseSrc());

        destEntry = new JTextField();
        destEntry.setPreferredSize(SIZE_FILE_ENTRY.getSize());

        destBrowseButton = new JButton("Browse");
        destBrowseButton.addActionListener(a -> browseDest());

        prettyCheckbox = new JCheckBox("Format Nicely (RECOMMENDED)");
        prettyCheckbox.setSelected(DEFAULT_PRETTY_PRINT);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        final JPanel srcPanel = new JPanel(new BorderLayout(10, 10));
        srcPanel.setBorder(BorderFactory.createTitledBorder("Select Svg File"));
        srcPanel.add(srcBrowseButton, BorderLayout.EAST);
        srcPanel.add(srcEntry, BorderLayout.CENTER);
        addComp(srcPanel);

        final JPanel destPanel = new JPanel(new BorderLayout(10, 10));
        destPanel.setBorder(BorderFactory.createTitledBorder("Save As (Leave for default)"));
        destPanel.add(destBrowseButton, BorderLayout.EAST);
        destPanel.add(destEntry, BorderLayout.CENTER);
        addComp(destPanel);

        addComp(prettyCheckbox, 4);
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


    @Nullable
    private Path getSrc() {
        final String src = srcEntry.getText();
        if (Format.isEmpty(src))
            return null;

        return Path.of(src);
    }

    @Nullable
    private Path getSrcIfExists() {
        final Path src = getSrc();
        if (src == null || !Files.isRegularFile(src))
            return null;

        return src;
    }


    private void browseSrc() {
        final ChooserConfig config = ChooserConfig.openFileSingle()
                .setDialogTitle("Select SVG file")
                .setStartDir(R.DIR_EXTERNAL_PATH_FUNCTIONS)
                .setUseAcceptAllFIleFilter(false)
                .setFileHidingEnabled(false)
                .setChoosableFileFilters(R.SVG_FILE_FILTER)
                .setApproveButtonText("Select")
                .setApproveButtonTooltipText("Select Scalable Vector Graphics file")
                .build();

        final File[] files = config.showFIleChooser(parent.getFrame());
        if (files == null || files.length == 0 || files[0] == null)
            return;

        final File file = files[0];
        srcEntry.setText(file.getPath());
    }

    private void browseDest() {
        final Path src = getSrc();
        Path startDir = null;
        if (src != null) {
            startDir = src.getParent();
        }

        final ChooserConfig config = ChooserConfig.openFileSingle()
                .setDialogTitle("Save as")
                .setStartDir(startDir)
                .setUseAcceptAllFIleFilter(false)
                .setFileHidingEnabled(false)
                .setChoosableFileFilters(R.PATH_DATA_FILE_FILTER)
                .setApproveButtonText("Save")
                .build();

        final File[] files = config.showFIleChooser(parent.getFrame());
        if (files == null || files.length == 0 || files[0] == null)
            return;

        final File file = files[0];
        destEntry.setText(FileUtil.ensureExtension(file, R.PATH_DATA_FILE_EXTENSION).getPath());
    }

    public void showDialog(@Nullable Consumer<Path> successCallback) {
        final String dialogTitle = "Extract SVG vectors";
        final int op = JOptionPane.showConfirmDialog(parent.getFrame(), this, dialogTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (op != JOptionPane.OK_OPTION)
            return;

        final String srcStr = srcEntry.getText();
        final String destStr = destEntry.getText();
        if (Format.isEmpty(srcStr)) {
            parent.showErrorMessageDialog("No Svg Source file specified", dialogTitle);
            return;
        }
        final Path src = Path.of(srcStr);
        if (!Files.isRegularFile(src)) {
            parent.showErrorMessageDialog("Svg Source file does not exists\nFile: " + src, dialogTitle);
            return;
        }

        final Path dest;
        if (Format.isEmpty(destStr)) {
            dest = FileUtil.getNonExistingFile(Path.of(FileUtil.changeExtension(src.toString(), R.PATH_DATA_FILE_EXTENSION)));
        } else {
            dest = Path.of(destStr);
        }

        // Todo: show snackbar
        final Canceller c = PathFunctionManager.convertSvgToPathDataFileAsync(src, dest, prettyCheckbox.isSelected(), new TaskConsumer<Path>() {
            @Override
            public void onFailed(@Nullable Throwable t) {
                final String errorMsg = t == null? "Unknown": t.getClass().getSimpleName() + " -> " + t.getMessage();
                parent.showErrorMessageDialog(String.format("Failed to convert SVG\n\nSource: %s\nDestination: %s\nError: %s", src, dest, errorMsg), dialogTitle);
            }

            @Override
            public void onCancelled(@Nullable Path dataProcessedYet) {
                TaskConsumer.super.onCancelled(dataProcessedYet);
            }

            @Override
            public void consume(Path out) {
                if (out == null) {
                    onFailed(null);
                    return;
                }

                if (successCallback != null) {
                    successCallback.consume(out);
                }

                parent.showInfoMessageDialog(String.format("Successfully extracted path data from SVG\n\nSource: %s\nDestination: %s", src, out), dialogTitle);
            }
        });
    }
}
