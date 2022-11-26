package ui.audio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.live.Listeners;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import java.util.Collection;

public abstract class AbstractLinePlayer implements AudioPlayer, LineListener {

    public enum StopMode {
        PAUSE,
        STOP_EXPLICIT
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
    public @Nullable Object getTag() {
        return mTag;
    }

    @Override
    public void setTag(@Nullable Object tag) {
        mTag = tag;
    }

    @NotNull
    protected abstract DataLine getLine();

    @NotNull
    protected abstract AudioFormat getFormat();



    @Override
    public int getFramePosition() {
        return getLine().getFramePosition();
    }

    @Override
    public long getLongFramePosition() {
        return getLine().getLongFramePosition();
    }

    public long getMicrosecondLength() {
        return (long) (getFrameLength() / getFormat().getFrameRate());
    }

    @Override
    public long getMicrosecondPosition() {
        return getLine().getMicrosecondPosition();
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
        if (LineEvent.Type.OPEN.equals(event.getType())) {
            updateState(State.OPEN);
        } else if (LineEvent.Type.START.equals(event.getType())) {
            updateState(State.PLAYING);
        } else if (LineEvent.Type.STOP.equals(event.getType())) {
            final StopMode stopMode = mNextStopMode;
            mNextStopMode = null;

            if (stopMode == StopMode.PAUSE) {
                updateState(State.PAUSED);
            } else if (stopMode == StopMode.STOP_EXPLICIT) {
                updateState(State.STOPPED);
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

                if (ended) {
                    updateState(State.ENDED);
                }
            }
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
        if (newState == State.OPEN || newState == State.ENDED || newState == State.CLOSED) {
            mCurLoop = 0;
        }

        onStateChanged(old, newState);
        mListeners.forEachListener(l -> l.onPlayerStateChanged(AbstractLinePlayer.this, old, newState));

        if (newState == State.ENDED && mCloseOnEnd) {
            closeNoThrow();
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
    public void play() throws PlayerException {
        if (isPlaying())
            return;

        considerOpen();

        if (isSeekSupported()) {
            final int lastFramePos = getLastPausedFramePosition();
            setFramePosition(lastFramePos != -1? lastFramePos: 0);
        }

        getLine().start();
        invalidateLastPausedFramePosition();
    }

    @Override
    public synchronized final void pause() {
        if (!isPlaying())
            return;

        mLastPausedFrame = getFramePosition();
        markNextStopAs(StopMode.PAUSE);
        getLine().stop();
    }

    private synchronized void doStop(boolean ifPlaying, @Nullable StopMode stopMode) {
        if (ifPlaying && !isPlaying())
            return;

        markNextStopAs(stopMode);
        getLine().stop();
    }

    protected void end() {
        doStop(true, null);
    }

    @Override
    public synchronized final void stop() {
        if (isPlaying()) {
            doStop(false, StopMode.STOP_EXPLICIT);
        } else if (isPaused()) {
            updateState(State.STOPPED);
        }
    }

    @Override
    public synchronized void close() throws Exception {
        getLine().close();
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
