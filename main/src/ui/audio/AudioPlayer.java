package ui.audio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.Log;
import util.live.ListenersI;

import javax.sound.sampled.AudioFormat;

public interface AudioPlayer extends AutoCloseable, ListenersI<AudioPlayer.Listener> {

    class CreationException extends RuntimeException {

        public CreationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    class PlayerException extends RuntimeException {

        public PlayerException(String message, Throwable cause) {
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


    int LOOP_CONTINUOUSLY = -1;


    enum State {
        IDLE,
        OPEN,
        PLAYING,
        PAUSED,
        STOPPED,
        ENDED,
        CLOSED
    }

    interface Listener {
        void onPlayerStateChanged(@NotNull AudioPlayer player, @NotNull State old, @NotNull State state);
    }



    @NotNull
    default String logTAG() {
        return getClass().getSimpleName();
    }

    long getId();

    State getState();

    @Nullable
    Object getTag();

    void setTag(@Nullable Object tag);



    default boolean isOpen() {
        return getState() == State.OPEN;
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

    void considerOpen() throws PlayerException;

    default boolean considerOpenNoThrow() {
        try {
            considerOpen();
            return true;
        } catch (PlayerException e) {
            Log.e(logTAG(), e.getMessage(), e.getCause());
            return false;
        }
    }

    void play() throws PlayerException;

    void pause();

    void stop();

    @Override
    void close() throws Exception;


    default boolean playNoThrow() {
        try {
            play();
            return true;
        } catch (PlayerException e) {
            Log.e(logTAG(), e.getMessage(), e.getCause());
        }

        return false;
    }

    default void closeNoThrow() {
        try {
            close();
        } catch (Throwable t) {
            Log.e(logTAG(), "Exception in closing clip", t);
        }
    }



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
