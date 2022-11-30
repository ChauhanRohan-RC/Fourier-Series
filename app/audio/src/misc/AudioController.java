package misc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import player.AudioClipPlayer;
import player.AudioPlayer;
import player.AudioStreamer;
import source.AudioSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class AudioController implements AudioPlayer.Listener {

    public static final String TAG = "main.AudioController";

    public interface ExistsStrategy {

        ExistsStrategy KEEP = p -> true;

        ExistsStrategy CLOSE = p -> {
            p.close();
            return false;
        };


        /**
         * @return  true to indicate that new player should NOT be created,
         * otherwise existing player is closed and new player is created
         * */
        boolean handleExisting(@NotNull AudioPlayer player);

    }



    private volatile boolean mEnabled = true;
    @NotNull
    private final Map<Long, AudioPlayer> mOpenPlayers = Collections.synchronizedMap(new HashMap<>());
    private long mIdGen = 10000;

    @Override
    public synchronized void onPlayerStateChanged(@NotNull AudioPlayer player, AudioPlayer.@NotNull State old, AudioPlayer.@NotNull State state) {
        switch (state) {
            case OPEN -> {
                player.ensureListener(AudioController.this);
                mOpenPlayers.put(player.getId(), player);
            }

            case CLOSED -> {
                player.removeListener(AudioController.this);
                mOpenPlayers.remove(player.getId());
            }
        }
    }

    public int openPlayersCount() {
        return mOpenPlayers.size();
    }

    public synchronized void closeAllOpenPlayers(@Nullable Predicate<? super AudioPlayer> filter) {
        final List<AudioPlayer> players = CollectionUtil.linkedListCopy(mOpenPlayers.values());
        mOpenPlayers.clear();

        if (filter == null) {
            players.forEach(AudioPlayer::close);
        } else {
            players.stream().filter(filter).forEach(AudioPlayer::close);
        }
    }

    public synchronized void closeAllSteaming(boolean streaming) {
        closeAllOpenPlayers(p -> p.isStreaming() == streaming);
    }


    @Nullable
    public synchronized AudioPlayer getPlayerIfOpen(long id) {
        return mOpenPlayers.get(id);
    }

    @Nullable
    public synchronized AudioPlayer getPlayerIfOpen(@NotNull AudioSource source) {
        for (AudioPlayer player: mOpenPlayers.values()) {
            if (source.equals(player.getSource()))
                return player;
        }

        return null;
    }

    public synchronized void closePlayer(long id) {
        final AudioPlayer player = getPlayerIfOpen(id);
        if (player != null) {
            player.close();
        }
    }

    public synchronized void closePlayer(@NotNull AudioSource source) {
        final AudioPlayer player = getPlayerIfOpen(source);
        if (player != null) {
            player.close();
        }
    }

    @Nullable
    public synchronized AudioPlayer createPlayer(boolean stream, long id, @NotNull AudioSource source, @NotNull ExistsStrategy existsStrategy) {
        if (!mEnabled)
            return null;

        final AudioPlayer old = getPlayerIfOpen(id);
        if (old != null) {
            final boolean handled = existsStrategy.handleExisting(old);
            if (handled) {
                return old;
            }
        }

        closePlayer(id);
        final AudioPlayer player = stream? new AudioStreamer(id, source): new AudioClipPlayer(id, source);
        player.setCloseOnEnd(true);
        player.addListener(this);

        if (!player.considerOpen()) {
            player.removeListener(this);
            return null;
        }

        return player;
    }


    public synchronized long nextId() {
        return mIdGen++;
    }

    /**
     * Play a sound clip and forget it
     * */
    public synchronized AudioPlayer play(boolean stream, @NotNull AudioSource source) {
        final AudioPlayer player = createPlayer(stream, nextId(), source, ExistsStrategy.CLOSE);  // with a custom unique id
        if (player != null) {
            player.play();
        }

        return player;
    }

    public synchronized AudioPlayer playClip(@NotNull AudioSource source) {
        return play(false, source);
    }

    public synchronized AudioPlayer stream(@NotNull AudioSource source) {
        return play(true, source);
    }

    /**
     * Plays a clip forever (loops continuously), and keeps existing player
     * */
    public synchronized AudioPlayer playForever(boolean stream, long id, @NotNull AudioSource source) {
        final AudioPlayer player = createPlayer(stream, id, source, ExistsStrategy.KEEP);
        if (player != null) {
            if (player.isLoopSupported()) {
                player.loopContinuously();
            }

            player.play();
        }

        return player;
    }

    public synchronized AudioPlayer playForever(boolean stream, @NotNull AudioSource source) {
        final AudioPlayer old = getPlayerIfOpen(source);

        if (old != null) {
            if (old.isLoopSupported()) {
                old.loopContinuously();
            }

            old.play();
            return old;
        }

        return playForever(stream, nextId(), source);
    }




    public boolean isEnabled() {
        return mEnabled;
    }


    protected void onEnabledChanged(final boolean enabled) {
        if (!enabled) {
            closeAllOpenPlayers(null);
        }
    }

    private void setEnabledInternal(boolean enabled) {
        mEnabled = enabled;
        onEnabledChanged(enabled);
    }

    /**
     * @return whether enabled state is changed
     * */
    public final boolean setEnabled(boolean enabled) {
        final boolean old = mEnabled;
        if (old == enabled)
            return false;

        setEnabledInternal(enabled);
        return true;
    }

    public void toggleEnabled() {
        setEnabledInternal(!mEnabled);
    }

}

