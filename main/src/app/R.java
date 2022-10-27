package app;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import provider.FunctionMeta;
import provider.FunctionProviderI;
import provider.FunctionType;
import provider.PathFunctionProvider;
import util.*;
import util.async.Async;
import util.async.CancellationProvider;
import util.async.Canceller;
import util.async.Consumer;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class R {

    public static final String TAG = "Resources";

    public static void init() {
        ensureDirs();

        String lookAndFeel = null;
        try {
            lookAndFeel = UIManager.getSystemLookAndFeelClassName();
            if (Format.notEmpty(lookAndFeel)) {
                UIManager.setLookAndFeel(lookAndFeel);
            } else {
                throw new NullPointerException("Could not found system look and feel");
            }
        } catch (Throwable t) {
            Log.e(TAG, "Failed to set look and feel: " + lookAndFeel, t);
        }
    }


    public static final String COMMENT_TOKEN = "#";
    
    public static final String DISPLAY_NAME_FUNCTION_NOOP = "--select--";
    public static final String DISPLAY_NAME_FUNCTION_UNKNOWN = "Unknown Function";

    /* External Files */
    public static final boolean LOAD_EXTERNAL_PATH_FUNCTIONS_ON_START = false;

    public static final String ROTOR_STATE_DUMP_FILE_EXTENSION = ".json";
    public static final String ROTOR_STATE_DUMP_FILE_DESCRIPTION = "Rotor States";
    public static final FileFilter ROTOR_STATE_DUMP_FILE_FILTER = new OpenFileFilter(ROTOR_STATE_DUMP_FILE_EXTENSION, ROTOR_STATE_DUMP_FILE_DESCRIPTION);

    public static final String PATH_DATA_FILE_EXTENSION = ".pd";
    public static final String PATH_DATA_FILE_DESCRIPTION = "Path Data";
    public static final FileFilter PATH_DATA_FILE_FILTER = new OpenFileFilter(PATH_DATA_FILE_EXTENSION, PATH_DATA_FILE_DESCRIPTION);

    public static final boolean DEFAULT_VALIDATE_EXTERNAL_FILES = true;
    public static final Charset ENCODING = StandardCharsets.UTF_8;

    /* Dir Structure */
    public static final Path DIR_MAIN = Path.of("").toAbsolutePath();
    public static final Path DIR_EXTERNAL_PROGRAMS = DIR_MAIN.resolve("PROGRAMS");
    public static final Path DIR_EXTERNAL_PATH_FUNCTIONS = DIR_MAIN.resolve("PATH_FUNCTIONS");
    public static final Path DIR_ROTOR_STATE_DUMPS = DIR_MAIN.resolve("SAVES");

    public static final Path DIR_RES = DIR_MAIN.resolve("res");
    public static final Path DIR_IMAGE = DIR_RES.resolve("image");
    public static final Path APP_ICON = DIR_IMAGE.resolve("icon.png");

    public static boolean ensureRotorStatesDumpDir() {
        return FileUtil.ensureDir(DIR_ROTOR_STATE_DUMPS);
    }

    public static boolean ensureExternalProgramsDir() {
        return FileUtil.ensureDir(DIR_EXTERNAL_PROGRAMS);
    }

    public static boolean ensureExternalPathFunctions() {
        return FileUtil.ensureDir(DIR_EXTERNAL_PATH_FUNCTIONS);
    }

    public static void ensureDirs() {
        ensureRotorStatesDumpDir();
        ensureExternalProgramsDir();
        ensureExternalPathFunctions();
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
        if (!ensureRotorStatesDumpDir())
            return null;

        if (Format.isEmpty(funcName)) {
            funcName = DISPLAY_NAME_FUNCTION_UNKNOWN;
        }

        return FileUtil.getNonExistingFile(DIR_ROTOR_STATE_DUMPS.resolve(funcName + ROTOR_STATE_DUMP_FILE_EXTENSION));
    }


    /* .............................. External Functions ..................................... */

    /**
     * Delimits multiple shapes path data in a single file
     * */
    public static final String PATH_DATA_SHAPES_DELIMITER = "[|]";

    @NotNull
    public static String createExternalPathFunctionDisplayTitle(@NotNull String fullName) {
        return FileUtil.getName(fullName) + " (ext path)";
    }

    @NotNull
    public static String createExternalProgramFunctionDisplayTitle(@NotNull String fullName) {
        return FileUtil.getName(fullName) + " (ext program)";
    }

    @NotNull
    public static String createExternalRotorStatesFunctionDisplayTitle(@NotNull String fullName) {
        return FileUtil.getName(fullName) + " (ext rotors)";
    }


    public static boolean isValidPathData(@NotNull String pathData) {
        return !pathData.isBlank();
    }

    public static boolean isValidPathDataFile(@NotNull Path path) {
        return path.toString().endsWith(PATH_DATA_FILE_EXTENSION);
    }

    public static boolean isValidRotorStatesFile(@NotNull Path path) {
        return path.toString().endsWith(ROTOR_STATE_DUMP_FILE_EXTENSION);
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
            return loadExternalPathFunction(createExternalPathFunctionDisplayTitle(file.getFileName().toString()), pathData);
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
    public static String getGraphInCenterText() {
        return "Center";
    }

    @NotNull
    public static String getGraphInCenterShortDescription() {
        return "Graph in center [C]";
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
        return "Configure Frequency Provider";
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
