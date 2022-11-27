//package ui.audio;
//
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//import util.CollectionUtil;
//
//import java.net.URL;
//import java.util.*;
//
//public class AudioController implements AudioPlayer.Listener {
//
//    public static final String TAG = "AudioController";
//
//    public interface ExistsStrategy {
//
//        ExistsStrategy KEEP = p -> true;
//
//        ExistsStrategy CLOSE = p -> {
//            p.closeNoThrow();
//            return false;
//        };
//
//
//        /**
//         * @return  true to indicate that new player should NOT be created,
//         * otherwise existing player is closed and new player is created
//         * */
//        boolean handleExisting(@NotNull AudioPlayer player);
//
//    }
//
//
//
//    @NotNull
//    private final Map<Long, AudioPlayer> mOpenCLipPlayers= Collections.synchronizedMap(new HashMap<>());
//    private long mIdGen = 10000;
//
//    @Override
//    public synchronized void onPlayerStateChanged(@NotNull AudioPlayer player, AudioPlayer.@NotNull State old, AudioPlayer.@NotNull State state) {
//        switch (state) {
//            case OPEN -> {
//                player.ensureListener(AudioController.this);
//                mOpenCLipPlayers.put(player.getId(), player);
//            }
//
//            case CLOSED -> {
//                player.removeListener(AudioController.this);
//                mOpenCLipPlayers.remove(player.getId());
//            }
//        }
//    }
//
//    public int openPlayersCount() {
//        return mOpenCLipPlayers.size();
//    }
//
//    public synchronized void closeAllOpenPlayers() {
//        final List<AudioPlayer> players = CollectionUtil.linkedListCopy(mOpenCLipPlayers.values());
//        mOpenCLipPlayers.clear();
//
//        players.forEach(AudioPlayer::closeNoThrow);
//    }
//
//    @Nullable
//    public synchronized AudioPlayer getClipPlayerIfOpen(long id) {
//        return mOpenCLipPlayers.get(id);
//    }
//
//    @Nullable
//    public synchronized AudioPlayer getClipPlayerIfOpen(@NotNull URL url) {
//        for (AudioPlayer player: mOpenCLipPlayers.values()) {
//            Object tag = player.getTag();
//            if (tag instanceof URL && url.equals(tag))
//                return player;
//        }
//
//        return null;
//    }
//
//
//    public synchronized void closeClipPlayer(long id) {
//        final AudioPlayer player = getClipPlayerIfOpen(id);
//        if (player != null) {
//            player.closeNoThrow();
//        }
//    }
//
//    public synchronized void closeClipPlayer(@NotNull URL url) {
//        final AudioPlayer player = getClipPlayerIfOpen(url);
//        if (player != null) {
//            player.closeNoThrow();
//        }
//    }
//
//    @Nullable
//    public synchronized AudioPlayer play(long id, @NotNull URL url, int loopCount, @NotNull ExistsStrategy existsStrategy) {
//        final AudioPlayer old = getClipPlayerIfOpen(id);
//        if (old != null) {
//            final boolean handled = existsStrategy.handleExisting(old);
//            if (handled) {
//                old.playNoThrow();
//                return old;
//            }
//        }
//
//        closeClipPlayer(id);
//        final AudioPlayer player = AudioPlayer.createNoThrow(id, url);
//        if (player != null) {
//            player.setCloseOnEnd(true);
//            player.addListener(this);
//
//            if (!player.considerOpenNoThrow()) {
//                player.removeListener(this);
//                return null;
//            }
//
//            player.setTag(url);
//            player.setLoopCount(loopCount);
//            player.playNoThrow();
//            return player;
//        }
//
//        return null;
//    }
//
//
//    public synchronized long nextId() {
//        return mIdGen++;
//    }
//
//    /**
//     * Play a sound clip and forget it
//     * */
//    public synchronized AudioPlayer playOnce(@NotNull URL url) {
//        return play(nextId(), url, 0, ExistsStrategy.CLOSE);        // with a custom unique id
//    }
//
//    /**
//     * Plays a clip forever (loops continuously), and keeps existing player
//     * */
//    public synchronized AudioPlayer playForever(long id, @NotNull URL url) {
//        return play(id, url, AudioPlayer.LOOP_CONTINUOUSLY, ExistsStrategy.KEEP);
//    }
//
//
//
//    /**
//     * @return id of the player, or {@code -1} if sound clip could not be played
//     * */
//    public synchronized AudioPlayer playForever(@NotNull URL url) {
//        final AudioPlayer old = getClipPlayerIfOpen(url);
//
//        if (old != null) {
//            old.setLoopCount(AudioPlayer.LOOP_CONTINUOUSLY);
//            old.playNoThrow();
//            return old;
//        }
//
//        return playForever(nextId(), url);
//    }
//
//}



package ui.audio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ui.audio.source.AudioSource;
import ui.audio.source.URLAudioSource;
import util.CollectionUtil;

import java.net.URL;
import java.util.*;

public class AudioController implements AudioPlayer.Listener {

    public static final String TAG = "AudioController";

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

    public synchronized void closeAllOpenPlayers() {
        final List<AudioPlayer> players = CollectionUtil.linkedListCopy(mOpenPlayers.values());
        mOpenPlayers.clear();

        players.forEach(AudioPlayer::close);
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

    @Nullable
    public synchronized AudioPlayer getPlayerIfOpen(@NotNull URL url) {
        return getPlayerIfOpen(new URLAudioSource(url));
    }


    public synchronized void closePlayer(long id) {
        final AudioPlayer player = getPlayerIfOpen(id);
        if (player != null) {
            player.close();
        }
    }

    public synchronized void closePlayer(@NotNull URL url) {
        final AudioPlayer player = getPlayerIfOpen(url);
        if (player != null) {
            player.close();
        }
    }

    @Nullable
    public synchronized AudioPlayer createPlayer(boolean stream, long id, @NotNull URL url, @NotNull ExistsStrategy existsStrategy) {
        final AudioPlayer old = getPlayerIfOpen(id);
        if (old != null) {
            final boolean handled = existsStrategy.handleExisting(old);
            if (handled) {
                return old;
            }
        }

        closePlayer(id);
        final AudioSource source = new URLAudioSource(url);
        final AudioPlayer player = stream? new AudioStreamer(id, source): new AudioClipPlayer(id, source);
        player.setCloseOnEnd(true);
        player.addListener(this);

        if (!player.considerOpen()) {
            player.removeListener(this);
            return null;
        }

        player.setTag(url);
        return player;

    }


    public synchronized long nextId() {
        return mIdGen++;
    }

    /**
     * Play a sound clip and forget it
     * */
    public synchronized AudioPlayer play(boolean stream, @NotNull URL url) {
        final AudioPlayer player = createPlayer(stream, nextId(), url, ExistsStrategy.CLOSE);  // with a custom unique id
        if (player != null) {
            player.play();
        }

        return player;
    }

    public synchronized AudioPlayer playClip(@NotNull URL url) {
        return play(false, url);
    }

    public synchronized AudioPlayer stream(@NotNull URL url) {
        return play(true, url);
    }

    /**
     * Plays a clip forever (loops continuously), and keeps existing player
     * */
    public synchronized AudioPlayer playForever(boolean stream, long id, @NotNull URL url) {
        final AudioPlayer player = createPlayer(stream, id, url, ExistsStrategy.KEEP);
        if (player != null) {
            if (player.isLoopSupported()) {
                player.loopContinuously();
            }

            player.play();
        }

        return player;
    }

    public synchronized AudioPlayer playForever(boolean stream, @NotNull URL url) {
        final AudioPlayer old = getPlayerIfOpen(url);

        if (old != null) {
            if (old.isLoopSupported()) {
                old.loopContinuously();
            }

            old.play();
            return old;
        }

        return playForever(stream, nextId(), url);
    }

}

