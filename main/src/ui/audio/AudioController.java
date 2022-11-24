package ui.audio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.CollectionUtil;
import util.Log;
import util.async.Function;

import java.net.URL;
import java.util.*;

public class AudioController implements AudioClipPlayer.Listener {

    public static final String TAG = "AudioController";

    public enum ExistsStrategy {
        /**
         * Keeps the existing player (does NOT create any new player)
         * */
        KEEP(p -> true),

//        PAUSE(p -> {
//            p.pause();
//            return false;
//        }),
//
//        STOP(p -> {
//            p.stop();
//            return false;
//        }),

        /**
         * Close existing player (and create new player if needed)
         * */
        CLOSE(p -> {
            p.closeNoThrow();
            return false;
        });

        // Return true to indicate that new player should NOT be created
        @Nullable
        public final Function<AudioClipPlayer, Boolean> existingPlayerHandler;

        ExistsStrategy(@Nullable Function<AudioClipPlayer, Boolean> existingPlayerHandler) {
            this.existingPlayerHandler = existingPlayerHandler;
        }
    }




    @NotNull
    private final Map<Long, AudioClipPlayer> mOpenCLipPlayers= Collections.synchronizedMap(new HashMap<>());
    private long mIdGen = 10000;

    @Override
    public synchronized void onPlayerStateChanged(@NotNull AudioClipPlayer player, AudioClipPlayer.@NotNull State old, AudioClipPlayer.@NotNull State state) {
        Log.d(TAG, "Player state changed: ID: " + player.getId() + ", state: " + state);

        switch (state) {
            case OPEN -> {
                player.ensureListener(AudioController.this);
                mOpenCLipPlayers.put(player.getId(), player);
            }

            case CLOSED -> {
                player.removeListener(AudioController.this);
                mOpenCLipPlayers.remove(player.getId());
            }
        }
    }

    public int openPlayersCount() {
        return mOpenCLipPlayers.size();
    }

    public synchronized void closeAllOpenPlayers() {
        final List<AudioClipPlayer> players = CollectionUtil.linkedListCopy(mOpenCLipPlayers.values());
        mOpenCLipPlayers.clear();

        players.forEach(AudioClipPlayer::closeNoThrow);
    }

    @Nullable
    public synchronized AudioClipPlayer getClipPlayerIfOpen(long id) {
        return mOpenCLipPlayers.get(id);
    }

    public synchronized void closeClipPlayer(long id) {
        final AudioClipPlayer player = getClipPlayerIfOpen(id);
        if (player != null) {
            player.closeNoThrow();
        }
    }

    public synchronized boolean play(long id, @NotNull URL url, int loopCount, @Nullable ExistsStrategy existsStrategy) {
        if (existsStrategy != null && existsStrategy.existingPlayerHandler != null) {
            final AudioClipPlayer player = getClipPlayerIfOpen(id);
            if (player != null) {
                final boolean handled = existsStrategy.existingPlayerHandler.apply(player);
                if (handled) {
                    return true;
                }
            }
        }

        closeClipPlayer(id);
        final AudioClipPlayer player = AudioClipPlayer.createNoThrow(id, url);
        if (player != null) {
            player.setCloseOnEnd(true);
            player.addListener(this);

            if (!player.considerOpenNoThrow()) {
                player.removeListener(this);
                return false;
            }

//            player.setLoopCount(loopCount);  // TODO: BUG: undefined behaviour, sometimes blocks indefinitely
            return player.playNoThrow();
        }

        return false;
    }

    /**
     * Play a sound clip and forget it
     * */
    public synchronized boolean playOnce(@NotNull URL url) {
        return play(mIdGen++, url, 0, null);        // with a custom unique id
    }

}
