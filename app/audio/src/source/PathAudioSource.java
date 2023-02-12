package source;

import misc.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class PathAudioSource implements AudioSource {

    @NotNull
    public static String displayName(@NotNull Path path) {
        return FileUtil.getName(path.getFileName().toString());
    }



    @NotNull
    private final String displayName;
    @NotNull
    private final Path path;

    @Nullable
    private volatile AudioFileFormat mFormat;

    public PathAudioSource(@NotNull String displayName, @NotNull Path path) {
        this.displayName = displayName;
        this.path = path;
    }

    public PathAudioSource(@NotNull Path path) {
        this(displayName(path), path);
    }

    @Override
    public @NotNull String getDisplayName() {
        return displayName;
    }

    @NotNull
    public Path getPath() {
        return path;
    }


    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o instanceof PathAudioSource pas) {
            return path.equals(pas.path);
        }

        if (o instanceof Path _path) {
            return path.equals(_path);
        }

        return false;
    }

    @Override
    public String toString() {
        return path.toString();
    }


    @Override
    public @NotNull AudioInputStream openAudioInputStream() throws Exception {
        return AudioSystem.getAudioInputStream(new BufferedInputStream(Files.newInputStream(path)));
    }

}
