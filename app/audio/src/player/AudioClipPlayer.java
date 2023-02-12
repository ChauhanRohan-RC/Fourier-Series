package player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import source.AudioSource;

import javax.sound.sampled.*;


/**
 * A player to play short sound clips<br>
 * It uses {@link Clip clip}, which <strong>LOADS WHOLE SOUND DATA IN MEMORY</strong>
 *
 * @see Clip
 * */
public class AudioClipPlayer extends AbstractLinePlayer {

    public static final String TAG = "AudioClipPlayer";

//    /**
//     * Creates Sound player for the given URL
//     *
//     * @param id id of the player
//     * @param url url of the audio file
//     *
//     * @return Sound player for the given URL
//     * @throws CreationException if there is an error creating the sound player. Use its {@link CreationException#getCause() cause}
//     * to get actual exception
//     * */
//    @NotNull
//    public static AudioClipPlayer create(long id, @NotNull URL url) throws CreationException {
//        try {
//            AudioInputStream stream = AudioSystem.getAudioInputStream(url);
//            final AudioFormat baseFormat = stream.getFormat();
//            final AudioFormat newFormat = AudioPlayer.transformAudioFormat(baseFormat);
//            if (newFormat != baseFormat) {
//                stream = AudioSystem.getAudioInputStream(newFormat, stream);
//            }
//
//            final Clip clip = AudioSystem.getClip();
//
//            return new AudioClipPlayer(id, stream, clip);
//        } catch (UnsupportedAudioFileException e) {
//            throw new CreationException("Unsupported Audio File format\nURL: " + url, e);
//        } catch (LineUnavailableException e) {
//            throw new CreationException("Sound clip could not be initialised due to system restrictions\nURL: " + url, e);
//        } catch (IOException e) {
//            throw new CreationException("I/O error in initialising Audio Stream\nURL: " + url, e);
//        } catch (IllegalArgumentException e) {
//            throw new CreationException("No installed mixer supports sound clip\nURL: " + url, e);
//        } catch (Throwable t) {
//            throw new CreationException("Unknown error in initialising Audio Clip Player\nURL: " + url, t);
//        }
//    }
//
//    @Nullable
//    public static AudioClipPlayer createNoThrow(long id, @NotNull URL url) {
//        try {
//            return create(id, url);
//        } catch (CreationException e) {
//            Log.e(TAG, e.getMessage(), e.getCause());
//        }
//
//        return null;
//    }




    @NotNull
    private final AudioSource source;

    @Nullable
    private volatile AudioInputStream mStream;
    @Nullable
    private volatile Clip mClip;

    public AudioClipPlayer(long id, @NotNull AudioSource source) {
        super(id);
        this.source = source;
    }


    @Override
    public @NotNull String logTag() {
        return TAG;
    }

    @NotNull
    @Override
    public AudioSource getSource() {
        return source;
    }

    @Override
    public boolean isStreaming() {
        return false;
    }

    @Override
    protected @Nullable DataLine getLine() {
        return mClip;
    }

    @Override
    protected @Nullable AudioFormat getFormat() {
        final AudioInputStream stream = mStream;

        if (stream != null) {
            return stream.getFormat();
        }

        return null;
    }

    @Override
    public long getFrameLength() {
        final AudioInputStream stream = mStream;
        if (stream != null) {
            return stream.getFrameLength();
        }

        final Clip clip = mClip;
        if (clip != null) {
            return clip.getFrameLength();
        }

        return AudioSystem.NOT_SPECIFIED;
    }

    @Override
    public long getMicrosecondLength() {
        final Clip clip = mClip;
        if (clip != null) {
            return clip.getMicrosecondLength();
        }

        return AudioSystem.NOT_SPECIFIED;
    }


    @Override
    protected synchronized void onStateChanged(@NotNull State old, @NotNull State newState) {

    }


    @Override
    public boolean isSeekSupported() {
        return true;
    }

    @Override
    public synchronized void setFramePosition(int frames) {
        final Clip clip = mClip;
        if (clip != null && clip.isOpen()) {
            clip.setFramePosition(frames);
        }
    }

    @Override
    public synchronized void setMicrosecondPosition(long microseconds) {
        final Clip clip = mClip;
        if (clip != null && clip.isOpen()) {
            clip.setMicrosecondPosition(microseconds);
        }
    }


    public synchronized boolean considerOpen() {
        if (isOpen())
            return true;

        try {
            final AudioInputStream stream = AudioPlayer.openAudioInputStream(source);
            mStream = stream;

            // Detach
            final Clip oldClip = mClip;
            if (oldClip != null) {
                oldClip.removeLineListener(this);
                mClip = null;
            }

            final Clip newClip = AudioSystem.getClip();
            mClip = newClip;

            newClip.addLineListener(this);
            newClip.open(stream);
            return true;
        } catch (PlayerException e) {
            onError(e);
        } catch (LineUnavailableException e) {
            onError(new PlayerException("Audio data line could not be initialised due to system restrictions\nAudio Source: " + source, e));
        } catch (IllegalArgumentException e) {
            onError(new PlayerException("No installed mixer supports sound clip\nAudio Source: " + source, e));
        } catch (Throwable t) {
            onError(new PlayerException("Unknown error in initialising Audio Player\nAudio Source: " + source, t));
        }

        return false;
    }

    @Override
    protected synchronized void doClose() throws Exception {
        final Clip clip = mClip;
        if (clip != null) {
            clip.close();
            mClip = null;
        }

        final AudioInputStream stream = mStream;
        if (stream != null) {
            stream.close();
            mStream = null;
        }
    }

    @Override
    public boolean isLoopSupported() {
        return true;
    }

    @Override
    public void setLoopPointFrames(int start, int end) throws IllegalArgumentException, UnsupportedOperationException {
        final Clip clip = mClip;
        if (clip != null && clip.isOpen()) {
            clip.setLoopPoints(start, end);
        }
    }

    @Override
    protected void onLoopCountChanged(int oldLoopCount, int loopCount) {
        super.onLoopCountChanged(oldLoopCount, loopCount);
    }
}
