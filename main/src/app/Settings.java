package app;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import json.Json;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.FileUtil;
import util.Format;
import util.Log;
import util.async.Async;
import util.async.Canceller;
import util.async.TaskConsumer;
import util.live.Listeners;
import util.live.ListenersI;
import util.live.WeakListeners;
import util.main.ComplexUtil;

import javax.swing.*;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class Settings {

    public static final String TAG = "Settings";

    public interface Listener {
        void onLookAndFeelChanged(@NotNull String className);

        void onFTIntegrationIntervalCountChanged(int fourierTransformSimpson13NDefault);
    }


    public static final String DEFAULT_LOOK_AND_FEEL_CLASSNAME = FlatDarculaLaf.class.getName();
    public static final int DEFAULT_FT_INTEGRATION_INTERVAL_COUNT = ComplexUtil.FOURIER_TRANSFORM_SIMPSON_13_N_DEFAULT;


    @NotNull
    public static String getCurrentLookAndFeelClassName() {
        return UIManager.getLookAndFeel().getClass().getName();
    }

    public static int getCurrentFTIntegrationIntervalCount() {
        return ComplexUtil.getFourierTransformSimpson13NCurrentDefault();
    }



    /* ........................... Singleton ....................... */

    @Nullable
    private static volatile Settings sInstance;

    @NotNull
    private static Settings createDefault() {
        return new Settings();
    }

    /**
     * Retrieve single instance of {@link Settings settings}, creating and initializing it does not exist
     * */
    @NotNull
    public static Settings getSingleton() {
        Settings ins = sInstance;
        if (ins != null) {
            return ins;
        }

        synchronized (Settings.class) {
            ins = sInstance;
            if (ins != null) {
                return ins;
            }

            if (Files.isRegularFile(R.SETTINGS_FILE)) {
                try {
                    ins = Settings.loadFromJson(R.SETTINGS_FILE);
                } catch (Throwable e) {
                    Log.e(TAG, "failed to load setting from file: " + R.SETTINGS_FILE, e);
                }
            }
        }

        if (ins == null) {
            ins = createDefault();
        }

        // INIT
        sInstance = ins;
        ins.applySettings();

        return ins;
    }

    /**
     * Save the settings instance
     *
     * @param file file to save settings to
     * @param sync whether to save synchronously
     * */
    public static void considerSave(@NotNull Path file, boolean sync, @Nullable TaskConsumer<Void> callback) {
        final Settings settings = sInstance;
        if (settings == null) {
            if (callback != null) {
                callback.consume(null);
            }

            return;
        }


        if (sync) {
            try {
                settings.writeJson(file);
                if (callback != null) {
                    Async.uiPost(() -> callback.consume(null));
                }
            } catch (Throwable e) {
                Log.e(TAG, "Failed to save settings to file: " + file, e);
                if (callback != null) {
                    Async.uiPost(() -> callback.onFailed(e));
                }
            }
        } else {
            TaskConsumer<Void> call = new TaskConsumer<>() {
                @Override
                public void onFailed(@Nullable Throwable e) {
                    Log.e(TAG, "Failed to save settings to file: " + file, e);
                }

                @Override
                public void consume(Void data) {

                }
            };

            if (callback != null) {
                call = call.andThen(callback);
            }

            settings.writeJsonAsync(file, call);
        }
    }

    public static void considerSave(boolean sync, @Nullable TaskConsumer<Void> callback) {
        considerSave(R.SETTINGS_FILE, sync, callback);
    }






    @Expose(serialize = false, deserialize = false)
    private transient int mModCount;

    @Expose(serialize = false, deserialize = false)
    private transient final ListenersI<Listener> mListeners = new WeakListeners<>();        // Weak Listeners

    @SerializedName("theme")
    @Nullable
    private volatile String mLookAndFeelClassName;

    @SerializedName("numerical_integration_interval_count")
    private volatile int mFtIntegrationIntervalCount = -1;

    // DEFAULT
    private Settings() {
//        mLookAndFeelClassName = DEFAULT_LOOK_AND_FEEL_CLASSNAME;
    }

    public int getModCount() {
        return mModCount;
    }


    /* ........................ Listeners ........................... */

    public void addListener(@NotNull Listener l) {
        mListeners.addListener(l);
    }

    public boolean removeListener(@NotNull Listener l) {
        return mListeners.removeListener(l);
    }

    public void ensureListener(@NotNull Listener l) {
        mListeners.ensureListener(l);
    }

    public boolean containsListener(@NotNull Listener l) {
        return mListeners.containsListener(l);
    }


    /* ................................. Apply and Reset ................................. */

    /**
     * Apply Settings to internal domains
     *  */
    public void applySettings() {
        setLookAndFeel(getLookAndFeelOrDefault());
        setFTIntegrationIntervalCount(getFTIntegrationIntervalCountOrDefault());
    }

    public void resetAppearance() {
        setLookAndFeel(DEFAULT_LOOK_AND_FEEL_CLASSNAME);
    }

    public void resetConfig() {
        setFTIntegrationIntervalCount(DEFAULT_FT_INTEGRATION_INTERVAL_COUNT);
    }

    public void resetAll() {
        resetAppearance();
        resetConfig();
    }



    /* .............................. Appearance .......................... */

    protected void onLookAndFeelChanged(@NotNull String className) {
        mLookAndFeelClassName = className;
        mModCount++;

        mListeners.dispatchOnMainThread(l -> l.onLookAndFeelChanged(className));
    }

    /**
     * @param defaultVal default value to return in case this preference is not set
     * @return current look and feel class name, or {@code null if not set}
     * */
    public String getLookAndFeel(String defaultVal) {
        final String cur =  mLookAndFeelClassName;
        return Format.isEmpty(cur)? defaultVal: cur;
    }

    @NotNull
    public String getLookAndFeelOrDefault() {
        return getLookAndFeel(DEFAULT_LOOK_AND_FEEL_CLASSNAME);
    }


    /**
     * Sets the look and feel class name
     *
     * @param className look and feel class name, or {@code null to set default}
     * @return whether the look and feel is successfully applied
     * */
    public boolean setLookAndFeel(@Nullable String className) {
        if (Format.isEmpty(className)) {
            className = DEFAULT_LOOK_AND_FEEL_CLASSNAME;
        }

        if (className.equals(getCurrentLookAndFeelClassName()))
            return true;

        try {
            UIManager.setLookAndFeel(className);
            onLookAndFeelChanged(className);
            return true;
        } catch (Throwable t) {
            Log.e(TAG, "Failed to set look and feel: " + className, t);
        }

        return false;
    }



    /* ................................. Config .................................... */

    protected void onFTIntegrationIntervalCountChanged(int intervalCount) {
        mFtIntegrationIntervalCount = intervalCount;
        mModCount++;

        mListeners.dispatchOnMainThread(l -> l.onFTIntegrationIntervalCountChanged(intervalCount));
    }

    /**
     * @param defaultVal default value to return in case this preference is not set
     * @return Fourier transform Integration Interval Count, or default val if not set
     * */
    public int getFTIntegrationIntervalCount(int defaultVal) {
        final int cur = mFtIntegrationIntervalCount;
        return cur < 1? defaultVal: cur;
    }

    public int getFTIntegrationIntervalCountOrDefault() {
        return getFTIntegrationIntervalCount(DEFAULT_FT_INTEGRATION_INTERVAL_COUNT);
    }

    /**
     * Sets Fourier Transform numerical integration interval count
     *
     * @param intervalCount interval count, or {@code < 1} for default
     * @return whether the preference is successfully applied
     * */
    public boolean setFTIntegrationIntervalCount(int intervalCount) {
        if (intervalCount < 1) {
            intervalCount = DEFAULT_FT_INTEGRATION_INTERVAL_COUNT;
        }

        if (intervalCount == getCurrentFTIntegrationIntervalCount())
            return true;

        if (!ComplexUtil.setFourierTransformSimpson13NCurrentDefault(intervalCount)) {
            return false;
        }

        onFTIntegrationIntervalCountChanged(intervalCount);
        return true;
    }





    /*...................................  JSON  ...........................................*/

    /* Save */

    @NotNull
    public String toJsonString() throws JsonParseException {
        return Json.get().gson.toJson(this, getClass());
    }

    public void writeJson(@NotNull Appendable writer) throws JsonParseException {
        Json.get().gson.toJson(this, getClass(), writer);
    }

    public void writeJson(@NotNull Path file, @NotNull Charset encoding) throws JsonParseException, IOException {
        FileUtil.ensureFileParentDir(file);

        try (final Writer writer = Files.newBufferedWriter(file, encoding)) {
            writeJson(writer);
        }
    }

    public void writeJson(@NotNull Path file) throws JsonParseException, IOException {
        writeJson(file, R.ENCODING);
    }

    @NotNull
    public Canceller writeJsonAsync(@NotNull Path file, @NotNull Charset encoding, @Nullable TaskConsumer<Void> consumer) {
        return Async.execute(() -> {
            writeJson(file, encoding);
            return null;
        }, consumer);
    }

    @NotNull
    public Canceller writeJsonAsync(@NotNull Path file, @Nullable TaskConsumer<Void> consumer) {
        return writeJsonAsync(file, R.ENCODING, consumer);
    }

    /* Load */

    @NotNull
    public static Settings loadFromJson(@NotNull Reader json) throws JsonParseException {
        return Json.get().gson.fromJson(json, Settings.class);
    }

    @NotNull
    public static Settings loadFromJson(@NotNull Path file, @NotNull Charset encoding) throws IOException, JsonParseException {
        try (final Reader reader = Files.newBufferedReader(file, encoding)) {
            return loadFromJson(reader);
        }
    }

    @NotNull
    public static Settings loadFromJson(@NotNull Path file) throws IOException, JsonParseException {
        return loadFromJson(file, R.ENCODING);
    }

    @NotNull
    public static Canceller loadFromJsonAsync(@NotNull Path file, @NotNull Charset encoding, @NotNull TaskConsumer<Settings> consumer) {
        return Async.execute(() -> loadFromJson(file, encoding), consumer);
    }

    @NotNull
    public static Canceller loadFromJsonAsync(@NotNull Path file, @NotNull TaskConsumer<Settings> consumer) {
        return loadFromJsonAsync(file, R.ENCODING, consumer);
    }
}
