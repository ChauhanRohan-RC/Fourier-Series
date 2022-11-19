package app;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.google.gson.*;
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
import util.live.ListenersI;
import util.live.WeakListeners;
import util.main.ComplexUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class Settings {

    public static final String TAG = "Settings";

    public interface Listener {
        void onLookAndFeelChanged(@NotNull String className);

        void onFTIntegrationIntervalCountChanged(int fourierTransformSimpson13NDefault);

        void onLogPrefsChanged();
    }


    /* ................. KEYS ................ */

    // Appearance
    private static final String PREFS_APPEARANCE = "appearance";
    private static final String KEY_LOOK_AND_FEEL_CLASS_NAME = "theme";

    // Config
    private static final String PREFS_CONFIG = "config";
    private static final String KEY_FT_INTEGRATION_INTERVALS = "numerical_integration_interval_count";

    // Logs
    private static final String PREFS_LOGS = "logs";
    private static final String KEY_LOG_DEBUG = "debug";
    private static final String KEY_LOG_TO_CONSOLE = "console_logging";
    private static final String KEY_LOG_TO_FILE = "file_logging";



    // Default Appearance
    public static final String DEFAULT_LOOK_AND_FEEL_CLASSNAME = FlatDarculaLaf.class.getName();

    // Default Config
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

    /* Appearance */
    @Nullable
    @SerializedName(KEY_LOOK_AND_FEEL_CLASS_NAME)
    private volatile String mLookAndFeelClassName;

    /* Configurations */
    @SerializedName(KEY_FT_INTEGRATION_INTERVALS)
    private volatile int mFtIntegrationIntervalCount = -1;

    /* Logs */
    @SerializedName(KEY_LOG_DEBUG)
    @Nullable
    private volatile Boolean mLogDebug;

    @SerializedName(KEY_LOG_TO_CONSOLE)
    @Nullable
    private volatile Boolean mLogToConsole;

    @SerializedName(KEY_LOG_TO_FILE)
    @Nullable
    private volatile Boolean mLogToFile;

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
        // Appearance
        setLookAndFeel(getLookAndFeelOrDefault());

        // Config
        setFTIntegrationIntervalCount(getFTIntegrationIntervalCountOrDefault());

        // Logs
        setLogDebug(getLogDebugOrDefault());
        setLogToConsole(getLogToConsoleOrDefault());
        setLogToFile(getLogToFileOrDefault());
    }

    public void resetAppearance() {
        setLookAndFeel(DEFAULT_LOOK_AND_FEEL_CLASSNAME);
    }

    public void resetConfig() {
        setFTIntegrationIntervalCount(DEFAULT_FT_INTEGRATION_INTERVAL_COUNT);
    }

    public void resetLogs() {
        setLogDebug(Log.DEFAULT_DEBUG);
        setLogToConsole(Log.DEFAULT_LOG_TO_CONSOLE);
        setLogToFile(Log.DEFAULT_LOG_TO_FILE);
    }

    public void resetAll() {
        resetAppearance();
        resetConfig();
        resetLogs();
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



    /* ........................ LOGS ........................... */

    protected void onLogPrefsChanged() {
        mListeners.dispatchOnMainThread(Listener::onLogPrefsChanged);
    }

    @Nullable
    public Boolean getLogDebug() {
        return mLogDebug;
    }

    public boolean getLogDebug(boolean defaultValue) {
        final Boolean debug = mLogDebug;
        if (debug != null)
            return debug;
        return defaultValue;
    }

    public boolean getLogDebugOrDefault() {
        return getLogDebug(Log.DEFAULT_DEBUG);
    }

    public void setLogDebug(boolean debug) {
        boolean changed = Log.setDebug(debug);
        mLogDebug = Log.isDebugEnabled();
        if (changed) {
            onLogPrefsChanged();
        }
    }


    @Nullable
    public Boolean getLogToConsole() {
        return mLogToConsole;
    }

    public boolean getLogToConsole(boolean defaultValue) {
        final Boolean console = mLogToConsole;
        if (console != null)
            return console;
        return defaultValue;
    }

    public boolean getLogToConsoleOrDefault() {
        return getLogToConsole(Log.DEFAULT_LOG_TO_CONSOLE);
    }

    public void setLogToConsole(boolean console) {
        boolean changed = Log.setLogToConsole(console);
        mLogToConsole = Log.isLoggingToConsole();
        if (changed) {
            onLogPrefsChanged();
        }
    }


    @Nullable
    public Boolean getLogToFile() {
        return mLogToFile;
    }

    public boolean getLogToFile(boolean defaultValue) {
        final Boolean file = mLogToFile;
        if (file != null)
            return file;
        return defaultValue;
    }

    public boolean getLogToFileOrDefault() {
        return getLogToFile(Log.DEFAULT_LOG_TO_FILE);
    }

    public void setLogToFile(boolean logToFile) {
        boolean changed = Log.setLogToFile(logToFile);
        mLogToFile = Log.isLoggingToFile();
        if (changed) {
            onLogPrefsChanged();
        }
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



    /* Json Adapter */

    public static class GsonAdapter implements JsonSerializer<Settings>, JsonDeserializer<Settings> {

        @Override
        public JsonElement serialize(Settings src, Type typeOfSrc, JsonSerializationContext context) {
            // Appearance
            final JsonObject appearance = new JsonObject();
            appearance.addProperty(KEY_LOOK_AND_FEEL_CLASS_NAME, getCurrentLookAndFeelClassName());

            // Config
            final JsonObject config = new JsonObject();
            config.addProperty(KEY_FT_INTEGRATION_INTERVALS, getCurrentFTIntegrationIntervalCount());

            // LOGS
            final JsonObject logs = new JsonObject();
            logs.addProperty(KEY_LOG_DEBUG, Log.isDebugEnabled());
            logs.addProperty(KEY_LOG_TO_CONSOLE, Log.isLoggingToConsole());
            logs.addProperty(KEY_LOG_TO_FILE, Log.isLoggingToFile());

            // Finalizing
            final JsonObject settings = new JsonObject();
            settings.add(PREFS_APPEARANCE, appearance);
            settings.add(PREFS_CONFIG, config);
            settings.add(PREFS_LOGS, logs);
            return settings;
        }

        @Override
        public Settings deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            final JsonObject o = json.getAsJsonObject();

            final Settings settings = new Settings();

            // Appearance
            final JsonObject appearance = o.getAsJsonObject(PREFS_APPEARANCE);
            if (appearance != null) {
                final JsonPrimitive laf = appearance.getAsJsonPrimitive(KEY_LOOK_AND_FEEL_CLASS_NAME);
                if (laf != null) {
                    settings.mLookAndFeelClassName = laf.getAsString();
                }
            }

            // Config
            final JsonObject config = o.getAsJsonObject(PREFS_CONFIG);
            if (config != null) {
                final JsonPrimitive ftIntegrationIntervals = config.getAsJsonPrimitive(KEY_FT_INTEGRATION_INTERVALS);
                if (ftIntegrationIntervals != null) {
                    settings.mFtIntegrationIntervalCount = ftIntegrationIntervals.getAsInt();
                }
            }

            // LOg
            final JsonObject logs = o.getAsJsonObject(PREFS_LOGS);
            if (logs != null) {
                final JsonPrimitive debug = logs.getAsJsonPrimitive(KEY_LOG_DEBUG);
                if (debug != null) {
                    settings.mLogDebug = debug.getAsBoolean();
                }

                final JsonPrimitive console = logs.getAsJsonPrimitive(KEY_LOG_TO_CONSOLE);
                if (console != null) {
                    settings.mLogToConsole = console.getAsBoolean();
                }

                final JsonPrimitive file = logs.getAsJsonPrimitive(KEY_LOG_TO_FILE);
                if (file != null) {
                    settings.mLogToFile = file.getAsBoolean();
                }
            }

            return settings;
        }
    }



    /* Actions */

    private static class ResetAllAction extends AbstractAction {

        public ResetAllAction() {
            super("Reset Settings");
            putValue(SHORT_DESCRIPTION, "Reset all preferences");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            getSingleton().resetAll();
        }
    }

    @Nullable
    private static Action sResetAllAction;

    @NotNull
    public static Action getResetAllAction() {
        Action action = sResetAllAction;
        if (action == null) {
            action = new ResetAllAction();
            sResetAllAction = action;
        }

        return action;
    }
}
