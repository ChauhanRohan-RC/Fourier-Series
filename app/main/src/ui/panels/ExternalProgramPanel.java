package ui.panels;

import app.R;
import function.definition.ComplexDomainFunctionI;
import misc.ChooserConfig;
import misc.ExternalJava;
import misc.Format;
import misc.Log;
import org.jetbrains.annotations.Nullable;
import ui.frames.FourierUi;
import ui.util.Ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExternalProgramPanel extends JPanel {

    public static final String DEFAULT_INFO = "# Load externally defined Programmatic Functions (Java)\n\n" +
                                              "1. Select project folder (classpath)\n" +
                                              "2. Select JAVA source file (implementing " + ComplexDomainFunctionI.class.getSimpleName() + ") inside project folder\n\n" +
                                              "JAR files inside project folder will be added to classpath during compilation\n" +
                                              "For more information, see DOCUMENTATION";

    private static final Dimension ENTRY_DIMENSION = new Dimension(300, 30);

    @Nullable
    private final Component parent;
    private final JTextArea infoLabel;

    private final JPanel dirPanel;
    private final JLabel dirLabel;
    private final JTextField dirEntry;
    private final JButton dirBrowseButton;

    private final JPanel srcPanel;
    private final JLabel srcLabel;
    private final JTextField srcEntry;
    private final JButton srcBrowseButton;

    public ExternalProgramPanel(@Nullable Component parent, @Nullable String info) {
        this.parent = parent;

        dirLabel = new JLabel("   Project Folder ");
        dirEntry = new JTextField(R.DIR_EXTERNAL_PROGRAMS.toString());
        dirEntry.setPreferredSize(ENTRY_DIMENSION.getSize());
        dirBrowseButton = new JButton("Browse");

        srcLabel = new JLabel("Function Source ");
        srcEntry = new JTextField();
        srcEntry.setPreferredSize(ENTRY_DIMENSION.getSize());
        srcBrowseButton = new JButton("Browse");

        infoLabel = new JTextArea();
        infoLabel.setEditable(false);
        infoLabel.setFont(dirLabel.getFont());
        infoLabel.setLineWrap(true);
        infoLabel.setWrapStyleWord(true);

        /* Packing */
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(infoLabel);

        dirPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 4));
        dirPanel.add(dirLabel);
        dirPanel.add(dirEntry);
        dirPanel.add(dirBrowseButton);
        add(dirPanel);

        srcPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 4));
        srcPanel.add(srcLabel);
        srcPanel.add(srcEntry);
        srcPanel.add(srcBrowseButton);
        add(srcPanel);

        /* Listeners */
        dirBrowseButton.addActionListener(e -> browseDir());
        srcBrowseButton.addActionListener(e -> browseSource());

        setInfo(info);
    }

    @Nullable
    public String getInfo() {
        return infoLabel.getText();
    }

    public void setInfo(@Nullable String info) {
        if (info == null) {
            info = DEFAULT_INFO;
        }

        infoLabel.setText(info);
        infoLabel.setVisible(Format.notEmpty(info));
    }


    /* Project Directory (Classpath) */

    @Nullable
    public String getDirPath() {
        return dirEntry.getText();
    }

    @Nullable
    public Path getDir() {
        final String dirPath = getDirPath();
        return Format.notEmpty(dirPath) ? Path.of(dirPath) : null;
    }

    @Nullable
    public Path getDirIfExists() {
        final Path dir = getDir();
        return dir != null && Files.isDirectory(dir) ? dir : null;
    }

    public void setDir(@Nullable Path dir) {
        dirEntry.setText(dir != null ? dir.toString() : null);
    }

    public void browseDir() {
        R.ensureExternalProgramsDir();
        Path startDir = getDirIfExists();
        if (startDir == null) {
            startDir = R.DIR_EXTERNAL_PROGRAMS;
        }

        final ChooserConfig config = ChooserConfig.openDirSingle()
                .setDialogTitle("Browse Project Folder")
                .setStartDir(startDir)
                .setFileHidingEnabled(false)
                .setApproveButtonText("Select")
                .setApproveButtonTooltipText("Select Project Folder")
                .build();

        final File[] files = config.showFIleChooser(parent);
        if (files == null || files.length == 0 || files[0] == null)
            return;

        final File dir = files[0];
        if (!dir.isDirectory()) {
            Ui.showErrorMessageDialog(parent, "Folder \"" + dir + "\" does not exists", null);
            return;
        }

        setDir(dir.toPath());
        setRelativeSource(null);
    }

    /* Project Source File: relative to classpath */

    @Nullable
    public String getSourcePathRelative() {
        return srcEntry.getText();
    }

    @Nullable
    public Path getSourceRelative() {
        final String srcPath = getSourcePathRelative();
        return Format.notEmpty(srcPath) ? Path.of(srcPath) : null;
    }

    @Nullable
    public Path getSource() {
        final String dir = getDirPath();
        final String src = getSourcePathRelative();

        if (Format.isEmpty(dir) || Format.isEmpty(src)) {
            return null;
        }

        return Path.of(dir, src);
    }

    @Nullable
    public Path getSourceIfExists() {
        final Path src = getSource();
        return src != null && Files.isRegularFile(src) ? src : null;
    }

    public void setRelativeSource(@Nullable Path relativeSource) {
        srcEntry.setText(relativeSource != null ? relativeSource.toString() : null);
    }

    public void browseSource() {
        final Path dir = getDirIfExists();
        if (dir == null) {
            setDir(null);
            Ui.showErrorMessageDialog(parent, "Select Project Folder first", null);
            return;
        }

        final ChooserConfig config = ChooserConfig.openFileSingle()
                .setDialogTitle("Select Function Source")
                .setStartDir(dir)
                .setUseAcceptAllFIleFilter(false)
                .setChoosableFileFilters(ExternalJava.FILE_FILTER_JAVA_SOURCE)
                .setFileHidingEnabled(false)
                .setApproveButtonTooltipText("Select")
                .setApproveButtonTooltipText("Select function java source file")
                .build();

        final File[] files = config.showFIleChooser(parent);
        if (files == null || files.length == 0 || files[0] == null)
            return;

        final Path src = files[0].toPath();
        Path relSrc = null;

        String err = null;
        if (!Files.isRegularFile(src)) {
            err = "Source file does not exists";
        } else {
            if (src.startsWith(dir)) {
                try {
                    relSrc = dir.relativize(src);
                } catch (IllegalArgumentException exc) {
                    Log.e(FourierUi.TAG, "Source not inside project folder", exc);
                }
            }

            if (relSrc == null) {
                err = "Source file must be inside project folder";
            }
        }

        if (Format.notEmpty(err)) {
            Ui.showErrorMessageDialog(parent, err, null);
            return;
        }

        setRelativeSource(relSrc);
    }

    @Nullable
    public ExternalJava.Location showDialog() {
        R.ensureExternalProgramsDir();

        final int option = JOptionPane.showConfirmDialog(parent, this, "Load Programmtic Function", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {

            String err = null;
            final Path dir = getDir();
            final Path relSrc;

            if (dir == null) {
                err = "Project folder is required";
            } else if (!Files.isDirectory(dir)) {
                err = "Project Folder does not exists\nFolder: " + dir;
            } else if ((relSrc = getSourceRelative()) == null) {
                err = "No Function JAVA source selected";
            } else {
                final Path src = dir.resolve(relSrc);
                if (!Files.isRegularFile(src)) {
                    err = "Function source file does not exists\nProject Folder: " + dir + "\nSource File: " + relSrc;
                } else {
                    return new ExternalJava.Location(dir, relSrc.toString(), src);
                }
            }

            if (Format.notEmpty(err)) {
                Ui.showErrorMessageDialog(parent, err, null);
            }
        }

        return null;
    }

}
