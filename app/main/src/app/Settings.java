package app;

import action.BaseAction;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import json.Json;
import misc.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ui.MusicPlayer;
import ui.AuxSoundsPlayer;
import async.Async;
import async.Canceller;
import async.TaskConsumer;
import live.ListenersI;
import live.WeakListeners;
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

        void onAppearancePreferencesChanged();

        void onFTIntegrationIntervalCountChanged(int fourierTransformSimpson13NDefault);

        void onConfigPreferencesChanged();

        void onLogPreferencesChanged();

        void onSoundPreferencesChanged();

        void onOtherPreferencesChanged();
    }


    /* ................. KEYS ................ */

    // Appearance
    private static final String PREFS_APPEARANCE = "appearance";
    private static final String KEY_LOOK_AND_FEEL_CLASS_NAME = "theme";
    private static final String KEY_DYNAMIC_COLORS = "dynamic_colors";

    // Config
    private static final String PREFS_CONFIG = "config";
    private static final String KEY_FAST_MATH_ENABLED = "fast_math_enabled";
    private static final String KEY_FT_INTEGRATION_INTERVALS = "numerical_integration_interval_count";

    // Logs
    private static final String PREFS_LOGS = "logs";
    private static final String KEY_LOG_DEBUG = "debug";
    private static final String KEY_LOG_TO_CONSOLE = "console_logging";
    private static final String KEY_LOG_TO_FILE = "file_logging";

    // Sounds
    private static final String PREFS_SOUND = "sound";
    private static final String KEY_SOUNDS_ENABLED = "aux_sounds_enabled";
    private static final String KEY_MUSIC_ENABLED = "music_enabled";

    // Others
    private static final String PREFS_OTHER = "other";
    private static final String KEY_OTHER_FLAGS = "flags";


    // Default Appearance
    public static final String DEFAULT_LOOK_AND_FEEL_CLASSNAME = FlatDarculaLaf.class.getName();
    public static final boolean DEFAULT_DYNAMIC_COLORS_ENABLED = true;

    // Default Config
    public static final boolean DEFAULT_FAST_MATH_ENABLED = MathUtil.DEFAULT_FAST_ENABLED;
    public static final int DEFAULT_FT_INTEGRATION_INTERVAL_COUNT = ComplexUtil.FOURIER_TRANSFORM_SIMPSON_13_N_DEFAULT;

    // Default sound
    public static final boolean DEFAULT_AUX_SOUNDS_ENABLED = AuxSoundsPlayer.DEFAULT_ENABLED;
    public static final boolean DEFAULT_MUSIC_ENABLED = MusicPlayer.DEFAULT_ENABLED;

    // Default Others
    public static final int OTHER_FLAG_INTRO_SHOWN = 1;
    public static final int DEFAULT_OTHER_FLAGS = 0;

    @NotNull
    public static String getCurrentLookAndFeelClassName() {
        return UIManager.getLookAndFeel().getClass().getName();
    }

    public static int getCurrentFTIntegrationIntervalCount() {
        return ComplexUtil.getFourierTransformSimpson13NCurrentDefault();
    }

    public static boolean isCurrentlyFastMathEnabled() {
        return MathUtil.isFastEnabled();
    }


    public static boolean isCurrentlyAuxSoundsEnabled() {
        return AuxSoundsPlayer.getSingleton().isEnabled();
    }

    public static boolean isCurrentlyMusicEnabled() {
        return MusicPlayer.getSingleton().isEnabled();
    }



    /* ........................... Singleton ....................... */

    @Nullable
    private static volatile Settings sInstance;

    @NotNull
    private static Settings createDefault() {
        return new Settings();
    }

    public static void init() {
        getSingleton();
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

    @Nullable
    @SerializedName(KEY_DYNAMIC_COLORS)
    private volatile Boolean mDynamicColorsEnabled;

    /* Configurations */
    @SerializedName(KEY_FAST_MATH_ENABLED)
    @Nullable
    private volatile Boolean mFastMathEnabled;

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

    /* Sounds */
    @SerializedName(KEY_SOUNDS_ENABLED)
    @Nullable
    private volatile Boolean mAuxSoundsEnabled;

    @SerializedName(KEY_MUSIC_ENABLED)
    @Nullable
    private volatile Boolean mMusicEnabled;

    /* Other */
    @SerializedName(KEY_OTHER_FLAGS)
    @Nullable
    private volatile Integer mOtherFlags;

    /* Actions */

    @Nullable
    private volatile BaseAction mDynamicColorsAction;
    @Nullable
    private volatile BaseAction mFastMathAction;

    @Nullable
    private BaseAction mResetAppearanceAction;
    @Nullable
    private BaseAction mResetConfigAction;
    @Nullable
    private BaseAction mResetSoundAction;
    @Nullable
    private BaseAction mResetAllAction;


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
        setDynamicColorsEnabled(getDynamicColorsEnabledOrDefault());

        // Config
        setFastMathEnabled(getFastMathEnabledOrDefault());
        setFTIntegrationIntervalCount(getFTIntegrationIntervalCountOrDefault());

        // sounds
        setAuxSoundsEnabled(getAuxSoundsEnabledOrDefault());
        setMusicEnabled(getMusicEnabledOrDefault());

        // Logs
        setLogDebug(getLogDebugOrDefault());
        setLogToConsole(getLogToConsoleOrDefault());
        setLogToFile(getLogToFileOrDefault());

        // Others
        setOtherFlags(getOtherFlagsOrDefault());
    }

    public void resetAppearance() {
        setLookAndFeel(DEFAULT_LOOK_AND_FEEL_CLASSNAME);
        setDynamicColorsEnabled(DEFAULT_DYNAMIC_COLORS_ENABLED);
    }

    public void resetConfig() {
        setFastMathEnabled(DEFAULT_FAST_MATH_ENABLED);
        setFTIntegrationIntervalCount(DEFAULT_FT_INTEGRATION_INTERVAL_COUNT);
    }

    public void resetSound() {
        setAuxSoundsEnabled(DEFAULT_AUX_SOUNDS_ENABLED);
        setMusicEnabled(DEFAULT_MUSIC_ENABLED);
    }

    public void resetLogs() {
        setLogDebug(Log.DEFAULT_DEBUG);
        setLogToConsole(Log.DEFAULT_LOG_TO_CONSOLE);
        setLogToFile(Log.DEFAULT_LOG_TO_FILE);
    }

    public void resetOthers() {
        setOtherFlags(DEFAULT_OTHER_FLAGS);
    }

    protected void onReset() {
        syncActions();
    }

    public void resetAll() {
        resetAppearance();
        resetConfig();
        resetSound();
        resetLogs();
        // DO NOT RESET other preferences here

        onReset();
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




    protected void onAppearancePrefsChanged() {
        mModCount++;
        syncActions();

        mListeners.dispatchOnMainThread(Listener::onAppearancePreferencesChanged);
    }


    protected void onDynamicColorsEnabledChanged(boolean dynamicColorsEnabled) {
        onAppearancePrefsChanged();
    }

    @Nullable
    public Boolean getDynamicColorsEnabled() {
        return mDynamicColorsEnabled;
    }

    public boolean getDynamicColorsEnabled(boolean defaultValue) {
        final Boolean en = mDynamicColorsEnabled;
        if (en != null)
            return en;
        return defaultValue;
    }

    public boolean getDynamicColorsEnabledOrDefault() {
        return getDynamicColorsEnabled(DEFAULT_DYNAMIC_COLORS_ENABLED);
    }

    public void setDynamicColorsEnabled(final @Nullable Boolean dynamicColorsEnabled) {
        final boolean toSet = dynamicColorsEnabled != null? dynamicColorsEnabled: DEFAULT_DYNAMIC_COLORS_ENABLED;
        final Boolean val = mDynamicColorsEnabled;
        if (val != null && val == toSet)
            return;

        mDynamicColorsEnabled = toSet;
        onDynamicColorsEnabledChanged(toSet);
    }

    public void toggleDynamicColorsEnabled() {
        setDynamicColorsEnabled(!getDynamicColorsEnabledOrDefault());
    }


    /* ................................. Config .................................... */

    protected void onConfigChanged() {
        mModCount++;
        syncActions();

        mListeners.dispatchOnMainThread(Listener::onConfigPreferencesChanged);
    }

    protected void onFastMathEnabledChanged() {
        onConfigChanged();
    }

    @Nullable
    public Boolean getFastMathEnabled() {
        return mFastMathEnabled;
    }

    public boolean getFastMathEnabled(boolean defaultValue) {
        final Boolean val = mFastMathEnabled;
        if (val != null)
            return val;
        return defaultValue;
    }

    public boolean getFastMathEnabledOrDefault() {
        return getFastMathEnabled(DEFAULT_FAST_MATH_ENABLED);
    }

    public void setFastMathEnabled(boolean enabled) {
        final boolean changed = MathUtil.setFastEnabled(enabled);
        mFastMathEnabled = MathUtil.isFastEnabled();

        if (changed) {
            onFastMathEnabledChanged();
        }
    }

    public void toggleFastMathEnabled() {
        setFastMathEnabled(!getFastMathEnabledOrDefault());
    }



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
        mModCount++;
        syncActions();

        mListeners.dispatchOnMainThread(Listener::onLogPreferencesChanged);
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


    /* .................................. Sounds ...................................... */

    protected void onSoundPrefsChanged() {
        mModCount++;
        syncActions();

        mListeners.dispatchOnMainThread(Listener::onSoundPreferencesChanged);
    }

    @Nullable
    public Boolean getAuxSoundsEnabled() {
        return mAuxSoundsEnabled;
    }

    public boolean getAuxSoundsEnabled(boolean defaultValue) {
        final Boolean en = mAuxSoundsEnabled;
        if (en != null)
            return en;
        return defaultValue;
    }

    public boolean getAuxSoundsEnabledOrDefault() {
        return getAuxSoundsEnabled(DEFAULT_AUX_SOUNDS_ENABLED);
    }

    public void setAuxSoundsEnabled(boolean soundsEnabled) {
        boolean changed = AuxSoundsPlayer.getSingleton().setEnabled(soundsEnabled);
        mAuxSoundsEnabled = AuxSoundsPlayer.getSingleton().isEnabled();
        if (changed) {
            onSoundPrefsChanged();
        }
    }


    @Nullable
    public Boolean getMusicEnabled() {
        return mMusicEnabled;
    }

    public boolean getMusicEnabled(boolean defaultValue) {
        final Boolean en = mMusicEnabled;
        if (en != null)
            return en;
        return defaultValue;
    }

    public boolean getMusicEnabledOrDefault() {
        return getMusicEnabled(DEFAULT_MUSIC_ENABLED);
    }

    public void setMusicEnabled(boolean musicEnabled) {
        boolean changed = MusicPlayer.getSingleton().setEnabled(musicEnabled);
        mMusicEnabled = MusicPlayer.getSingleton().isEnabled();
        if (changed) {
            onSoundPrefsChanged();
        }
    }


    /* ...............................  Others  ............................. */

    protected void onOtherPreferencesChanged() {
        mModCount++;
        syncActions();

        mListeners.dispatchOnMainThread(Listener::onOtherPreferencesChanged);
    }

    @Nullable
    public Integer getOtherFlags() {
        return mOtherFlags;
    }

    public int getOtherFlags(int defaultValue) {
        final Integer en = mOtherFlags;
        if (en != null)
            return en;
        return defaultValue;
    }

    public int getOtherFlagsOrDefault() {
        return getOtherFlags(DEFAULT_OTHER_FLAGS);
    }

    public void setOtherFlags(int otherFlags) {
        final Integer prev = mOtherFlags;
        boolean changed = prev == null || Flaggable.areFlagsDifferent(prev, otherFlags);
        mOtherFlags = otherFlags;
        if (changed) {
            onOtherPreferencesChanged();
        }
    }

    public void addOtherFlags(int flags) {
        setOtherFlags(getOtherFlags(0) | flags);
    }

    public void removeOtherFlags(int flags) {
        setOtherFlags(Flaggable.removeFlags(getOtherFlags(0), flags));
    }

    public boolean containsOtherFlag(int flag, boolean defaultValue) {
        final Integer prev = mOtherFlags;
        if (prev == null)
            return defaultValue;

        return Flaggable.hasAllFlags(prev, flag);
    }


    /* Actions */

    @NotNull
    public Action getToggleFastMathAction() {
        BaseAction action = mFastMathAction;
        if (action == null) {
            synchronized (this) {
                action = mFastMathAction;
                if (action == null) {
                    action = new FastMathToggleAction();
                    mFastMathAction = action;
                }
            }
        }

        return action;
    }

    @NotNull
    public Action getToggleDynamicColorsAction() {
        BaseAction action = mDynamicColorsAction;
        if (action == null) {
            synchronized (this) {
                action = mDynamicColorsAction;
                if (action == null) {
                    action = new DynamicColorsAction();
                    mDynamicColorsAction = action;
                }
            }
        }

        return action;
    }

    @NotNull
    public Action getResetAppearanceAction() {
        BaseAction action = mResetAppearanceAction;
        if (action == null) {
            synchronized (this) {
                action = mResetAppearanceAction;
                if (action == null) {
                    action = new ResetAppearanceAction();
                    mResetAppearanceAction = action;
                }
            }
        }

        return action;
    }

    @NotNull
    public Action getResetConfigAction() {
        BaseAction action = mResetConfigAction;
        if (action == null) {
            synchronized (this) {
                action = mResetConfigAction;
                if (action == null) {
                    action = new ResetConfigAction();
                    mResetConfigAction = action;
                }
            }
        }

        return action;
    }

    @NotNull
    public Action getResetSoundAction() {
        BaseAction action = mResetSoundAction;
        if (action == null) {
            synchronized (this) {
                action = mResetSoundAction;
                if (action == null) {
                    action = new ResetSoundAction();
                    mResetSoundAction = action;
                }
            }
        }

        return action;
    }

    @NotNull
    public Action getResetLogsAction() {
        return Log.getResetAction();
    }

    @NotNull
    public Action getResetAllAction() {
        BaseAction action = mResetAllAction;
        if (action == null) {
            synchronized (this) {
                action = mResetAllAction;
                if (action == null) {
                    action = new ResetAllAction();
                    mResetAllAction = action;
                }
            }
        }

        return action;
    }

    protected void syncActions() {
        final BaseAction fma = mDynamicColorsAction;
        if (fma != null) {
            fma.setSelected(getFastMathEnabledOrDefault());
        }

        final BaseAction dca = mDynamicColorsAction;
        if (dca != null) {
            dca.setSelected(getDynamicColorsEnabledOrDefault());
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
            appearance.addProperty(KEY_DYNAMIC_COLORS, src.getDynamicColorsEnabledOrDefault());

            // Config
            final JsonObject config = new JsonObject();
            config.addProperty(KEY_FAST_MATH_ENABLED, isCurrentlyFastMathEnabled());
            config.addProperty(KEY_FT_INTEGRATION_INTERVALS, getCurrentFTIntegrationIntervalCount());

            // Sound
            final JsonObject sound = new JsonObject();
            sound.addProperty(KEY_SOUNDS_ENABLED, isCurrentlyAuxSoundsEnabled());
            sound.addProperty(KEY_MUSIC_ENABLED, isCurrentlyMusicEnabled());

            // LOGS
            final JsonObject logs = new JsonObject();
            logs.addProperty(KEY_LOG_DEBUG, Log.isDebugEnabled());
            logs.addProperty(KEY_LOG_TO_CONSOLE, Log.isLoggingToConsole());
            logs.addProperty(KEY_LOG_TO_FILE, Log.isLoggingToFile());

            // Others
            final JsonObject others = new JsonObject();
            others.addProperty(KEY_OTHER_FLAGS, src.getOtherFlagsOrDefault());

            // Finalizing
            final JsonObject settings = new JsonObject();
            settings.add(PREFS_APPEARANCE, appearance);
            settings.add(PREFS_CONFIG, config);
            settings.add(PREFS_SOUND, sound);
            settings.add(PREFS_LOGS, logs);
            settings.add(PREFS_OTHER, others);
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

                final JsonPrimitive dynamicColors = appearance.getAsJsonPrimitive(KEY_DYNAMIC_COLORS);
                if (dynamicColors != null) {
                    settings.mDynamicColorsEnabled = dynamicColors.getAsBoolean();
                }
            }

            // Config
            final JsonObject config = o.getAsJsonObject(PREFS_CONFIG);
            if (config != null) {
                final JsonPrimitive fastMath = config.getAsJsonPrimitive(KEY_FAST_MATH_ENABLED);
                if (fastMath != null) {
                    settings.mFastMathEnabled = fastMath.getAsBoolean();
                }

                final JsonPrimitive ftIntegrationIntervals = config.getAsJsonPrimitive(KEY_FT_INTEGRATION_INTERVALS);
                if (ftIntegrationIntervals != null) {
                    settings.mFtIntegrationIntervalCount = ftIntegrationIntervals.getAsInt();
                }
            }

            // Sound
            final JsonObject sound = o.getAsJsonObject(PREFS_SOUND);
            if (sound != null) {
                final JsonPrimitive auxSounds = sound.getAsJsonPrimitive(KEY_SOUNDS_ENABLED);
                if (auxSounds != null) {
                    settings.mAuxSoundsEnabled = auxSounds.getAsBoolean();
                }

                final JsonPrimitive music = sound.getAsJsonPrimitive(KEY_MUSIC_ENABLED);
                if (music != null) {
                    settings.mMusicEnabled = music.getAsBoolean();
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

            // Others
            final JsonObject others = o.getAsJsonObject(PREFS_OTHER);
            if (others != null) {
                final JsonPrimitive flags = others.getAsJsonPrimitive(KEY_OTHER_FLAGS);
                if (flags != null) {
                    settings.mOtherFlags = flags.getAsInt();
                }
            }

            return settings;
        }
    }



    /* Actions */

    private class DynamicColorsAction extends BaseAction {

        private DynamicColorsAction() {
            setName("Dynamic Colors");
            setShortDescription("Toggle dynamic colors in function drawing");
            setSelected(getDynamicColorsEnabledOrDefault());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            toggleDynamicColorsEnabled();
        }
    }


    private class FastMathToggleAction extends BaseAction {

        private FastMathToggleAction() {
            setName("Fast Math");
            setShortDescription("Enables fast computations of e^x, sin, cos etc with expense of accuracy");
            setSelected(getFastMathEnabledOrDefault());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            toggleFastMathEnabled();
        }
    }


    private class ResetAppearanceAction extends BaseAction {

        public ResetAppearanceAction() {
            setName("Reset Appearance");
            setShortDescription("Reset appearance preferences");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            resetAppearance();
        }
    }


    private class ResetConfigAction extends BaseAction {

        public ResetConfigAction() {
            setName("Reset Configuration");
            setShortDescription("Reset configuration preferences");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            resetConfig();
        }
    }

    private class ResetSoundAction extends BaseAction {

        public ResetSoundAction() {
            setName("Reset Sound Settings");
            setShortDescription("Reset sound and music preferences");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            resetSound();
        }
    }



    private class ResetAllAction extends BaseAction {

        public ResetAllAction() {
            setName("Reset Settings");
            setShortDescription("Reset all preferences");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            resetAll();
        }
    }

}
