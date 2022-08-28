package main;

import main.models.function.provider.FunctionProviderI;
import main.models.function.provider.PathFunctionProvider;
import main.util.Log;
import main.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class R {

    public static final String TAG = "Resources";

    public static final String DISPLAY_NAME_FUNCTION_NOOP = "--select--";
    public static final String DISPLAY_NAME_FUNCTION_UNKNOWN = "Unknown Function";
    public static final String ROTOR_STATES_DUMP_FILE_EXT = ".rs";

    public static final Path DIR_MAIN = Path.of("").toAbsolutePath();
    public static final Path DIR_EXT_FUNCTIONS = DIR_MAIN.resolve("EXT_FUNCTIONS");
    public static final Path DIR_ROTOR_STATE_DUMPS = DIR_MAIN.resolve("SAVES");

    public static final Path DIR_RES = DIR_MAIN.resolve("res");
    public static final Path DIR_IMAGE = DIR_RES.resolve("image");
    public static final Path APP_ICON = DIR_IMAGE.resolve("icon.png");


    static @Nullable String getParent(@NotNull String path) {
        final int index = path.lastIndexOf(File.separatorChar);
        return (index < 1) ? null: path.substring(0, index);
    }

    static @NotNull String getFullName(@NotNull String path) {
        final int index = path.lastIndexOf(File.separatorChar);
        return (index == -1) ? path: path.substring(index + 1);
    }


    /**
     * Splits LoadMeta and FullName from path
     * Note : LoadMeta can be <code>null</code>.
     *
     * example  1. "/storage/emulated/0/log_file.txt" -> ( "/storage/emulated/0", "log_file.txt" )
     *          2. "/storage/emulated/0/log_dir" -> ( "/storage/emulated/0", "log_file" )
     * */
    public static @NotNull Pair<String, String> split(@NotNull String path) {
        final int index = path.lastIndexOf(File.separatorChar);
        return (index < 1) ? new Pair<>(null, (index == -1) ? path: path.substring(1)): new Pair<>(path.substring(0, index), path.substring(index + 1));
    }




    public static @NotNull String getExtFromName(@NotNull String fullName, boolean includeDot) {
        int index = fullName.lastIndexOf(".");
        return (index < 1) ? "": includeDot ? fullName.substring(index): fullName.substring(index + 1);
    }

    public static @NotNull String getName(@NotNull String fullName) {
        int index = fullName.lastIndexOf(".");
        return (index < 1) ? fullName: fullName.substring(0, index);
    }

    /**
     * Splits Full name to Name and Extension
     *
     * example  1. "log_file.txt" -> ( "log_file", ".txt" ) (if includeDot else "txt".
     *          2. "log_dir" -> ( "log_dir", "" )
     * */
    public static @NotNull Pair<String, String> splitNameExt(@NotNull String fullName, boolean includeDot) {
        final int index = fullName.lastIndexOf(".");
        return (index < 1) ? new Pair<>(fullName, ""): new Pair<>(fullName.substring(0, index), includeDot? fullName.substring(index): fullName.substring(index + 1));
    }




    public static @NotNull String getExtFromPath(@NotNull String path, boolean includeDot) {
        return getExtFromName(getFullName(path), includeDot);
    }

    /**
     * Splits Extension from path
     *
     * example  1. "/storage/emulated/0/log_file.txt" -> ( "/storage/emulated/0/log_file", ".txt" ) (if includeDot else "txt".
     *          2. "/storage/emulated/0/log_dir" -> ( "/storage/emulated/0/log_dir", "" )
     * */
    public static @NotNull Pair<String, String> splitExtFromPath(@NotNull String path, boolean includeDot) {
        final int index = path.lastIndexOf(File.separatorChar);

        final String parent, fullName;
        if (index == -1) {
            parent = "";
            fullName = path;
        } else if (index == 0) {
            parent = File.separator;                     // including separator
            fullName = path.substring(1);
        } else {
            parent = path.substring(0, index + 1);      // including separator
            fullName = path.substring(index + 1);
        }

        final Pair<String, String> nameExt = splitNameExt(fullName, includeDot);
        return new Pair<>(parent + nameExt.first, nameExt.second);
    }

    /**
     * Finds a non-existing file for a given file, by adding numbered suffix before extension
     *
     * <pre>
     *     for example
     *     let input file -> "/storage/emulated/0/Android/rc (201).txt"
     *
     *     if input does not exists, it will return (0, inputFile),
     *     else returns (202, new File("/storage/emulated/0/Android/rc (202).txt"))
     * </pre>
     *
     * @param path  path to get alternate non-existing file
     * @return pair containing (non-existing suffix no, suffixed file), or (0, inputFile) if inputFile does not exists
     * */
    @NotNull
    public static Pair<Integer, Path> getNonExistingSuffixAndFile(@NotNull Path path) {
        if (!Files.exists(path))
            return new Pair<>(0, path);

        final Pair<String, String> pathExt = splitExtFromPath(path.toString(), true);
        Path temp;
        int num = 1, index, last;

//            if (pathExt.first.charAt(last = pathExt.first.length() - 1) == ')' && (pathExt.first.charAt(index = last - 2) == '(' || pathExt.first.charAt(index = last - 3) == '(')) {

        if (pathExt.first.charAt(last = pathExt.first.length() - 1) == ')' && (index = pathExt.first.lastIndexOf('(')) != -1) {
            try {
                num = Integer.parseInt(pathExt.first.substring(index + 1, last));
                if (pathExt.first.charAt(index - 1) == ' ') index--;
                pathExt.first = pathExt.first.substring(0, index);
            } catch (NumberFormatException ignored) { }
        }

        while (Files.exists(temp = Path.of(pathExt.first + " (" + num + ")" + pathExt.second))) {
            num++;
        }

        return new Pair<>(num, temp);
    }



    /**
     * @see #getNonExistingSuffixAndFile(Path)
     *
     * {@inheritDoc}
     * */
    public static @NotNull Path getNonExistingFile(@NotNull Path file) {
        return getNonExistingSuffixAndFile(file).second;
    }

    public static boolean ensureRotorStatesDumpDir() {
        if (!Files.isDirectory(DIR_ROTOR_STATE_DUMPS)) {
            try {
                Files.createDirectories(DIR_ROTOR_STATE_DUMPS);
            } catch (Throwable t) {
                Log.e(TAG, "Failed to create Rotor States dump directory <" + DIR_ROTOR_STATE_DUMPS.toAbsolutePath() + ">");
                return false;
            }
        }

        return true;
    }



    @Nullable
    public static Path createRotorStatesDumpFile(@Nullable String funcName) {
        if (!ensureRotorStatesDumpDir())
            return null;

        if (isEmpty(funcName)) {
            funcName = DISPLAY_NAME_FUNCTION_UNKNOWN;
        }

        return getNonExistingFile(DIR_ROTOR_STATE_DUMPS.resolve(funcName + ROTOR_STATES_DUMP_FILE_EXT));
    }


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


    /**
     * Delimits multiple shapes path data in a single file
     * */
    public static final String PATH_DATA_SHAPES_DELIMITER = "[|]";


//    public static final boolean EXTERNAL_FUNCTION_HUE_CYCLE = true;

    public static boolean isEmpty(@Nullable CharSequence sequence) {
        return sequence == null || sequence.length() == 0;
    }

    public static boolean notEmpty(@Nullable CharSequence sequence) {
        return !isEmpty(sequence);
    }


//    public static final ComplexDomainFunctionI RC_CHARS = new CharMerger(Arrays.asList(new CharR(), new CharC()), 10, true);
//
//
//    /**
//     * @return Fallback function to load when {@link #PATH_FILE} could not be used
//     * */
//    @Nullable
//    public static ComplexDomainFunctionI getFallbackFunction() {
////        return new StepFunction(false);
//
//        final PathFunctionMerger pathFunc = Database.PathData.RC_AQUIRE.getPathFunction();
//        if (pathFunc != null && PATH_FILE_FUNCTION_HUE_CYCLE) {
//            pathFunc.hueCycle();
//        }
//
//        return pathFunc;
//    }

    @NotNull
    public static String displayTitleForExtFile(@NotNull Path file) {
        String name = file.getFileName().toString();
        final int dot = name.lastIndexOf('.');
        if (dot != -1) {
            name = name.substring(0, dot);
        }

        return name + " (ext)";
    }


    public static boolean isValidPathData(@NotNull String pathData) {
        return !pathData.isBlank();
    }

    @Nullable
    private static FunctionProviderI loadExtFunctionFromFile(@NotNull Path file) {
        try {
//                final List<String> pathData = Files.readAllLines(file, StandardCharsets.UTF_8);
//                if (CollectionUtil.notEmpty(pathData) && notEmpty(pathData.get(0))) {
//                    return new PathFunctionProvider(displayTitleForExtFile(file), pathData.toArray(new String[0]));
//                }

            final String pathData = Files.readString(file, StandardCharsets.UTF_8);
            if (notEmpty(pathData)) {
                final String[] paths = pathData.split(PATH_DATA_SHAPES_DELIMITER);     // splits shapes
                final String[] parsed = List.of(paths).stream().filter(R::isValidPathData).toArray(String[]::new);

                if (parsed.length > 0) {
                    return new PathFunctionProvider(displayTitleForExtFile(file), parsed);
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "Exception while loading external function from file <" + file + ">", t);
        }

        return null;
    }

    @Nullable
    public static FunctionProviderI loadExtFunction(@NotNull Path file) {
        if (Files.isRegularFile(file)) {
            return loadExtFunctionFromFile(file);
        }

        return null;
    }

    @NotNull
    @Unmodifiable
    public static List<FunctionProviderI> loadExternalFunctions() {
        if (Files.isDirectory(DIR_EXT_FUNCTIONS)) {
            final List<FunctionProviderI> funcs = new ArrayList<>();

            try {
                Files.walkFileTree(DIR_EXT_FUNCTIONS, new FileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        final FunctionProviderI func = loadExtFunctionFromFile(file);
                        if (func != null) {
                            funcs.add(func);
                        }

                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (Throwable t) {
                Log.e(TAG, "Exception while loading external functions from dir <" + DIR_EXT_FUNCTIONS + ">", t);
            }


//            try (Stream<Path> files = Files.list(DIR_EXT_FUNCTIONS)) {
//                files.forEach(file -> {
//                    final FunctionProviderI func = loadExtFunction(file);
//                    if (func != null) {
//                        funcs.add(func);
//                    }
//                });
//            } catch (Throwable t) {
//                Log.e(TAG, "Exception while loading external functions from dir <" + DIR_EXT_FUNCTIONS + ">", t);
//            }

            return funcs;
        }

        return Collections.emptyList();
    }





    /* ......................  Strings ............................. */

    private static Pattern sWhiteSpacePattern;

    @NotNull
    public static Pattern getWhiteSpacePattern() {
        if (sWhiteSpacePattern == null) {
            sWhiteSpacePattern = Pattern.compile("\\s");;
        }

        return sWhiteSpacePattern;
    }

    @NotNull
    public static String removeAllWhiteSpaces(@NotNull CharSequence s) {
        return getWhiteSpacePattern().matcher(s).replaceAll("");
    }

    @NotNull
    public static String getFunctionProviderLabelText() {
        return "Function";
    }

    @NotNull
    public static String getFunctionProviderTooltipText() {
        return "Select a function provider";
    }


    @NotNull
    public static String getWaveToggleText(boolean drawingWave) {
        return "Wave";
    }

    @NotNull
    public static String getWaveToggleTooltipText(boolean drawingWave) {
        return (drawingWave? "show as graph": "show as wave") + " [W]";
    }

    @NotNull
    public static String getInvertYText() {
        return "Invert-Y";
    }

    @NotNull
    public static String getInvertYTooltipText() {
        return "Invert Y-axis";
    }

    @NotNull
    public static String getInvertXText() {
        return "Invert-X";
    }

    @NotNull
    public static String getInvertXTooltipText() {
        return "Invert X-axis";
    }

    @NotNull
    public static String getGraphInCenterText() {
        return "Center";
    }

    @NotNull
    public static String getGraphInCenterTooltipText() {
        return "Graph in center [C]";
    }

    @NotNull
    public static String getPointsJoiningText() {
        return "Join Points";
    }

    @NotNull
    public static String getPointsJoiningTooltipText() {
        return "Join points with lines";
    }

    @NotNull
    public static String getHueCycleText() {
        return "Color Cycle";
    }

    @NotNull
    public static String getHueCycleTooltipText() {
        return "Set color cycle override";
    }

    @NotNull
    public static String getAutoTrackInCenterText() {
        return "Auto Track";
    }

    @NotNull
    public static String getAutoTrackInCenterTooltipText() {
        return "Tracks the graph while drawing";
    }


    @NotNull
    public static String getEndBehaviourLabelText() {
        return "On End";
    }

    @NotNull
    public static String getEndBehaviourTooltipText() {
        return "Configure end behaviour";
    }

    @NotNull
    public static String getToggleControlsText(boolean controlsShown) {
        return controlsShown? "Hide Dock": "Show Dock";
    }

    @NotNull
    public static String getToggleControlsTooltipText(boolean controlsShown) {
        return (controlsShown? "Hide controls dock": "Show controls dock") + " [Ctrl-C]";
    }

    @NotNull
    public static String getPlayPauseText(boolean playing) {
        return playing? "Pause": "Play";
    }

    @NotNull
    public static String getPlayPauseTooltipText(boolean playing) {
        return (playing? "pause rotors": "start rotors") + " [SPACE]";
    }

    @NotNull
    public static String getResetText() {
        return "Reset";
    }

    @NotNull
    public static String getResetTooltipText() {
        return "Reset [Ctrl-R]";
    }

    @NotNull
    public static String getResetScaleAndDragText() {
        return "Reset View";
    }

    @NotNull
    public static String getResetScaleAndDragTooltipText() {
        return "Reset viewport [Shift-R]";
    }

    @NotNull
    public static String getRotorCountText(int count) {
        return "Rotors: " + count;
    }

    @NotNull
    public static String getRotorCountSliderTooltipText() {
        return "change no of rotors";
    }

    @NotNull
    public static String getSpeedPercentText(int percent) {
        return "Speed: " + percent + "%";
    }

    @NotNull
    public static String getSpeedSliderTooltipText() {
        return "change speed";
    }

    @NotNull
    public static String getScaleText(double scale) {
        return "Scale: " + ((int) Math.round(scale * 100)) + "%";
    }

    @NotNull
    public static String getScaleIncTooltipText() {
        return "Zoom In [Shift-UP]";
    }

    @NotNull
    public static String getScaleDecTooltipText() {
        return "Zoom Out [Shift-DOWN]";
    }


    @Nullable
    public static String getStatusText(boolean loading, int pendingRotorCount) {
        if (!loading)
            return null;

        return "Loading" + (pendingRotorCount > 0? " " + pendingRotorCount + " Rotors": "...");
    }


}
