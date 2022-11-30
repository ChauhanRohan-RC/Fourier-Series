package app;

import misc.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import provider.Providers;
import ui.frames.FourierUi;
import async.Async;
import async.TaskCompletionListener;

import javax.swing.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class App {

    public static final String TAG = "App";

    private static int sWindowCount;
    @NotNull
    private static final List<JFrame> sFrames = new LinkedList<>();


    private static final Settings.Listener sSettingListener = new Settings.Listener() {
        @Override
        public void onLookAndFeelChanged(@NotNull String className) {

        }

        @Override
        public void onFTIntegrationIntervalCountChanged(int fourierTransformSimpson13NDefault) {

        }

        @Override
        public void onLogPrefsChanged() {

        }

        @Override
        public void onSoundPrefChanged() {

        }
    };

    private static void init() {
        R.init();
        Settings.getSingleton().ensureListener(sSettingListener);

        Log.v(TAG, "Initialising...");
    }

    private static void finish() {
        Log.v(TAG, "Saving Configurations...");

        Settings.considerSave(false, (TaskCompletionListener<Void>) (data, failed, cancelled, error) -> {
            R.finish();

            Log.v(TAG, "Quiting");
            System.exit(0);
        });
    }

    public static void onWindowOpen(@NotNull JFrame frame) {
        sFrames.add(frame);
        sWindowCount++;
        Log.v(TAG, "Window Open: Title: " + frame.getTitle() + " | WindowCount: " + sWindowCount);
    }

    public static void onWindowClose(@NotNull JFrame frame) {
        sFrames.remove(frame);
        sWindowCount--;
        Log.v(TAG, "Window Close: Title: " + frame.getTitle() + " | WindowCount: " + sWindowCount);
        if (sWindowCount <= 0) {
            finish();
        }
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

    }

    // TODO: Main production launcher
    private static void launchMain(String[] args) {
        Async.postIfNotOnMainThread(() -> {
            final FourierUi ui = new FourierUi(null, -1);
            ui.setFunctionProvider(Providers.NoopProvider.getSingleton());          // start with None
        });
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
