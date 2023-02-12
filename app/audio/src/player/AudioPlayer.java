package player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import source.AudioSource;
import live.ListenersI;

import javax.sound.sampled.*;
import java.io.IOException;

public interface AudioPlayer extends AutoCloseable, ListenersI<AudioPlayer.Listener> {

    class PlayerException extends RuntimeException {

        public PlayerException(String message, @NotNull Throwable cause) {
            super(message, cause);
        }
    }


    @NotNull
    static AudioFormat createMpegAudioFormat(@NotNull AudioFormat base) {
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                base.getSampleRate(),
                16,
                base.getChannels(),
                base.getChannels() * 2,
                base.getSampleRate(),
                false);
    }

    @NotNull
    static AudioFormat transformAudioFormat(@NotNull AudioFormat base) {
        final AudioFormat.Encoding enc = base.getEncoding();
        if (enc != null && enc.toString().startsWith("MPEG")) {
            return createMpegAudioFormat(base);
        }

        return base;
    }

    @NotNull
    static AudioFileFormat transformAudioFileFormat(@NotNull AudioFileFormat format) {
        final AudioFormat af = format.getFormat();
        if (af == null)
            return format;

        final AudioFormat newAf = transformAudioFormat(af);
        if (af == newAf || af.matches(newAf))
            return format;

        return new AudioFileFormat(format.getType(), newAf, format.getFrameLength(), format.properties());
    }

    @NotNull
    static AudioInputStream openAudioInputStream(@NotNull AudioSource source) throws PlayerException {
        try {
            AudioInputStream stream = source.openAudioInputStream();
            final AudioFormat baseFormat = stream.getFormat();
            final AudioFormat newFormat = transformAudioFormat(baseFormat);
            if (newFormat != baseFormat) {
                stream = AudioSystem.getAudioInputStream(newFormat, stream);
            }

            return stream;
        } catch (UnsupportedAudioFileException e) {
            throw new PlayerException("Unsupported Audio File format\nAudio Source: " + source, e);
        } catch (IOException e) {
            throw new PlayerException("I/O error in initialising Audio Stream\nAudio Source: " + source, e);
        } catch (Throwable t) {
            throw new PlayerException("Unknown error in loading audio\nAudio Source: " + source, t);
        }
    }


    int LOOP_CONTINUOUSLY = -1;


    enum State {

        PLAYING,
        PAUSED,
        STOPPED,
        ENDED,
        OPEN,

        ERROR,
        CLOSING,
        CLOSED,
        IDLE;

        public boolean isAtLeast(@NotNull State state) {
            return ordinal() <= state.ordinal();
        }
    }

    interface Listener {
        void onPlayerStateChanged(@NotNull AudioPlayer player, @NotNull State old, @NotNull State state);
    }


    @NotNull
    default String logTag() {
        return getClass().getSimpleName();
    }

    long getId();

    @NotNull
    AudioSource getSource();

    boolean isStreaming();

    State getState();

    @Nullable
    Object getTag();

    void setTag(@Nullable Object tag);

    @Nullable
    PlayerException getError();

    default boolean isOpen() {
        return getState().isAtLeast(State.OPEN);
    }

    default boolean isPlaying() {
        return getState() == State.PLAYING;
    }

    default boolean isPaused() {
        return getState() == State.PAUSED;
    }

    long getFrameLength();

    int getFramePosition();

    long getLongFramePosition();

    long getMicrosecondLength();

    long getMicrosecondPosition();



    boolean isCloseOnEndEnabled();

    void setCloseOnEnd(boolean closeOnEnd);

    boolean considerOpen();

//    default boolean considerOpenNoThrow() {
//        try {
//            considerOpen();
//            return true;
//        } catch (PlayerException e) {
//            Log.e(logTAG(), e);
//            return false;
//        }
//    }

    void play();

    void pause();

    void stop();

    @Override
    void close();


//    default boolean playNoThrow() {
//        try {
//            play();
//            return true;
//        } catch (PlayerException e) {
//            Log.e(logTAG(), e);
//        }
//
//        return false;
//    }

//    default void closeNoThrow() {
//        try {
//            close();
//        } catch (Throwable t) {
//            Log.e(logTAG(), "Exception in closing clip", t);
//        }
//    }



    boolean isSeekSupported();

    void setFramePosition(int frames) throws UnsupportedOperationException;

    void setMicrosecondPosition(long microseconds) throws UnsupportedOperationException;



    boolean isLoopSupported();

    int getLoopCount();

    int getCurrentLoop();

    void setLoopPointFrames(int start, int end) throws IllegalArgumentException, UnsupportedOperationException;

    void setLoopCount(int count) throws UnsupportedOperationException;

    default void loopContinuously() throws UnsupportedOperationException {
        setLoopCount(LOOP_CONTINUOUSLY);
    }


}
