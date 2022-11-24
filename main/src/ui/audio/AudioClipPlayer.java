package ui.audio;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.Log;
import util.live.Listeners;
import util.live.ListenersI;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;


/**
 * A player to play short sound clips<br>
 * It uses {@link Clip clip}, which <strong>LOADS WHOLE SOUND DATA IN MEMORY</strong>
 *
 * @see Clip
 * */
public class AudioClipPlayer implements AutoCloseable, LineListener, ListenersI<AudioClipPlayer.Listener> {

    public static final String TAG = "AudioClipPlayer";

    public static class CreationException extends RuntimeException {

        public CreationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class PlayerException extends RuntimeException {

        public PlayerException(String message, Throwable cause) {
            super(message, cause);
        }
    }


    public enum State {
        IDLE,
        OPEN,
        PLAYING,
        PAUSED,
        ENDED,
        CLOSED
    }

    public interface Listener {

        void onPlayerStateChanged(@NotNull AudioClipPlayer player, @NotNull State old, @NotNull State state);

    }


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
            final AudioInputStream stream = AudioSystem.getAudioInputStream(url);
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




    private final long id;
    @NotNull
    private final AudioInputStream stream;
    @NotNull
    private final Clip clip;

    @NotNull
    private volatile State mState = State.IDLE;

    private volatile boolean mCloseOnEnd;
    private volatile boolean mStopIsPause;

    private int mLastPausedPosFrame = -1;

    @NotNull
    private final Listeners<Listener> mListeners = new Listeners<>();
    @Nullable
    private volatile Object mTag;

    private AudioClipPlayer(long id, @NotNull AudioInputStream stream, @NotNull Clip newClip) {
        this.id = id;
        this.stream = stream;
        this.clip = newClip;

        clip.addLineListener(this);
    }

    public final long getId() {
        return id;
    }

    @Nullable
    public Object getTag() {
        return mTag;
    }

    public AudioClipPlayer setTag(@Nullable Object tag) {
        mTag = tag;
        return this;
    }

    @Override
    public void update(LineEvent event) {
        if (LineEvent.Type.OPEN.equals(event.getType())) {
            updateState(State.OPEN);
        } else if (LineEvent.Type.START.equals(event.getType())) {
            updateState(State.PLAYING);
        } else if (LineEvent.Type.STOP.equals(event.getType())) {
            final boolean isPause = mStopIsPause;
            mStopIsPause = false;
            updateState(isPause? State.PAUSED: State.ENDED);
        } else if (LineEvent.Type.CLOSE.equals(event.getType())) {
            updateState(State.CLOSED);
        }
    }


    private synchronized void updateState(@NotNull State newState) {
        final State old = mState;
        if (old == newState)
            return;

        mState = newState;
        onStateChangedInternal(old, newState);
    }

    private synchronized void onStateChangedInternal(@NotNull State old, @NotNull State newState) {
        if (newState == State.ENDED && mCloseOnEnd) {
            closeNoThrow();
        }

        onStateChanged(old, newState);
        mListeners.forEachListener(l -> l.onPlayerStateChanged(AudioClipPlayer.this, old, newState));
    }

    protected synchronized void onStateChanged(@NotNull State old, @NotNull State newState) {

    }


    @NotNull
    public State getState() {
        return mState;
    }

    public boolean isOpen() {
        return clip.isOpen();
    }

    public boolean isPlaying() {
        return clip.isRunning();
    }

    public boolean isPaused() {
        return !(isPlaying() || mLastPausedPosFrame == -1);
    }

    public int getFrameCount() {
        return clip.getFrameLength();
    }

    public int getFramePosition() {
        return clip.getFramePosition();
    }

    public long getLongFramePosition() {
        return clip.getLongFramePosition();
    }

    public long getMicrosecondDuration() {
        return clip.getMicrosecondLength();
    }

    public long getMicrosecondPosition() {
        return clip.getMicrosecondPosition();
    }

    public AudioClipPlayer setFramePosition(int frames) {
        clip.setFramePosition(frames);
        return this;
    }

    public AudioClipPlayer setMicrosecondPosition(long microseconds) {
        clip.setMicrosecondPosition(microseconds);
        return this;
    }


    public boolean isCloseOnEndEnabled() {
        return mCloseOnEnd;
    }

    public AudioClipPlayer setCloseOnEnd(boolean closeOnEnd) {
        mCloseOnEnd = closeOnEnd;
        return this;
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

    public synchronized boolean considerOpenNoThrow() {
        try {
            considerOpen();
            return true;
        } catch (PlayerException e) {
            Log.e(TAG, null, e);
            return false;
        }
    }


    public synchronized void play() throws PlayerException {
        if (isPlaying())
            return;

        considerOpen();

        final int lastFramePos = mLastPausedPosFrame;
        clip.setFramePosition(lastFramePos != -1? lastFramePos: 0);

        clip.start();
        mLastPausedPosFrame = -1;
    }

    public synchronized void pause() {
        if (!isPlaying())
            return;

        mLastPausedPosFrame = clip.getFramePosition();
        mStopIsPause = true;
        clip.stop();
    }

    public synchronized void stop() {
        mStopIsPause = false;
        clip.stop();
        mLastPausedPosFrame = -1;
    }

    public synchronized AudioClipPlayer setLoopPoints(int start, int end) throws IllegalArgumentException {
        clip.setLoopPoints(start, end);
        return this;
    }

    public synchronized AudioClipPlayer setLoopCount(int count) {
        clip.loop(count);
        return this;
    }

    public synchronized AudioClipPlayer loopContinuously() {
        return setLoopCount(Clip.LOOP_CONTINUOUSLY);
    }

    public synchronized boolean playNoThrow() {
        try {
            play();
            return true;
        } catch (Throwable t) {
            Log.e(TAG, "Failed to start sound clip", t);
        }

        return false;
    }


    @Override
    public void close() throws Exception {
        clip.stop();
        clip.close();
    }

    public void closeNoThrow() {
        try {
            close();
        } catch (Throwable t) {
            Log.e(TAG, "Exception in closing clip", t);
        }
    }


    @Override
    public int listenersCount() {
        return mListeners.listenersCount();
    }

    @Override
    public boolean addListener(@NotNull Listener listener) {
        return mListeners.addListener(listener);
    }

    @Override
    public boolean removeListener(@NotNull Listener listener) {
        return mListeners.removeListener(listener);
    }

    @Override
    public boolean containsListener(@NotNull Listener listener) {
        return mListeners.containsListener(listener);
    }

    @Override
    public @NotNull Collection<Listener> iterationCopy() {
        return mListeners.iterationCopy();
    }
}
