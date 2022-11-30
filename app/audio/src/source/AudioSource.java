package source;

import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.AudioInputStream;

public interface AudioSource {

    @NotNull
    String getDisplayName();

    @NotNull
    AudioInputStream openAudioInputStream() throws Exception;

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    @Override
    String toString();
}
