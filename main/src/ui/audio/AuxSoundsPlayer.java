package ui.audio;

import app.R;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ui.action.BaseAction;
import ui.audio.source.PathAudioSource;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.nio.file.Path;


/**
 * Auxiliary Sounds player
 * */
public class AuxSoundsPlayer extends AudioController {

    public static final boolean DEFAULT_ENABLED = true;

    @Nullable
    private static AuxSoundsPlayer sInstance;

    @NotNull
    private static AuxSoundsPlayer createSoundsPlayer() {
        final AuxSoundsPlayer player = new AuxSoundsPlayer();
        player.setEnabled(DEFAULT_ENABLED);
        return player;
    }

    @NotNull
    public static synchronized AuxSoundsPlayer getSingleton() {
        AuxSoundsPlayer p = sInstance;
        if (p == null) {
            synchronized (AuxSoundsPlayer.class) {
                p = sInstance;
                if (p == null) {
                    p = createSoundsPlayer();
                    sInstance = p;
                }
            }
        }

        return p;
    }




    @NotNull
    private final EnabledAction enabledAction;

    private AuxSoundsPlayer() {
        super();
        enabledAction = new EnabledAction();
    }


    @Nullable
    public AudioPlayer playClip(@NotNull Path clip) {
        return playClip(new PathAudioSource(clip));
    }

    @Nullable
    public AudioPlayer stream(@NotNull Path clip) {
        return stream(new PathAudioSource(clip));
    }

    @Nullable
    public AudioPlayer playWindowOpen() {
        return playClick();     // TODO change
    }

    @Nullable
    public AudioPlayer playWindowClose() {
        return playClick();     // TODO change
    }


    @Nullable
    public AudioPlayer playClick() {
        return playClip(R.SOUND_FILE_CLICK);
    }

    @Nullable
    public AudioPlayer playHover() {
        return playClip(R.SOUND_FILE_HOVER);        // TODO: bug not working
    }

    public void closeAllClips() {
        closeAllSteaming(false);
    }


    @NotNull
    public Action getEnabledAction() {
        return enabledAction;
    }

    private void syncActions() {
        enabledAction.sync();
    }

    @Override
    protected void onEnabledChanged(boolean enabled) {
        super.onEnabledChanged(enabled);
        syncActions();
    }




    private class EnabledAction extends BaseAction {

        private EnabledAction() {
            setName("Play Sounds");
            setShortDescription("Enable/Disable aux sounds (start, clicks, hover etc)");
            sync();
        }

        private void sync() {
            setSelected(AuxSoundsPlayer.this.isEnabled());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AuxSoundsPlayer.this.toggleEnabled();
        }
    }

}
