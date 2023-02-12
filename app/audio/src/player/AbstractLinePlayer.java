package player;

import async.Async;
import misc.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import live.Listeners;

import javax.sound.sampled.*;
import java.util.Collection;

public abstract class AbstractLinePlayer implements AudioPlayer, LineListener {

    public enum StopMode {
        PAUSE,
        STOP_EXPLICIT,
        ERROR,
        CLOSE
    }


    private final long id;

    @NotNull
    private volatile State mState = State.IDLE;

    private volatile int mCurLoop;
    private volatile int mLoopCount;

    private volatile boolean mCloseOnEnd;
    @Nullable
    private volatile StopMode mNextStopMode;

    private int mLastPausedFrame = -1;

    @NotNull
    private final Listeners<Listener> mListeners = new Listeners<>();
    @Nullable
    private volatile Object mTag;

    @Nullable
    private volatile PlayerException mError;
    
    public AbstractLinePlayer(long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public State getState() {
        return mState;
    }

    @Override
    public boolean isOpen() {
        final DataLine line = getLine();
        return line != null && line.isOpen();
    }

    @Override
    public @Nullable Object getTag() {
        return mTag;
    }

    @Override
    public void setTag(@Nullable Object tag) {
        mTag = tag;
    }

    @Override
    public @Nullable PlayerException getError() {
        return mError;
    }

    protected void setError(@Nullable PlayerException error) {
        mError = error;
    }

    protected void onError(@Nullable PlayerException error) {
        setError(error);
        if (error != null) {
            Log.e(logTag(), error);
        }

        forceState(State.ERROR);
    }


    @Nullable
    protected abstract DataLine getLine();

    @Nullable
    protected AudioFormat getFormat() {
        final DataLine line = getLine();
        return line != null? line.getFormat(): null;
    }


    @Override
    public int getFramePosition() {
        final DataLine line = getLine();
        return line != null? line.getFramePosition(): AudioSystem.NOT_SPECIFIED;
    }

    @Override
    public long getLongFramePosition() {
        final DataLine line = getLine();
        return line != null? line.getLongFramePosition(): AudioSystem.NOT_SPECIFIED;
    }

    public long getMicrosecondLength() {
        final AudioFormat format = getFormat();
        final float frameRate;
        if (format == null || (frameRate = format.getFrameRate()) == AudioSystem.NOT_SPECIFIED)
            return AudioSystem.NOT_SPECIFIED;

        final long fl = getFrameLength();
        if (fl == AudioSystem.NOT_SPECIFIED)
            return AudioSystem.NOT_SPECIFIED;

        return (long) (fl / frameRate);
    }

    @Override
    public long getMicrosecondPosition() {
        final DataLine line = getLine();
        return line != null? line.getMicrosecondPosition(): AudioSystem.NOT_SPECIFIED;
    }

    @Override
    public final boolean isCloseOnEndEnabled() {
        return mCloseOnEnd;
    }

    @Override
    public final void setCloseOnEnd(boolean closeOnEnd) {
        final boolean old = mCloseOnEnd;
        if (old == closeOnEnd)
            return;

        mCloseOnEnd = closeOnEnd;
        onCloseOnEndChanged(closeOnEnd);
    }

    protected void onCloseOnEndChanged(boolean closeOnEnd) {

    }

    @Override
    public void update(LineEvent event) {
        final StopMode stopMode = mNextStopMode;
        mNextStopMode = null;

        if (LineEvent.Type.OPEN.equals(event.getType())) {
            updateState(State.OPEN);
        } else if (LineEvent.Type.START.equals(event.getType())) {
            updateState(State.PLAYING);
        } else if (LineEvent.Type.STOP.equals(event.getType())) {
            if (stopMode == StopMode.PAUSE) {
                updateState(State.PAUSED);
            } else if (stopMode == StopMode.STOP_EXPLICIT) {
                forceState(State.STOPPED);
            } else if (stopMode == StopMode.ERROR) {
                forceState(State.ERROR);
            } else if (stopMode == StopMode.CLOSE) {
                forceState(State.CLOSING);
                // will close after this, so do nothing (see {@link #close()})
            } else {
                boolean ended = true;
                if (isLoopSupported()) {
                    final int nextLoop = mCurLoop + 1;
                    final int loopCount = mLoopCount;
                    if (loopCount == LOOP_CONTINUOUSLY || nextLoop <= loopCount) {
                        // loop
                        mCurLoop = nextLoop;
                        play();
                        ended = false;
                    }
                }

                if (ended) {;
                    forceState(State.ENDED);
                }
            }
        } else if (LineEvent.Type.CLOSE.equals(event.getType())) {
            forceState(State.CLOSED);
        }
    }


    private synchronized void updateState(@NotNull State newState, boolean force) {
        final State old = mState;
        if (!force && old == newState)
            return;

        mState = newState;
        onStateChangedInternal(old, newState);
    }

    private synchronized void updateState(@NotNull State newState) {
        updateState(newState, false);
    }

    private synchronized void forceState(@NotNull State newState) {
        updateState(newState, true);
    }

    private synchronized void onStateChangedInternal(@NotNull State old, @NotNull State newState) {
        if (newState == State.OPEN || newState == State.ENDED || newState == State.CLOSED) {
            mCurLoop = 0;
        }

        onStateChanged(old, newState);
        mListeners.forEachListener(l -> l.onPlayerStateChanged(AbstractLinePlayer.this, old, newState));

        if (newState == State.ENDED && mCloseOnEnd) {
            close();
        }
    }

    protected synchronized void onStateChanged(@NotNull State old, @NotNull State newState) {

    }



    protected final void markNextStopAs(@Nullable StopMode nextStopMode) {
        mNextStopMode = nextStopMode;
    }

    protected final StopMode nextStopMark() {
        return mNextStopMode;
    }

    protected int getLastPausedFramePosition() {
        return mLastPausedFrame;
    }

    protected void invalidateLastPausedFramePosition() {
        mLastPausedFrame = -1;
    }


    protected void onLoopCountChanged(int oldLoopCount, int loopCount) {

    }

    private void onLoopCountChangedInternal(int oldLoopCount, int loopCount) {
        if (loopCount != LOOP_CONTINUOUSLY && mCurLoop > loopCount) {
            end();
            mCurLoop = 0;
        }

        onLoopCountChanged(oldLoopCount, loopCount);
    }

    @Override
    public synchronized void setLoopCount(int count) {
        if (!isLoopSupported()) {
            throw new UnsupportedOperationException("Loop is not supported");
        }

        if (count < 0) {
            count = LOOP_CONTINUOUSLY;
        }

        final int old = mLoopCount;
        if (old != count) {
            mLoopCount = count;
            onLoopCountChangedInternal(old, count);
        }
    }

    @Override
    public int getLoopCount() {
        return mLoopCount;
    }

    @Override
    public int getCurrentLoop() {
        return mCurLoop;
    }




    //    @Override
//    public final void considerOpen() throws PlayerException {
//
//    }

    @Override
    public synchronized void play() {
        if (isPlaying())
            return;

        Async.execute(() -> {
            if (!considerOpen())
                return;

            final DataLine line = getLine();
            if (line != null) {
                try {
                    if (isSeekSupported()) {
                        final int lastFramePos = getLastPausedFramePosition();
                        setFramePosition(lastFramePos != -1? lastFramePos: 0);
                    }

                    line.start();
                    invalidateLastPausedFramePosition();
                } catch (Throwable t) {
                    onError(new PlayerException("failed to play audio", t));
                }
            }
        });
    }

    @Override
    public synchronized final void pause() {
        if (!isPlaying())
            return;

        final DataLine line = getLine();
        if (line == null)
            return;

        mLastPausedFrame = getFramePosition();
        markNextStopAs(StopMode.PAUSE);
        line.stop();
    }

    protected synchronized void stopLine(boolean ifPlaying, @Nullable StopMode stopMode) {
        if (ifPlaying && !isPlaying())
            return;

        final DataLine line = getLine();
        if (line == null)
            return;

        markNextStopAs(stopMode);
        line.stop();
    }

    protected void end() {
        stopLine(true, null);
    }

    @Override
    public synchronized final void stop() {
        if (isPlaying()) {
            stopLine(false, StopMode.STOP_EXPLICIT);
        } else if (isPaused()) {
            forceState(State.STOPPED);
        }
    }


    protected synchronized void doClose() throws Exception {
        final DataLine line = getLine();
        if (line != null)
            line.close();
    }

    @Override
    public synchronized final void close() {
        if (isPlaying()) {
            markNextStopAs(StopMode.CLOSE);     // closing a running line will dispatch stop event
        }

        try {
            doClose();
        } catch (Throwable t) {
            setError(new PlayerException("Exception in closing audio data line...force closing now", t));
            forceState(State.CLOSED);      // mark as closed
        }
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + "(source: " + getSource().getDisplayName() + ")";
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
