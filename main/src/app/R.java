package app;

import com.formdev.flatlaf.*;
import com.formdev.flatlaf.intellijthemes.FlatArcOrangeIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatDarkPurpleIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDarkerIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialLighterIJTheme;
import function.definition.ComplexDomainFunctionI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import provider.FunctionMeta;
import provider.FunctionProviderI;
import provider.FunctionType;
import provider.PathFunctionProvider;
import ui.audio.AudioController;
import util.*;
import util.async.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FontUIResource;
import javax.tools.JavaFileObject;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class R {

    public static final String TAG = "Resources";

    @NotNull
    public static final List<UIManager.LookAndFeelInfo> LOOK_AND_FEELS_INTERNAL;
    @NotNull
    public static final List<UIManager.LookAndFeelInfo> LOOK_AND_FEELS_FLAT_LAF;
    @NotNull
    public static final List<UIManager.LookAndFeelInfo> LOOK_AND_FEELS_FLAT_LAF_MATERIAL;

    static {
        // internal LAF's
        final UIManager.LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
        LOOK_AND_FEELS_INTERNAL = Arrays.asList(infos);

        // Main LAF's
        LOOK_AND_FEELS_FLAT_LAF = new ArrayList<>();
        LOOK_AND_FEELS_FLAT_LAF.add(new UIManager.LookAndFeelInfo(FlatLightLaf.NAME, FlatLightLaf.class.getName()));
        LOOK_AND_FEELS_FLAT_LAF.add(new UIManager.LookAndFeelInfo(FlatIntelliJLaf.NAME, FlatIntelliJLaf.class.getName()));
        LOOK_AND_FEELS_FLAT_LAF.add(new UIManager.LookAndFeelInfo(FlatDarkLaf.NAME, FlatDarkLaf.class.getName()));
        LOOK_AND_FEELS_FLAT_LAF.add(new UIManager.LookAndFeelInfo(FlatDarculaLaf.NAME, FlatDarculaLaf.class.getName()));

        // Material LAF's
        LOOK_AND_FEELS_FLAT_LAF_MATERIAL = new ArrayList<>();
        LOOK_AND_FEELS_FLAT_LAF_MATERIAL.add(new UIManager.LookAndFeelInfo(FlatMaterialLighterIJTheme.NAME, FlatMaterialLighterIJTheme.class.getName()));
        LOOK_AND_FEELS_FLAT_LAF_MATERIAL.add(new UIManager.LookAndFeelInfo(FlatMaterialDarkerIJTheme.NAME, FlatMaterialDarkerIJTheme.class.getName()));
        LOOK_AND_FEELS_FLAT_LAF_MATERIAL.add(new UIManager.LookAndFeelInfo(FlatOneDarkIJTheme.NAME, FlatOneDarkIJTheme.class.getName()));
        LOOK_AND_FEELS_FLAT_LAF_MATERIAL.add(new UIManager.LookAndFeelInfo(FlatArcOrangeIJTheme.NAME, FlatArcOrangeIJTheme.class.getName()));
        LOOK_AND_FEELS_FLAT_LAF_MATERIAL.add(new UIManager.LookAndFeelInfo(FlatDarkPurpleIJTheme.NAME, FlatDarkPurpleIJTheme.class.getName()));

        // Install
        LOOK_AND_FEELS_FLAT_LAF.forEach(UIManager::installLookAndFeel);
        LOOK_AND_FEELS_FLAT_LAF_MATERIAL.forEach(UIManager::installLookAndFeel);
    }


    public static void init() {
        ensureDirs();
        Log.setLogsDir(DIR_LOGS);

//        final Font defaultFont = getFontPdSansRegular();
//        if (defaultFont != null) {
//            setDefaultFont(new FontUIResource(defaultFont.deriveFont(12f)));
//        }
    }

    public static void finish() {

    }

    public static final String COMMENT_TOKEN = "#";
    
    public static final String DISPLAY_NAME_FUNCTION_NOOP = "--select--";
    public static final String DISPLAY_NAME_FUNCTION_UNKNOWN = "Unknown Function";

    /* External Files */
    public static final boolean LOAD_EXTERNAL_PATH_FUNCTIONS_ON_START = false;

    public static final String FUNCTION_STATE_SAVE_FILE_EXTENSION = ".json";
    public static final String FUNCTION_STATE_SAVE_FILE_DESCRIPTION = "Function State";
    public static final FileFilter FUNCTION_STATE_SAVE_FILE_FILTER = new OpenFileFilter(FUNCTION_STATE_SAVE_FILE_EXTENSION, FUNCTION_STATE_SAVE_FILE_DESCRIPTION);

    public static final String EXT_ROTOR_STATES_CSV_FILE_EXTENSION = ".csv";
    public static final String EXT_ROTOR_STATES_CSV_FILE_DESCRIPTION = "Rotor States";
    public static final FileFilter EXT_ROTOR_STATES_CSV_FILE_FILTER = new OpenFileFilter(EXT_ROTOR_STATES_CSV_FILE_EXTENSION, EXT_ROTOR_STATES_CSV_FILE_DESCRIPTION);

    public static final String PATH_DATA_FILE_EXTENSION = ".pd";
    public static final String PATH_DATA_FILE_DESCRIPTION = "Path Data";
    public static final FileFilter PATH_DATA_FILE_FILTER = new OpenFileFilter(PATH_DATA_FILE_EXTENSION, PATH_DATA_FILE_DESCRIPTION);

    public static final boolean DEFAULT_VALIDATE_EXTERNAL_FILES = true;
    public static final Charset ENCODING = StandardCharsets.UTF_8;

    /* Dir Structure */
    public static final Path DIR_MAIN = Path.of("").toAbsolutePath();
    public static final Path DIR_EXTERNAL_PROGRAMS = DIR_MAIN.resolve("PROGRAMS");
    public static final Path DIR_EXTERNAL_PATH_FUNCTIONS = DIR_MAIN.resolve("PATH_FUNCTIONS");
    public static final Path DIR_FUNCTION_STATE_SAVES = DIR_MAIN.resolve("FUNCTION STATES");
    public static final Path DIR_EXPORTS = DIR_MAIN.resolve("EXPORTS");
    public static final Path DIR_LOGS = DIR_MAIN.resolve("logs");


    /* .................... Resources ....................... */

    public static final Path DIR_RES = DIR_MAIN.resolve("res");
    public static final Path SETTINGS_FILE = DIR_RES.resolve("settings.json");


    // Images
    public static final Path DIR_IMAGE = DIR_RES.resolve("image");
    public static final Path APP_ICON = DIR_IMAGE.resolve("icon.png");

    // Fonts
    public static final Path DIR_FONT = DIR_RES.resolve("font");
    public static final Path FONT_FILE_AQUIRE = DIR_FONT.resolve("aquire.otf");
    public static final Path FONT_FILE_PD_SANS_REGULAR = DIR_FONT.resolve("product_sans_regular.ttf");

    public static void setDefaultFont(@NotNull FontUIResource fontUIResource) {
        int modCount = 0;

        final UIDefaults uiDef = UIManager.getDefaults();
        if (uiDef != null) {
            for (Map.Entry<Object, Object> e: uiDef.entrySet()) {
                if (uiDef.get(e.getKey()) instanceof FontUIResource) {
                    uiDef.put(e.getKey(), fontUIResource);
                    modCount++;
                }
            }
        }

        final UIDefaults lafDef = UIManager.getDefaults();
        if (lafDef != null) {
            for (Map.Entry<Object, Object> e: lafDef.entrySet()) {
                if (lafDef.get(e.getKey()) instanceof FontUIResource) {
                    lafDef.put(e.getKey(), fontUIResource);
                    modCount++;
                }
            }
        }

        if (modCount > 0) {
            App.updateAllFramesTree();
        }
    }

    @Nullable
    private static Map<String, Font> sLoadedFonts;

    private static void onFontLoaded(@NotNull String path, @NotNull Font font) {
        final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        env.registerFont(font);
    }

    @Nullable
    private static synchronized Font loadFont(@NotNull Path path) {
        final String filePath = path.toAbsolutePath().toString();

        Map<String, Font> cache = sLoadedFonts;
        Font font;
        if (cache != null && (font = cache.get(filePath)) != null) {
            return font;
        }

        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File(filePath));
            if (cache == null) {
                cache = new HashMap<>();
                sLoadedFonts = cache;
            }

            cache.put(filePath, font);
            onFontLoaded(filePath, font);
            return font;
        } catch (Throwable t) {
            Log.e(TAG, "Failed to load font from file: " + path, t);
        }

        return null;
    }

    @Nullable
    public static Font getFontPdSansRegular() {
        return loadFont(FONT_FILE_PD_SANS_REGULAR);
    }

    @Nullable
    public static Font getFontAquire() {
        return loadFont(FONT_FILE_AQUIRE);
    }



    // Sound
    public static final Path DIR_SOUND = DIR_RES.resolve("sound");
    public static final Path SOUND_FILE_CLICK = DIR_SOUND.resolve("test.wav");

    public static final AudioController AUDIO_CONTROLLER = new AudioController();

    public static void playSoundClick() {
        AUDIO_CONTROLLER.play(1232, url(SOUND_FILE_CLICK), 5, AudioController.ExistsStrategy.KEEP);
    }


    @NotNull
    public static URL url(@NotNull Path path) {
        try {
            return path.toUri().toURL();
        } catch (Throwable t) {
            final AssertionError error = new AssertionError("Failed to create URL from Path: " + path, t);
            Log.e(TAG, error.getMessage(), error.getCause());
            throw error;
        }
    }

    public static boolean ensureLogsDir() {
        return FileUtil.ensureDir(DIR_LOGS);
    }

    public static boolean ensureResDir() {
        return FileUtil.ensureDir(DIR_RES);
    }

    public static boolean ensureFunctionStateSaveDir() {
        return FileUtil.ensureDir(DIR_FUNCTION_STATE_SAVES);
    }

    public static boolean ensureExternalProgramsDir() {
        return FileUtil.ensureDir(DIR_EXTERNAL_PROGRAMS);
    }

    public static boolean ensureExternalPathFunctions() {
        return FileUtil.ensureDir(DIR_EXTERNAL_PATH_FUNCTIONS);
    }

    public static boolean ensureExportsDir() {
        return FileUtil.ensureDir(DIR_EXPORTS);
    }

    public static void ensureDirs() {
        ensureResDir();
        ensureFunctionStateSaveDir();
        ensureExternalProgramsDir();
        ensureExternalPathFunctions();
        ensureExportsDir();
        ensureLogsDir();
    }

    /* App Resources */

    @Nullable
    public static Image createIcon(@NotNull Path path) {
        if (Files.exists(path)) {
            try {
                return Toolkit.getDefaultToolkit().createImage(path.toString());
            } catch (Throwable t) {
                Log.e(TAG, "failed to create image from file <" + path + ">", t);
            }
        }

        return null;
    }

    @Nullable
    public static Image createAppIcon() {
        return createIcon(APP_ICON);
    }



    /* Rotor States DUmp */

    @Nullable
    public static Path createRotorStatesDumpFile(@Nullable String funcName) {
        if (!ensureFunctionStateSaveDir())
            return null;

        if (Format.isEmpty(funcName)) {
            funcName = DISPLAY_NAME_FUNCTION_UNKNOWN;
        }

        return FileUtil.getNonExistingFile(DIR_FUNCTION_STATE_SAVES.resolve(funcName + FUNCTION_STATE_SAVE_FILE_EXTENSION));
    }


    /* .............................. External Functions ..................................... */

    /**
     * Delimits multiple shapes path data in a single file
     * */
    public static final String PATH_DATA_SHAPES_DELIMITER = "[|]";

    public static final String DISPLAY_NAME_TOKEN_EXT_PATH = "ext_path";
    public static final String DISPLAY_NAME_TOKEN_EXT_PROGRAM = "ext_program";
    public static final String DISPLAY_NAME_TOKEN_LOADED = "loaded";

    @NotNull
    public static String displayNameWithToken(@NotNull String fullName, @NotNull String token) {
        final String name = FileUtil.getName(fullName);
        if (name.contains(token)) {
            return name;
        }

        if (name.charAt(name.length() - 1) == ')') {
            String temp = name.substring(0, name.length() - 1);
            return temp + "-" + token + ")";
        }

        return name + " (" + token + ")";
    }

    @NotNull
    public static String createExternalPathFunctionDisplayName(@NotNull String fullName) {
        return displayNameWithToken(fullName, DISPLAY_NAME_TOKEN_EXT_PATH);
    }

    @NotNull
    public static String createExternalProgramFunctionDisplayName(@NotNull String fullName) {
        return displayNameWithToken(fullName, DISPLAY_NAME_TOKEN_EXT_PROGRAM);
    }

    @NotNull
    public static String createExternallyLoadedFunctionDisplayName(@NotNull String fullName) {
        return displayNameWithToken(fullName, DISPLAY_NAME_TOKEN_LOADED);
    }


    public static boolean isValidPathData(@NotNull String pathData) {
        return !pathData.isBlank();
    }

    public static boolean isValidPathDataFile(@NotNull Path path) {
        return path.toString().endsWith(PATH_DATA_FILE_EXTENSION);
    }

    public static boolean isValidRotorStatesFile(@NotNull Path path) {
        return path.toString().endsWith(FUNCTION_STATE_SAVE_FILE_EXTENSION);
    }

    @Nullable
    public static FunctionProviderI loadExternalPathFunction(@NotNull String functionName, @NotNull String pathData) {
        if (Format.isEmpty(pathData))
            return null;

        pathData = Format.removeAllLinedComments(pathData, COMMENT_TOKEN, true);
        if (Format.isEmpty(pathData))
            return null;

        final String[] paths = pathData.split(PATH_DATA_SHAPES_DELIMITER);     // splits shapes
        final String[] parsed = Stream.of(paths).filter(R::isValidPathData).toArray(String[]::new);

        if (parsed.length > 0) {
            return new PathFunctionProvider(new FunctionMeta(FunctionType.EXTERNAL_PATH, functionName), parsed);
        }

        return null;
    }

    @Nullable
    public static FunctionProviderI loadExternalPathFunction(@NotNull Path file, boolean validateFile) {
        try {
            if (validateFile && !isValidPathDataFile(file)) {
                throw new IllegalArgumentException("Invalid file type");
            }

            final String pathData = Files.readString(file, StandardCharsets.UTF_8);
            return loadExternalPathFunction(createExternalPathFunctionDisplayName(file.getFileName().toString()), pathData);
        } catch (Throwable t) {
            Log.e(TAG, "Exception while loading external function from file <" + file + ">", t);
        }

        return null;
    }


    @Nullable
    public static FunctionProviderI[] loadExternalPathFunctions(Path[] files, boolean validateFiles, @Nullable CancellationProvider c) {
        if (files == null || files.length == 0)
            return null;

        final FunctionProviderI[] providers = new FunctionProviderI[files.length];
        for (int i=0; i < files.length; i++) {
            if (c != null && c.isCancelled())
                break;
            providers[i] = loadExternalPathFunction(files[0], validateFiles);
        }

        return providers;
    }

    @NotNull
    public static Canceller loadExternalPathFunctionsAsync(Path[] files, boolean validateFiles, @NotNull Consumer<FunctionProviderI[]> callback) {
        final Async.CExecutor exe = new Async.CExecutor();
        exe.execute(c -> loadExternalPathFunctions(files, validateFiles, c), callback);
        return exe;
    }

    @NotNull
    public static ExternalProgramFunction compileAndLoadExternalProgramFunction(@NotNull ExternalJava.Location location) throws
            ExternalJava.CompilationException,
            NoSuchMethodException,
            InvocationTargetException,
            InstantiationException,
            IllegalAccessException,
            IllegalArgumentException,
            ClassCastException {

        final Class<?> clazz = ExternalJava.compileAndLoadClass(new ExternalJava.JavaObject(location, JavaFileObject.Kind.SOURCE), true);
        if (!ComplexDomainFunctionI.class.isAssignableFrom(clazz)) {
            throw new ClassCastException("Function is not an instance of " + ComplexDomainFunctionI.class.getSimpleName());
        }

        final ComplexDomainFunctionI func = (ComplexDomainFunctionI) clazz.getDeclaredConstructor().newInstance();
        return new ExternalProgramFunction(func, location);
    }


    public static class LoadResult extends DirStat {

        @NotNull
        private final List<FunctionProviderI> providers = new ArrayList<>();

        @NotNull
        public List<FunctionProviderI> getFunctionProviders() {
            return providers;
        }

        private void addFunctionProvider(@NotNull FunctionProviderI provider) {
            providers.add(provider);
        }
    }

    @Nullable
    @Unmodifiable
    public static LoadResult loadExternalPathFunctions(@NotNull Path dir, boolean validateFiles, @Nullable CancellationProvider c) {
        if (!Files.isDirectory(dir))
            return null;

        final LoadResult result = new LoadResult();
        final FileVisitor<Path> visitor = new FileVisitor<>() {

            private FileVisitResult result() {
                return (c != null && c.isCancelled())? FileVisitResult.TERMINATE: FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                result.addDir();
                return result();
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                boolean success = false;
                if (!validateFiles || isValidPathDataFile(file)) {
                    final FunctionProviderI func = loadExternalPathFunction(file, false);
                    if (func != null) {
                        result.addFunctionProvider(func);
                        success = true;
                    }
                }

                result.addFile(success, attrs.size());
                return result();
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return result();
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return result();
            }
        };

        try {
            Files.walkFileTree(dir, visitor);
        } catch (Throwable t) {
            Log.e(TAG, "Exception while loading external functions from dir <" + dir + ">", t);
        }

        return result;
    }


    @NotNull
    public static Canceller loadExternalPathFunctionsAsync(@NotNull Path dir, boolean validateFiles, @NotNull Consumer<LoadResult> callback) {
        final Async.CExecutor exe = new Async.CExecutor();
        exe.execute((CancellationProvider c) -> loadExternalPathFunctions(dir, validateFiles, c), callback);
        return exe;
    }



    /* ENUMS */

    @NotNull
    public static String dumpEnum(@Nullable Enum<?> value) {
        return value != null? value.toString(): "";
    }

    @Nullable
    public static <T extends Enum<T>> T loadEnum(@NotNull Class<T> clazz, @Nullable String name) {
        return Format.isEmpty(name)? null: Enum.valueOf(clazz, name);
    }


    /* ......................  Strings ............................. */

    @NotNull
    public static String getFunctionProviderLabelText() {
        return "Function";
    }

    @NotNull
    public static String getFunctionProviderShortDescription() {
        return "Select a function provider";
    }


    @NotNull
    public static String getWaveToggleText(boolean drawingWave) {
        return "Wave";
    }

    @NotNull
    public static String getWaveToggleShortDescription(boolean drawingWave) {
        return (drawingWave? "show as graph": "show as wave") + " [W]";
    }

    @NotNull
    public static String getInvertYText() {
        return "Invert-Y";
    }

    @NotNull
    public static String getInvertYShortDescription() {
        return "Invert Y-axis";
    }

    @NotNull
    public static String getInvertXText() {
        return "Invert-X";
    }

    @NotNull
    public static String getInvertXShortDescription() {
        return "Invert X-axis";
    }

    @NotNull
    public static String getFTDrawAsLiveText() {
        return "Draw Live";
    }

    @NotNull
    public static String getFTDrawAsLiveShortDescription() {
        return "Draw only processed states";
    }


    @NotNull
    public static String getDrawSmoothCurveText() {
        return "Smooth Curve";
    }

    @NotNull
    public static String getDrawSmoothCurveShortDescription() {
        return "use Cubic Bezier Interpolation";
    }


    @NotNull
    public static String getGraphInCenterText() {
        return "Center";
    }

    @NotNull
    public static String getGraphInCenterShortDescription() {
        return "Graph in center [C]";
    }

    @NotNull
    public static String getDrawAxisText() {
        return "Draw Axis";
    }

    @NotNull
    public static String getDrawAxisShortDescription() {
        return "Draw X and Y axes";
    }

    @NotNull
    public static String getDrawCOMText() {
        return "Draw COM";
    }

    @NotNull
    public static String getDrawCOMShortDescription() {
        return "Draw Center of Mass";
    }


    @NotNull
    public static String getPointsJoiningText() {
        return "Join Points";
    }

    @NotNull
    public static String getPointsJoiningShortDescription() {
        return "Join points with lines";
    }

    @NotNull
    public static String getHueCycleText() {
        return "Color Cycle";
    }

    @NotNull
    public static String getHueCycleShortDescription() {
        return "Set color cycle override";
    }

    @NotNull
    public static String getAutoTrackInCenterText() {
        return "Auto Track";
    }

    @NotNull
    public static String getAutoTrackInCenterShortDescription() {
        return "Tracks the graph while drawing";
    }


    @NotNull
    public static String getRepeatModeLabelText() {
        return "On End";
    }

    @NotNull
    public static String getRepeatModeShortDescription() {
        return "Configure end behaviour";
    }

    @NotNull
    public static String getToggleControlsText(boolean controlsShown) {
        return (controlsShown? "Hide": "Show") + " Dock";
    }

    @NotNull
    public static String getToggleControlsShortDescription(boolean controlsShown) {
        return (controlsShown? "Hide": "Show") + " Controls Dock [Ctrl-C]";
    }

    @NotNull
    public static String getToggleMenuBarText(boolean menuVisible) {
        return (menuVisible? "Hide": "Show") + " Menu";
    }

    @NotNull
    public static String getToggleMenuBarShortDescription(boolean menuVisible) {
        return (menuVisible? "Hide": "Show") + " Menu [Ctrl-M]";
    }

    @NotNull
    public static String getConfigureRotorFrequencyProviderText() {
        return "Frequency Provider";
    }

    @NotNull
    public static String getConfigureRotorFrequencyProviderShortDescription() {
        return "Configure how frequencies for rotors are chosen";
    }


    @NotNull
    public static String getPlayPauseText(boolean playing) {
        return playing? "Pause": "Play";
    }

    @NotNull
    public static String getPlayPauseShortDescription(boolean playing) {
        return (playing? "Pause Rotors": "Start Rotors") + " [SPACE]";
    }

    @NotNull
    public static String getStopText() {
        return "Stop";
    }

    @NotNull
    public static String getStopShortDescription() {
        return "Stop Rotors [ESCAPE]";
    }

    @NotNull
    public static String getResetMainText() {
        return "Reset";
    }

    @NotNull
    public static String getResetMainShortDescription() {
        return "Reset Rotors [R]";
    }

    @NotNull
    public static String getResetScaleText() {
        return "Reset Zoom";
    }

    @NotNull
    public static String getResetScaleShortDescription() {
        return "Reset Rotors Zoom Scale";
    }

    @NotNull
    public static String getResetScaleAndDragText() {
        return "Reset View";
    }

    @NotNull
    public static String getResetScaleAndDragShortDescription() {
        return "Reset viewport [Shift-R]";
    }

    @NotNull
    public static String getResetFullText() {
        return "Full Reset";
    }

    @NotNull
    public static String getResetFullShortDescription() {
        return "Reset View and Rotors [Ctrl-R]";
    }


    @NotNull
    public static String getRotorCountText(int count) {
        return "Rotors: " + count;
    }

    @NotNull
    public static String getRotorCountSliderShortDescription() {
        return "change no of rotors";
    }

    @NotNull
    public static String getCurrentRotorText(int index, @Nullable Double freq) {
        if (index < 0)
            return "";

        String text = "Rotor " + index;
        if (freq != null) {
            text += String.format(" : %.2f", freq);
        }

        return text;
    }

    @NotNull
    public static String getCurrentRotorSliderShortDescription() {
        return "change current rotor";
    }


    @NotNull
    public static String getSpeedPercentText(int percent) {
        return "Speed: " + percent + "%";
    }

    @NotNull
    public static String getSpeedSliderShortDescription() {
        return "change speed";
    }
    
    @NotNull
    public static String getScaleText(double scale) {
        return "Scale: " + ((int) Math.round(scale * 100)) + "%";
    }

    @NotNull
    public static String getScaleText(boolean inc) {
        return inc? "+": "-";
    }
    
    @NotNull
    public static String getScaleShortDescription(boolean inc) {
        return inc? "Zoom In [Shift-UP]": "Zoom Out [Shift-DOWN]";
    }

    @NotNull
    public static String getFullscreenText() {
        return "Fullscreen";
    }

    @NotNull
    public static String getFullscreenText(boolean isFullscreen) {
        return isFullscreen? "Exit Fullscreen": "Fullscreen";
    }

    @NotNull
    public static String getFullscreenShortDescription(boolean isFullscreen) {
        return (isFullscreen? "Exit": "Enter") + " Fullscreen [Ctrl-F]";
    }

    @Nullable
    public static String getStatusText(boolean loading, int pendingRotorCount) {
        if (!loading)
            return null;

        return "Loading" + (pendingRotorCount > 0? " " + pendingRotorCount + " Rotors": "...");
    }


}
