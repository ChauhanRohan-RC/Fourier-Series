package ui.audio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.Log;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;


/**
 * A player to play short sound clips<br>
 * It uses {@link Clip clip}, which <strong>LOADS WHOLE SOUND DATA IN MEMORY</strong>
 *
 * @see Clip
 * */
public class AudioClipPlayer extends AbstractLinePlayer {

    public static final String TAG = "AudioClipPlayer";


    /**
     * Creates Sound player for the given URL
     *
     * @param id id of the player
     * @param url url of the audio file
     *
     * @return Sound player for the given URL
     * @throws CreationException if there is an error creating the sound player. Use its {@link CreationException#getCause() cause}
     * to get actual exception
     * */
    @NotNull
    public static AudioClipPlayer create(long id, @NotNull URL url) throws CreationException {
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(url);
            final AudioFormat baseFormat = stream.getFormat();
            final AudioFormat newFormat = AudioPlayer.transformAudioFormat(baseFormat);
            if (newFormat != baseFormat) {
                stream = AudioSystem.getAudioInputStream(newFormat, stream);
            }

            final Clip clip = AudioSystem.getClip();

            return new AudioClipPlayer(id, stream, clip);
        } catch (UnsupportedAudioFileException e) {
            throw new CreationException("Unsupported Audio File format\nURL: " + url, e);
        } catch (LineUnavailableException e) {
            throw new CreationException("Sound clip could not be initialised due to system restrictions\nURL: " + url, e);
        } catch (IOException e) {
            throw new CreationException("I/O error in initialising Audio Stream\nURL: " + url, e);
        } catch (IllegalArgumentException e) {
            throw new CreationException("No installed mixer supports sound clip\nURL: " + url, e);
        } catch (Throwable t) {
            throw new CreationException("Unknown error in initialising Audio Clip Player\nURL: " + url, t);
        }
    }

    @Nullable
    public static AudioClipPlayer createNoThrow(long id, @NotNull URL url) {
        try {
            return create(id, url);
        } catch (CreationException e) {
            Log.e(TAG, e.getMessage(), e.getCause());
        }

        return null;
    }




    @NotNull
    private final AudioInputStream stream;
    @NotNull
    private final Clip clip;

    private AudioClipPlayer(long id, @NotNull AudioInputStream stream, @NotNull Clip newClip) {
        super(id);
        this.stream = stream;
        this.clip = newClip;

        clip.addLineListener(this);
    }


    @Override
    public @NotNull String logTAG() {
        return TAG;
    }

    @Override
    protected @NotNull DataLine getLine() {
        return clip;
    }

    @Override
    protected @NotNull AudioFormat getFormat() {
        return stream.getFormat();
    }

    @Override
    protected synchronized void onStateChanged(@NotNull State old, @NotNull State newState) {

    }

    @Override
    public long getFrameLength() {
        return clip.getFrameLength();
    }

    @Override
    public long getMicrosecondLength() {
        return clip.getMicrosecondLength();
    }


    @Override
    public boolean isLoopSupported() {
        return true;
    }

    @Override
    public boolean isSeekSupported() {
        return true;
    }

    @Override
    public void setFramePosition(int frames) {
        clip.setFramePosition(frames);
    }

    @Override
    public void setMicrosecondPosition(long microseconds) {
        clip.setMicrosecondPosition(microseconds);
    }


    public synchronized void considerOpen() throws PlayerException {
        if (isOpen())
            return;

        try {
            clip.open(stream);
        } catch (Throwable t) {
            throw new PlayerException("Failed to open sound clip", t);
        }
    }


    @Override
    public void setLoopPointFrames(int start, int end) throws IllegalArgumentException, UnsupportedOperationException {
        clip.setLoopPoints(start, end);
    }

    @Override
    protected void onLoopCountChanged(int oldLoopCount, int loopCount) {
        super.onLoopCountChanged(oldLoopCount, loopCount);
    }
}
