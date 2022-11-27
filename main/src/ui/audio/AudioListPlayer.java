package ui.audio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ui.audio.source.AudioSource;
import util.Log;
import util.live.Listeners;
import util.live.ListenersI;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AudioListPlayer implements AutoCloseable, AudioPlayer.Listener, ListenersI<AudioListPlayer.Listener> {

    public static final String TAG = "AudioListPlayer";

    public record Indices(int loopIndex, int playerIndex) {

        boolean areValid(int loopCount, int playersCount) {
            return playerIndex >= 0 && playerIndex < playersCount && (loopIndex >= 0 && (loopCount == AudioPlayer.LOOP_CONTINUOUSLY || loopIndex <= loopCount));
        }
    }

    public interface Listener {
        void onListPlayerStateChanged(@NotNull AudioListPlayer listPlayer, @NotNull AudioPlayer.State oldState, @NotNull AudioPlayer.State newState);

        void onCurrentPlayerChanged(@NotNull AudioListPlayer listPlayer, @NotNull Indices oldIndices, @NotNull Indices newIndices);
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
            return new Indices(curLoopIndex - 1, playersCount - 1);
        }

        return null;
    }



    @NotNull
    private final List<AudioPlayer> players;

    @NotNull
    private volatile AudioPlayer.State mState = AudioPlayer.State.IDLE;

    private volatile int mCurPlayerIndex;
    private volatile int mCurLoopIndex;
    private volatile int mLoopCount;

    @Nullable
    private AudioPlayer.PlayerException error;
    @NotNull
    private final Listeners<Listener> mListeners = new Listeners<>();

    public AudioListPlayer(boolean stream, @NotNull List<AudioSource> sources) {

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
        if (!indices.areValid(mLoopCount, getCount()))
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



    @Override
    public void onPlayerStateChanged(@NotNull AudioPlayer player, AudioPlayer.@NotNull State old, AudioPlayer.@NotNull State state) {
        // Current player
        if (player == getCurrentPlayer()) {
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
                case CLOSED -> {

                }
            }
        }

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



    private void closeAllPlayers() {
        players.forEach(AudioPlayer::close);
    }

    // Reset back to idle state
    public void reset() {
        closeAllPlayers();
        mCurLoopIndex = 0;
        mCurPlayerIndex = 0;
        forceState(AudioPlayer.State.IDLE);
    }

    @Override
    public void close() {
        closeAllPlayers();
        mCurLoopIndex = 0;
        mCurPlayerIndex = 0;
        forceState(AudioPlayer.State.CLOSED);
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
