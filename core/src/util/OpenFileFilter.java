package util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class OpenFileFilter extends FileFilter {

    public static final boolean DEFAULT_ALLOW_DIRS = true;

    /**
     * File extension including '.' prefix
     * */
    @NotNull
    public final String extension;
    @Nullable
    public final String description;
    private boolean mDirsAllowed = DEFAULT_ALLOW_DIRS;

    public OpenFileFilter(@NotNull String extension, @Nullable String description) {
        if (extension == null || extension.isEmpty()) {
            extension = "";     // allow all
        } else if (extension.charAt(0) != '.') {
            extension = '.' + extension;
        }

        this.extension = extension.toLowerCase();

        if ((description == null || description.isEmpty()) && !this.extension.isEmpty()) {
            description = "*" + this.extension;
        } else if (!this.extension.isEmpty()) {
            description += " (*" + this.extension + ")";
        }

        this.description = description;
    }

    public OpenFileFilter(@NotNull String extension) {
        this(extension, null);
    }

    @NotNull
    public String getExtension() {
        return extension;
    }

    public OpenFileFilter setDirAllowed(boolean dirAllowed) {
        mDirsAllowed = dirAllowed;
        return this;
    }

    public boolean areDirsAllowed() {
        return mDirsAllowed;
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory())
            return mDirsAllowed;

        return extension.isEmpty() || f.getPath().toLowerCase().endsWith(extension);
    }

    @Override
    @Nullable
    public String getDescription() {
        return description;
    }
}
