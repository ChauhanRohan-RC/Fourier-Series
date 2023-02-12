package ui;

import action.BaseAction;
import app.R;
import async.Async;
import misc.AudioListPlayer;
import misc.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import player.AudioPlayer;
import source.AudioSource;
import source.PathAudioSource;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MusicPlayer extends AudioListPlayer {

    public static final String TAG = "MusicPlayer";
    public static final String LOG_TAG = TAG;

    public static final boolean DEFAULT_ENABLED = true;
    public static final boolean DEFAULT_SHUFFLE = true;
    public static final boolean DEFAULT_STREAM = true;
    public static final boolean DEFAULT_LOG_ENABLED = true;

    public static final int DEFAULT_LOOP_COUNT = AudioPlayer.LOOP_CONTINUOUSLY;

    private static final int REQUEST_DELAY_MS = 500;


    @Nullable
    private static MusicPlayer sInstance;

    @NotNull
    private static MusicPlayer createMusicPlayer() {
        final List<AudioSource> sources = R.getMusicFiles().stream().map(PathAudioSource::new).collect(Collectors.toList());
        if (DEFAULT_SHUFFLE) {
            Collections.shuffle(sources);
        }

        final MusicPlayer player = new MusicPlayer(DEFAULT_STREAM, sources);
        player.setLoopCount(DEFAULT_LOOP_COUNT);
        player.setEnabled(DEFAULT_ENABLED);
        player.setLogEnabled(DEFAULT_LOG_ENABLED);
        return player;
    }

    @NotNull
    public static synchronized MusicPlayer getSingleton() {
        MusicPlayer p = sInstance;
        if (p == null) {
            synchronized (MusicPlayer.class) {
                p = sInstance;
                if (p == null) {
                    p = createMusicPlayer();
                    sInstance = p;
                }
            }
        }

        return p;
    }


    private volatile boolean mLogEnabled;
    @NotNull
    private final AtomicInteger mRequestedActionId = new AtomicInteger();

    @NotNull
    private final Set<Object> mPlayRequests;
    @NotNull
    private final PlaybackMenuAction playbackMenuAction;
    @NotNull
    private final EnabledAction enabledAction;
    @NotNull
    private final PlayPauseAction playPauseAction;
    @NotNull
    private final PlayNextAction playNextAction;
    @NotNull
    private final PlayNextAction playPrevAction;
    @NotNull
    private final ResetAction resetAction;

    private MusicPlayer(boolean stream, @NotNull List<AudioSource> sources) {
        super(stream, sources);

        mPlayRequests = new HashSet<>();

        playbackMenuAction = new PlaybackMenuAction();
        enabledAction = new EnabledAction();
        playPauseAction = new PlayPauseAction();
        playNextAction = new PlayNextAction(true);
        playPrevAction = new PlayNextAction(false);
        resetAction = new ResetAction();
    }



    private void enqueueRequest(@NotNull Runnable action) {
        final int id = mRequestedActionId.incrementAndGet();
        Async.uiPost(() -> {
            if (mRequestedActionId.get() == id) {
                action.run();
            }
        }, REQUEST_DELAY_MS);
    }

    public synchronized void requestPlay(@NotNull Object token) {
        mPlayRequests.add(token);
        enqueueRequest(this::play);
    }

    public synchronized void requestPause(@NotNull Object token) {
        if (mPlayRequests.remove(token) && mPlayRequests.isEmpty()) {
            enqueueRequest(this::pause);
        }
    }

    public synchronized void requestPlayPause(@NotNull Object token, boolean play) {
        if (play) {
            requestPlay(token);
        } else {
            requestPause(token);
        }
    }


    @Override
    protected void doReset() {
        super.doReset();
        mPlayRequests.clear();
    }

    @NotNull
    public Action getPlaybackMenuAction() {
        return playbackMenuAction;
    }

    @NotNull
    public Action getEnabledAction() {
        return enabledAction;
    }

    @NotNull
    public Action getPlayPauseAction() {
        return playPauseAction;
    }

    @NotNull
    public Action getPlayNextAction() {
        return playNextAction;
    }

    @NotNull
    public Action getPlayPrevAction() {
        return playPrevAction;
    }

    @NotNull
    public Action getResetAction() {
        return resetAction;
    }



    private void syncActions() {
        playbackMenuAction.sync();
        enabledAction.sync();
        playPauseAction.sync();
        playNextAction.sync();
        playPrevAction.sync();
        resetAction.sync();
    }

    @NotNull
    public JMenu createPlaybackMenu() {
        final JMenu menu = new JMenu(getPlaybackMenuAction());
        menu.add(getPlayPauseAction());
        menu.addSeparator();
        menu.add(getPlayPrevAction());
        menu.add(getPlayNextAction());
        menu.addSeparator();
        menu.add(getResetAction());
        return menu;
    }


    public boolean isLogEnabled() {
        return mLogEnabled;
    }

    protected void onLogEnabledChanged(final boolean logEnabled) {

    }

    private void setLogEnabledInternal(boolean logEnabled) {
        mLogEnabled = logEnabled;
        onLogEnabledChanged(logEnabled);
    }

    public void setLogEnabled(boolean logsEnabled) {
        final boolean old = mLogEnabled;
        if (old == logsEnabled)
            return;

        setLogEnabledInternal(logsEnabled);
    }

    public void toggleLogEnabled() {
        setLogEnabledInternal(!mLogEnabled);
    }




    @Override
    protected void onEnabledChanged(boolean enabled) {
        super.onEnabledChanged(enabled);
        syncActions();
    }

    @Override
    protected synchronized void onStateChanged(AudioPlayer.@NotNull State old, AudioPlayer.@NotNull State newState) {
        if (mLogEnabled) {
            Log.v(TAG, String.format("StateChanged: %s -> %s", old, newState));
        }

        super.onStateChanged(old, newState);
        syncActions();
    }

    @Override
    protected void onPlayerStateChanged(@NotNull AudioPlayer player, AudioPlayer.@NotNull State old, AudioPlayer.@NotNull State state, boolean isCurrentPlayer) {
        if (mLogEnabled) {
            Log.v(TAG, String.format("PlayerStateChanged: %s: IS_CURRENT: %b : %s -> %s", player, isCurrentPlayer, old, state));
        }

        super.onPlayerStateChanged(player, old, state, isCurrentPlayer);
        syncActions();
    }

    @Override
    protected void onPlayerChanged(@NotNull Indices prevIndices, @NotNull Indices indices) {
        if (mLogEnabled) {
            Log.v(TAG, String.format("PlayerChanged: %s -> %s", prevIndices, indices));
        }

        super.onPlayerChanged(prevIndices, indices);
        syncActions();
    }



    private class PlaybackMenuAction extends BaseAction {

        private PlaybackMenuAction() {
            setName("Music");
            setShortDescription("Control background music playback");
            sync();
        }

        public void sync() {
            super.sync();
            this.setEnabled(MusicPlayer.this.isEnabled() && hasSources());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // no-op
        }
    }

    private class EnabledAction extends BaseAction {

        private EnabledAction() {
            setName("Play Music");
            setShortDescription("Enable/Disable background music");
            sync();
        }

        public void sync() {
            super.sync();
            this.setSelected(MusicPlayer.this.isEnabled());
            this.setEnabled(hasSources());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            MusicPlayer.this.toggleEnabled();
        }
    }


    private class PlayPauseAction extends BaseAction {

        private PlayPauseAction() {
            sync();
        }

        public void sync() {
            super.sync();
            String name = isPlaying() ? "Pause" : "Play";
            String des = name + " background music";

            final AudioSource cur = getCurrentSource();
            if (cur != null) {
                final String post = " (" + cur.getDisplayName() + ")";
                name += post;
                des += post;
            }

            this.setName(name);
            this.setShortDescription(des);
            this.setEnabled(MusicPlayer.this.isEnabled() && hasSources());
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            MusicPlayer.this.togglePlayPause();
        }
    }


    private class PlayNextAction extends BaseAction {

        private final boolean next;

        private PlayNextAction(boolean next) {
            this.next = next;
            sync();
        }

        public void sync() {
            super.sync();

            final Indices ind = next ? getNextPlayerIndex() : getPreviousPlayerIndex();
            final AudioSource src = ind != null ? getSourceAt(ind.playerIndex()) : null;

            final String command = next ? "Next" : "Previous";
            String name = "Play " + command;
            String des = "Play " + command + " music track";
            if (src != null) {
                final String post = " (" + src.getDisplayName() + ")";
                name += post;
                des += post;
            }

            this.setName(name);
            this.setShortDescription(des);
            this.setEnabled(MusicPlayer.this.isEnabled() && src != null);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (next) {
                playNext();
            } else {
                playPrevious();
            }
        }
    }


    private class ResetAction extends BaseAction {

        private ResetAction() {
            setName("Reset");
            setShortDescription("Reset background music playback");
            sync();
        }

        public void sync() {
            super.sync();
            this.setEnabled(MusicPlayer.this.isEnabled() && hasSources() && getState() != AudioPlayer.State.IDLE);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            MusicPlayer.this.reset();
        }
    }

}
