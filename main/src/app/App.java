package app;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import provider.Providers;
import ui.FourierUi;
import util.Log;
import util.async.Async;
import util.async.TaskCompletionListener;
import util.async.TaskConsumer;

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
    };

    private static void init() {
        Log.d(TAG, "Initialising...");
        R.init();

        Settings.getSingleton().ensureListener(sSettingListener);
    }

    private static void finish() {
        Log.d(TAG, "Quiting...");

        Settings.considerSave(false, (TaskCompletionListener<Void>) (data, failed, cancelled, error) -> {
            R.finish();
            System.exit(0);
        });
    }

    public static void onWindowOpen(@NotNull JFrame frame) {
        sFrames.add(frame);
        sWindowCount++;
        Log.d(TAG, "onWindowOpen: Title: " + frame.getTitle() + " | WindowCount: " + sWindowCount);
    }

    public static void onWindowClose(@NotNull JFrame frame) {
        sFrames.remove(frame);
        sWindowCount--;
        Log.d(TAG, "onWindowClose: Title: " + frame.getTitle() + " | WindowCount: " + sWindowCount);
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

    public static void main(String[] args) {
        init();

//        launchTest(args);
        launchMain(args);
    }

}
