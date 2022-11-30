package source;

import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class StreamAudioSource implements AudioSource {

    @NotNull
    private final String displayName;
    @NotNull
    private final InputStream in;

    public StreamAudioSource(@NotNull String displayName, @NotNull InputStream in) {
        this.displayName = displayName;
        this.in = in;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return displayName;
    }

    @NotNull
    public InputStream getInputStream() {
        return in;
    }

    @Override
    public int hashCode() {
        return in.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o instanceof StreamAudioSource sas) {
            return in.equals(sas.in);
        }

        if (o instanceof InputStream _in) {
            return in.equals(_in);
        }

        return false;
    }

    @Override
    public String toString() {
        return in.toString();
    }

    @Override
    public @NotNull AudioInputStream openAudioInputStream() throws Exception {
        return AudioSystem.getAudioInputStream(new BufferedInputStream(in));
    }

}
