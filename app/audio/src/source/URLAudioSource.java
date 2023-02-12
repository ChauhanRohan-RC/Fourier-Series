package source;

import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.net.URL;

public class URLAudioSource implements AudioSource {

    @NotNull
    public static String displayName(@NotNull URL url) {
        return url.toString();
    }


    @NotNull
    private final String displayName;
    @NotNull
    private final URL url;

    public URLAudioSource(@NotNull String displayName, @NotNull URL url) {
        this.displayName = displayName;
        this.url = url;
    }

    public URLAudioSource(@NotNull URL url) {
        this(displayName(url), url);
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return displayName;
    }

    @NotNull
    public URL getUrl() {
        return url;
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o instanceof URLAudioSource uas) {
            return url.equals(uas.url);
        }

        if (o instanceof URL _url) {
            return url.equals(_url);
        }

        return false;
    }

    @Override
    public String toString() {
        return url.toString();
    }

    @Override
    public @NotNull AudioInputStream openAudioInputStream() throws Exception {
        return AudioSystem.getAudioInputStream(url);
    }
}
