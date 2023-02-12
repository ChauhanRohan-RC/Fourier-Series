package misc;

import models.OpenFileFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Configurations for {@link JFileChooser}
 * <br><br>
 * USAGE
 * <br>
 * {@link #createFIleChooser()}<br>
 * {@link #showFIleChooser(Component)}
 * */
public class ChooserConfig {

    public static final boolean USE_DEFAULT_TITLE = true;

    @Nullable
    public static final String DEFAULT_TITLE_FILE_SINGLE = "Select FIle";
    @Nullable
    public static final String DEFAULT_TITLE_FILE_MULTIPLE = "Select Files";

    @Nullable
    public static final String DEFAULT_TITLE_DIR_SINGLE = "Select Folder";
    @Nullable
    public static final String DEFAULT_TITLE_DIR_MULTIPLE = "Select Folders";

    @Nullable
    public static final String DEFAULT_TITLE_FILE_AND_DIR_SINGLE = "Select Folder or File";
    @Nullable
    public static final String DEFAULT_TITLE_FILE_AND_DIR_MULTIPLE = "Select Folders and Files";


    @Nullable
    private String dialogTitle;

    /**
     * Dialog type, one of
     * <br>
     * {@link JFileChooser#OPEN_DIALOG}
     * {@link JFileChooser#SAVE_DIALOG}
     * {@link JFileChooser#CUSTOM_DIALOG}
     * */
    @Nullable
    private Integer dialogType;

    @Nullable
    private File startDir;

    /**
     * Selection Mode, one of
     * <br>
     * {@link JFileChooser#FILES_ONLY}
     * {@link JFileChooser#DIRECTORIES_ONLY}
     * {@link JFileChooser#FILES_AND_DIRECTORIES}
     * */
    @Nullable
    private Integer fileSelectionMode;
    @Nullable
    private Boolean multipleSelectionEnabled;

    @Nullable
    private Boolean useAcceptAllFIleFilter;
    @Nullable
    private FileFilter fileFilter;
    @Nullable
    private Collection<? extends FileFilter> choosableFileFilters;
    @Nullable
    private Boolean fileHidingEnabled;

    @Nullable
    private String approveButtonText;
    @Nullable
    private String approveButtonTooltipText;



    public static class Builder {

        @NotNull
        private final ChooserConfig config;

        public Builder() {
            config = new ChooserConfig();
        }

        private Builder(@NotNull ChooserConfig src) {
            config = new ChooserConfig(src);
        }

        @NotNull
        public ChooserConfig build() {
            return new ChooserConfig(config);
        }

        public Builder setDialogTitle(@Nullable String title) {
            config.dialogTitle = title;
            return this;
        }

        public Builder setDialogType(@Nullable Integer type) {
            config.dialogType = type;
            return this;
        }

        public Builder setStartDir(@Nullable File startDir) {
            config.startDir = startDir;
            return this;
        }

        public Builder setStartDir(@Nullable Path startDir) {
            return setStartDir(startDir != null? startDir.toFile(): null);
        }

        public Builder setFIleSelectionMode(Integer fIleSelectionMode) {
            config.fileSelectionMode = fIleSelectionMode;
            return this;
        }

        public Builder setMultipleSelectionEnabled(Boolean multipleSelectionEnabled) {
            config.multipleSelectionEnabled = multipleSelectionEnabled;
            return this;
        }

        public Builder setUseAcceptAllFIleFilter(Boolean useAcceptAllFIleFilter) {
            config.useAcceptAllFIleFilter = useAcceptAllFIleFilter;
            return this;
        }

        public Builder setFIleFilter(@Nullable FileFilter fileFilter) {
            config.fileFilter = fileFilter;
            return this;
        }

        public Builder setChoosableFileFilters(@Nullable Collection<FileFilter> fileFilters) {
            config.choosableFileFilters =  fileFilters;
            return this;
        }

        public Builder setChoosableFileFilters(FileFilter @Nullable ... fileFilters) {
            return setChoosableFileFilters(fileFilters != null? List.of(fileFilters): null);
        }

        public Builder setFileHidingEnabled(@Nullable Boolean fileHidingEnabled) {
            config.fileHidingEnabled = fileHidingEnabled;
            return this;
        }

        public Builder setApproveButtonText(@Nullable String approveButtonText) {
            config.approveButtonText = approveButtonText;
            return this;
        }

        public Builder setApproveButtonTooltipText(@Nullable String approveButtonTooltipText) {
            config.approveButtonTooltipText = approveButtonTooltipText;
            return this;
        }
    }


    private ChooserConfig() {
    }

    private ChooserConfig(@NotNull ChooserConfig src) {
        dialogTitle = src.dialogTitle;
        dialogType = src.dialogType;
        startDir = src.startDir;
        fileSelectionMode = src.fileSelectionMode;
        multipleSelectionEnabled = src.multipleSelectionEnabled;
        useAcceptAllFIleFilter = src.useAcceptAllFIleFilter;
        fileHidingEnabled = src.fileHidingEnabled;
        choosableFileFilters = src.choosableFileFilters != null? new ArrayList<>(src.choosableFileFilters): null;
        approveButtonText = src.approveButtonText;
        approveButtonTooltipText = src.approveButtonTooltipText;
    }

    @NotNull
    public ChooserConfig.Builder buildUpon() {
        return new Builder(this);
    }

    @Nullable
    public String getDialogTitle() {
        return dialogTitle;
    }

    @Nullable
    public Integer getDialogType() {
        return dialogType;
    }

    @Nullable
    public File getStartDir() {
        return startDir;
    }

    @Nullable
    public Integer getFileSelectionMode() {
        return fileSelectionMode;
    }


    @Nullable
    public Boolean getMultipleSelectionEnabled() {
        return multipleSelectionEnabled;
    }


    @Nullable
    public Boolean getUseAcceptAllFIleFilter() {
        return useAcceptAllFIleFilter;
    }

    @Nullable
    public FileFilter getFileFilter() {
        return fileFilter;
    }

    @Nullable
    public Collection<? extends FileFilter> getChoosableFileFilters() {
        return choosableFileFilters;
    }

    @Nullable
    public Boolean getFileHidingEnabled() {
        return fileHidingEnabled;
    }

    @Nullable
    public String getApproveButtonText() {
        return approveButtonText;
    }

    @Nullable
    public String getApproveButtonTooltipText() {
        return approveButtonTooltipText;
    }



    /* Defaults */

    public int getFileSelectionModeOrDefault() {
        return fileSelectionMode != null? fileSelectionMode: JFileChooser.FILES_ONLY;
    }

    public boolean getMultipleSelectionEnabledOrDefault() {
        return Boolean.TRUE.equals(multipleSelectionEnabled);
    }

    @Nullable
    public String getDialogTitleOrDefault() {
        String title = dialogTitle;
        if (title == null && USE_DEFAULT_TITLE) {
            final int selectionMode = getFileSelectionModeOrDefault();
            final boolean multi = getMultipleSelectionEnabledOrDefault();

            title = switch (selectionMode) {
                case JFileChooser.DIRECTORIES_ONLY -> multi? DEFAULT_TITLE_DIR_MULTIPLE: DEFAULT_TITLE_DIR_SINGLE;
                case JFileChooser.FILES_AND_DIRECTORIES -> multi? DEFAULT_TITLE_FILE_AND_DIR_MULTIPLE: DEFAULT_TITLE_FILE_AND_DIR_SINGLE;
                default -> multi? DEFAULT_TITLE_FILE_MULTIPLE: DEFAULT_TITLE_FILE_SINGLE;
            };
        }

        return title;
    }



    /* .....................................  File Chooser ................................... */

    @NotNull
    public JFileChooser createFIleChooser() {
        return createFileChooser(this);
    }

    @Nullable
    public File[] showFIleChooser(@Nullable Component parent) {
        return showFileChooser(parent, this);
    }

    @NotNull
    public static JFileChooser createFileChooser(@NotNull ChooserConfig config) {
        final boolean multiSelectionEnabled = config.getMultipleSelectionEnabledOrDefault();

        final JFileChooser chooser = new JFileChooser(config.getStartDir());
        chooser.setDialogTitle(config.getDialogTitleOrDefault());
        chooser.setFileSelectionMode(config.getFileSelectionModeOrDefault());
        chooser.setMultiSelectionEnabled(multiSelectionEnabled);

        final Integer type = config.getDialogType();
        if (type != null) {
            chooser.setDialogType(type);
        }

        final Boolean useAllFIleFilter = config.getUseAcceptAllFIleFilter();
        if (useAllFIleFilter != null) {
            chooser.setAcceptAllFileFilterUsed(useAllFIleFilter);
        }

        final Boolean fileHidingEnabled = config.getFileHidingEnabled();
        if (fileHidingEnabled != null) {
            chooser.setFileHidingEnabled(fileHidingEnabled);
        }

        final Collection<? extends FileFilter> fileFilters = config.getChoosableFileFilters();
        if (fileFilters != null) {
            fileFilters.forEach(chooser::addChoosableFileFilter);
        }

        final FileFilter fileFilter = config.getFileFilter();
        if (fileFilter != null) {
            chooser.setFileFilter(fileFilter);
        }

        final String approveButtonText = config.getApproveButtonText();
        if (approveButtonText != null) {
            chooser.setApproveButtonText(approveButtonText);
        }

        final String approveButtonTooltipText = config.getApproveButtonTooltipText();
        if (approveButtonTooltipText != null) {
            chooser.setApproveButtonToolTipText(approveButtonTooltipText);
        }

        return chooser;
    }

    @Nullable
    public static File[] showFileChooser(@Nullable Component parent, @NotNull ChooserConfig config) {
        final JFileChooser chooser = createFileChooser(config);

        final int option = chooser.showDialog(parent, null);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (chooser.isMultiSelectionEnabled()) {
                final File[] files =  chooser.getSelectedFiles();
                return files == null || files.length == 0? null: files;
            }

            File file = chooser.getSelectedFile();
            if (file == null)
                return null;

            final FileFilter filter = chooser.getFileFilter();
            if (filter instanceof OpenFileFilter open) {
                file = FileUtil.ensureExtension(file, open.getExtension(true));
            }

            return new File[] { file };
        }

        return null;
    }


    /* ..............................  Factory  ............................... */

    @NotNull
    public static ChooserConfig.Builder openDir(boolean multiple) {
        return new Builder()
                .setDialogType(JFileChooser.OPEN_DIALOG)
                .setFIleSelectionMode(JFileChooser.DIRECTORIES_ONLY)
                .setMultipleSelectionEnabled(multiple)
                .setUseAcceptAllFIleFilter(false);
    }

    @NotNull
    public static ChooserConfig.Builder openDirSingle() {
        return openDir(false);
    }

    @NotNull
    public static ChooserConfig.Builder openFile(boolean multiple) {
        return new Builder()
                .setDialogType(JFileChooser.OPEN_DIALOG)
                .setFIleSelectionMode(JFileChooser.FILES_ONLY)
                .setMultipleSelectionEnabled(multiple);
    }

    @NotNull
    public static ChooserConfig.Builder openFileSingle() {
        return openFile(false);
    }

    @NotNull
    public static ChooserConfig.Builder saveFile(boolean multiple) {
        return new Builder()
                .setDialogType(JFileChooser.SAVE_DIALOG)
                .setFIleSelectionMode(JFileChooser.FILES_ONLY)
                .setMultipleSelectionEnabled(multiple);
    }

    @NotNull
    public static ChooserConfig.Builder saveFileSingle() {
        return saveFile(false);
    }
}
