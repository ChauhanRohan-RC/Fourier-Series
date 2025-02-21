package app;

import misc.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import ui.MusicPlayer;
import ui.frames.FourierUi;
import async.Async;
import async.TaskCompletionListener;
import ui.util.Ui;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class App {

    public static final String TAG = "App";

    @NotNull
    private static final AtomicInteger sWindowCount = new AtomicInteger();
    @NotNull
    private static final List<JFrame> sFrames = Collections.synchronizedList(new LinkedList<>());

    private static final Settings.Listener sSettingListener = new Settings.Listener() {
        @Override
        public void onLookAndFeelChanged(@NotNull String className) {

        }

        @Override
        public void onAppearancePreferencesChanged() {

        }

        @Override
        public void onFTIntegrationIntervalCountChanged(int fourierTransformSimpson13NDefault) {

        }

        @Override
        public void onConfigPreferencesChanged() {

        }

        @Override
        public void onLogPreferencesChanged() {

        }

        @Override
        public void onSoundPreferencesChanged() {

        }

        @Override
        public void onOtherPreferencesChanged() {

        }
    };

    private static void init() {
        Log.v(TAG, "Initialising...");
        R.init();
        Settings.getSingleton().ensureListener(sSettingListener);
    }

    private static void finish() {
        Log.v(TAG, "Saving Configurations...");

        Settings.considerSave(false, (TaskCompletionListener<Void>) (data, failed, cancelled, error) -> {
            R.finish();

            Log.v(TAG, "Quiting");
            quit();
        });
    }
    private static void quit() {
//        AuxSoundsPlayer.getSingleton().closeAllClips();
        MusicPlayer.getSingleton().close();
        Async.shutDown();
        System.exit(0);
    }



    private static void considerShowIntro(@Nullable Component parent) {
        final Settings settings = Settings.getSingleton();
        if (!settings.containsOtherFlag(Settings.OTHER_FLAG_INTRO_SHOWN, false)) {
            Ui.showAboutDialog(parent);
            settings.addOtherFlags(Settings.OTHER_FLAG_INTRO_SHOWN);
        }
    }


    private static volatile boolean sFirstFrameOpen = false;

    private static void onFirstFrameOpen(@NotNull JFrame frame) {
        considerShowIntro(frame);
    }

    public static void onWindowOpen(@NotNull JFrame frame) {
        sFrames.add(frame);
        sWindowCount.incrementAndGet();
        Log.v(TAG, "Window Open: Title: " + frame.getTitle() + " | WindowCount: " + sWindowCount);

        if (!sFirstFrameOpen) {
            sFirstFrameOpen = true;
            onFirstFrameOpen(frame);
        }
    }

    public static void onWindowClose(@NotNull JFrame frame) {
        sFrames.remove(frame);
        final int count = sWindowCount.decrementAndGet();
        Log.v(TAG, "Window Close: Title: " + frame.getTitle() + " | WindowCount: " + count);

        if (count <= 0) {
            Async.uiPost(() -> {
                final int nowCount = sWindowCount.decrementAndGet();
                if (nowCount <= 0) {
                    finish();
                }
            }, 50);
        }
    }

    @Nullable
    public static FourierUi findFourierUi() {
        for (JFrame frame: sFrames) {
            if (frame instanceof FourierUi fu)
                return fu;
        }

        return null;
    }

    @NotNull
    public static FourierUi findOrLaunchFourierUi() {
        FourierUi fu = findFourierUi();
        if (fu == null) {
            fu = Ui.launchFourierUi(null);
        }

        return fu;
    }



    @NotNull
    @Unmodifiable
    public static List<JFrame> getFrames() {
        return Collections.unmodifiableList(sFrames);
    }

    public static void updateAllFramesTree() {
        sFrames.forEach(SwingUtilities::updateComponentTreeUI);
    }


    // TODO: Test launcher
    private static void launchTest(String[] args) {
        Async.postIfNotOnMainThread(() -> {
            final FourierUi ui = Ui.launchFourierUi(null);
        });
    }

    // TODO: Main production launcher
    private static void launchMain(String[] args) {
        Async.postIfNotOnMainThread(() -> Ui.launchFourierUi(null));
    }


    public static final boolean TEST = false;

    public static void main(String[] args) {
        init();

        if (TEST) {
            Log.v(TAG, "Test session starting...");
            launchTest(args);
        } else {
            launchMain(args);
        }
    }

}
