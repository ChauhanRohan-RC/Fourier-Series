package misc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import player.AudioClipPlayer;
import player.AudioPlayer;
import player.AudioStreamer;
import source.AudioSource;
import live.Listeners;
import live.ListenersI;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AudioListPlayer implements AutoCloseable, AudioPlayer.Listener, ListenersI<AudioListPlayer.Listener> {

    public record Indices(int loopIndex, int playerIndex) {
        boolean areValid(int loopCount, int playersCount) {
            return playerIndex >= 0 && playerIndex < playersCount && (loopIndex >= 0 && (loopCount == AudioPlayer.LOOP_CONTINUOUSLY || loopIndex <= loopCount));
        }

        @Override
        public String toString() {
            return String.format("[Loop: %d, Player: %d]", loopIndex, playerIndex);
        }
    }

    public interface Listener {

        void onListPlayerStateChanged(@NotNull AudioListPlayer listPlayer, @NotNull AudioPlayer.State oldState, @NotNull AudioPlayer.State newState);

        void onCurrentPlayerChanged(@NotNull AudioListPlayer listPlayer, @NotNull Indices oldIndices, @NotNull Indices newIndices);

        void onListPlayerEnabledChanged(@NotNull AudioListPlayer listPlayer, boolean enabled);
    }


    @NotNull
    private static AudioPlayer createPlayer(boolean stream, long id, @NotNull AudioSource source) {
        if (stream) {
            return new AudioStreamer(id, source);
        }

        return new AudioClipPlayer(id, source);
    }


    @Nullable
    public static Indices getNextPlayerIndex(int playersCount, int curPlayerIndex, int loopCount, int curLoopIndex) {
        if (playersCount == 0)
            return null;

        if (curPlayerIndex < 0 || curPlayerIndex >= playersCount) {
            throw new IndexOutOfBoundsException("Current player index should be >= 0 and < " + playersCount + ", given: " + curPlayerIndex);
        }

        // Current Loop
        if (curPlayerIndex < playersCount - 1)
            return new Indices(curLoopIndex, curPlayerIndex + 1);

        // Next Loop
        if (loopCount == AudioPlayer.LOOP_CONTINUOUSLY || curLoopIndex < loopCount) {
            return new Indices(curLoopIndex + 1, 0);
        }

        return null;
    }

    @Nullable
    public static Indices getPreviousPlayerIndex(int playersCount, int curPlayerIndex, int loopCount, int curLoopIndex) {
        if (playersCount == 0)
            return null;

        if (curPlayerIndex < 0 || curPlayerIndex >= playersCount) {
            throw new IndexOutOfBoundsException("Current player index should be >= 0 and < " + playersCount + ", given: " + curPlayerIndex);
        }

        // Current Loop
        if (curPlayerIndex > 0)
            return new Indices(curLoopIndex, curPlayerIndex - 1);

        // Previous Loop
        if (loopCount == AudioPlayer.LOOP_CONTINUOUSLY || curLoopIndex > 0) {
            return new Indices(Math.max(curLoopIndex - 1, 0), playersCount - 1);
        }

        return null;
    }



    @NotNull
    private final List<AudioPlayer> players;

    @NotNull
    private volatile AudioPlayer.State mState = AudioPlayer.State.IDLE;

    private volatile boolean mEnabled = true;

    private volatile int mCurPlayerIndex;
    private volatile int mCurLoopIndex;
    private volatile int mLoopCount;

    @Nullable
    private AudioPlayer.PlayerException error;
    @NotNull
    private final Listeners<Listener> mListeners = new Listeners<>();

    public AudioListPlayer(boolean stream, @NotNull List<? extends AudioSource> sources) {
        final AtomicInteger i = new AtomicInteger(0);
        this.players = sources.stream().map(s -> {
            final AudioPlayer player = createPlayer(stream, i.getAndIncrement(), s);
            player.setCloseOnEnd(true); // auto
            return player;
        }).collect(Collectors.toList());
    }

    public int getCount() {
        return players.size();
    }

    public boolean hasSources() {
        return getCount() > 0;
    }

    @NotNull
    public AudioPlayer getPlayerAt(int index) {
        return players.get(index);
    }

    @NotNull
    public AudioSource getSourceAt(int index) {
        return getPlayerAt(index).getSource();
    }

    @Nullable
    public AudioPlayer getCurrentPlayer() {
        final int index = mCurPlayerIndex;
        if (index < 0 || index >= getCount())
            return null;

        return getPlayerAt(mCurPlayerIndex);
    }

    @Nullable
    public AudioSource getCurrentSource() {
        final AudioPlayer player = getCurrentPlayer();
        return player != null? player.getSource(): null;
    }



    @Nullable
    public Indices getNextPlayerIndex(int curPlayerIndex, int curLoopIndex) {
        return getNextPlayerIndex(players.size(), curPlayerIndex, mLoopCount, curLoopIndex);
    }

    @Nullable
    public Indices getPreviousPlayerIndex(int curPlayerIndex, int curLoopIndex) {
        return getPreviousPlayerIndex(players.size(), curPlayerIndex, mLoopCount, curLoopIndex);
    }

    @Nullable
    public Indices getNextPlayerIndex() {
        return getNextPlayerIndex(mCurPlayerIndex, mCurLoopIndex);
    }

    @Nullable
    public Indices getPreviousPlayerIndex() {
        return getPreviousPlayerIndex(mCurPlayerIndex, mCurLoopIndex);
    }

    public boolean hasNext() {
        return getNextPlayerIndex() != null;
    }

    public boolean hasPrevious() {
        return getPreviousPlayerIndex() != null;
    }

    public boolean isOpen() {
        return mState.isAtLeast(AudioPlayer.State.OPEN);
    }

    public boolean isPlaying() {
        return mState == AudioPlayer.State.PLAYING;
    }

    public boolean isPaused() {
        return mState == AudioPlayer.State.PAUSED;
    }

    public synchronized boolean play() {
        if (isPlaying())
            return true;

//        final AudioPlayer player = getCurrentPlayer();
//        return player != null && player.play();
        return play(mCurPlayerIndex);       // just play current one
    }

    public synchronized void pause() {
        if (!isPlaying())
            return;

        final AudioPlayer player = getCurrentPlayer();
        if (player != null) {
            player.pause();
        }
    }

    public synchronized void togglePlayPause() {
        if (isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    public synchronized void stop() {
        if (!isPlaying())
            return;

        final AudioPlayer player = getCurrentPlayer();
        if (player != null) {
            player.stop();
        }
    }



    protected void onPlayerChanged(@NotNull Indices prevIndices, @NotNull Indices indices) {
        mListeners.forEachListener(l -> l.onCurrentPlayerChanged(AudioListPlayer.this, prevIndices, indices));
    }


    /**
     * @return if the new player is now started (opening or playing)
     * */
    private synchronized boolean play(@NotNull Indices indices) {
        if (!(mEnabled && indices.areValid(mLoopCount, getCount())))
            return false;

        final int curPlayerIndex = mCurPlayerIndex;
        final int curLoopIndex = mCurLoopIndex;
        mCurLoopIndex = indices.loopIndex;      // sometimes, only current loop is changed

        final AudioPlayer curPlayer = getCurrentPlayer();
        if (curPlayer != null) {
            if (curPlayerIndex == indices.playerIndex) {
                curPlayer.ensureListener(this);
                if (mState == AudioPlayer.State.IDLE || curLoopIndex != indices.loopIndex) {
                    onPlayerChanged(new Indices(curLoopIndex, curPlayerIndex), indices);
                }

                curPlayer.play();
                return true;
            }

            curPlayer.close();
            curPlayer.removeListener(this);
        }

        // new player
        mCurPlayerIndex = indices.playerIndex;
        final AudioPlayer player = getPlayerAt(indices.playerIndex);
        player.ensureListener(this);

        onPlayerChanged(new Indices(curLoopIndex, curPlayerIndex), indices);
        player.play();
        return true;
    }

    /**
     * @return if the new player is now started (opening or playing)
     * */
    public synchronized boolean play(int playerIndex) {
        return play(new Indices(mCurLoopIndex, playerIndex));
    }

    /**
     * @return if the new player is now started (opening or playing)
     * */
    public synchronized boolean playNext() {
        final Indices next = getNextPlayerIndex();
        return next != null && play(next);
    }

    /**
     * @return if the new player is now started (opening or playing)
     * */
    public synchronized boolean playPrevious() {
        final Indices prev = getPreviousPlayerIndex();
        return prev != null && play(prev);
    }


    private synchronized void forceEnd() {
        final AudioPlayer player = getCurrentPlayer();
        if (player != null) {
            player.removeListener(this);
            player.close();
        }

        mCurLoopIndex = 0;
        mCurPlayerIndex = 0;
        forceState(AudioPlayer.State.ENDED);
    }


    private void setError(@Nullable AudioPlayer.PlayerException error) {
        this.error = error;
    }

    @Nullable
    public AudioPlayer.PlayerException getError() {
        return error;
    }

    private void onError(@Nullable AudioPlayer.PlayerException error) {
        setError(error);
        forceState(AudioPlayer.State.ERROR);
    }




    protected void onPlayerStateChanged(@NotNull AudioPlayer player, AudioPlayer.@NotNull State old, AudioPlayer.@NotNull State state, boolean isCurrentPlayer) {
        // Current player
        if (isCurrentPlayer) {
            switch (state) {
                case OPEN -> {
                    if (!isOpen()) {
                        forceState(AudioPlayer.State.OPEN);
                    }
                }
                case PLAYING -> updateState(AudioPlayer.State.PLAYING);
                case PAUSED -> updateState(AudioPlayer.State.PAUSED);
                case STOPPED -> forceState(AudioPlayer.State.STOPPED);
                case ERROR -> onError(player.getError());
                case ENDED -> {
                    if (!playNext()) {
                        forceEnd();
                    }
                }

                case CLOSING, CLOSED -> {

                }
            }
        }

        // Detach on close
        if (state == AudioPlayer.State.CLOSED) {
            player.removeListener(this);
        }
    }

    @Override
    public final void onPlayerStateChanged(@NotNull AudioPlayer player, AudioPlayer.@NotNull State old, AudioPlayer.@NotNull State state) {
        onPlayerStateChanged(player, old, state, player == getCurrentPlayer());
    }



    protected synchronized void onStateChanged(@NotNull AudioPlayer.State old, @NotNull AudioPlayer.State newState) {

    }

    private synchronized void onStateChangedInternal(@NotNull AudioPlayer.State old, @NotNull AudioPlayer.State newState) {
        if (newState == AudioPlayer.State.OPEN || newState == AudioPlayer.State.ENDED || newState == AudioPlayer.State.CLOSED) {
            mCurLoopIndex = 0;
        }

        onStateChanged(old, newState);
        mListeners.forEachListener(l -> l.onListPlayerStateChanged(AudioListPlayer.this, old, newState));

//        if (newState == AudioPlayer.State.ENDED) {
//            close();
//        }
    }



    private synchronized void updateState(@NotNull AudioPlayer.State newState, boolean force) {
        final AudioPlayer.State old = mState;
        if (!force && old == newState)
            return;

        mState = newState;
        onStateChangedInternal(old, newState);
    }

    private synchronized void updateState(@NotNull AudioPlayer.State newState) {
        updateState(newState, false);
    }

    private synchronized void forceState(@NotNull AudioPlayer.State newState) {
        updateState(newState, true);
    }

    @NotNull
    public AudioPlayer.State getState() {
        return mState;
    }

    protected void onLoopCountChanged(int oldLoopCount, int loopCount) {

    }

    private synchronized void onLoopCountChangedInternal(int oldLoopCount, int loopCount) {
        if (loopCount != AudioPlayer.LOOP_CONTINUOUSLY && mCurLoopIndex > loopCount) {


            mCurLoopIndex = 0;
        }

        onLoopCountChanged(oldLoopCount, loopCount);
    }

    public synchronized void setLoopCount(int count) {
        if (count < 0) {
            count = AudioPlayer.LOOP_CONTINUOUSLY;
        }

        final int old = mLoopCount;
        if (old != count) {
            mLoopCount = count;
            onLoopCountChangedInternal(old, count);
        }
    }

    public int getLoopCount() {
        return mLoopCount;
    }

    public int getCurrentLoop() {
        return mCurLoopIndex;
    }



    private void closeAllPlayers(boolean detachListener) {
        if (detachListener) {
            final AudioPlayer player = getCurrentPlayer();
            if (player != null) {
                player.removeListener(this);
            }
        }

        players.forEach(AudioPlayer::close);
    }


    protected void doReset() {

    }

    // Reset back to idle state
    public final void reset() {
        closeAllPlayers(false);
        mCurLoopIndex = 0;
        mCurPlayerIndex = 0;

        doReset();
        forceState(AudioPlayer.State.IDLE);
    }

    @Override
    public void close() {
        closeAllPlayers(false);
        mCurLoopIndex = 0;
        mCurPlayerIndex = 0;
        forceState(AudioPlayer.State.CLOSED);
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    protected void onEnabledChanged(final boolean enabled) {
        if (!enabled) {
            reset();
        }

        mListeners.dispatchOnMainThread(l -> l.onListPlayerEnabledChanged(AudioListPlayer.this, enabled));
    }

    private void setEnabledInternal(boolean enabled) {
        mEnabled = enabled;
        onEnabledChanged(enabled);
    }

    /**
     * @return whether enabled state is changed
     * */
    public boolean setEnabled(boolean enabled) {
        final boolean old = mEnabled;
        if (old == enabled)
            return false;

        setEnabledInternal(enabled);
        return true;
    }

    public void toggleEnabled() {
        setEnabledInternal(!mEnabled);
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
