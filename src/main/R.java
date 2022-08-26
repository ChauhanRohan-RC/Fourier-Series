package main;

import main.models.function.provider.FunctionProviderI;
import main.models.function.provider.PathFunctionProvider;
import main.util.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.awt.*;
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
import java.util.stream.Stream;

public class R {

    public static final String TAG = "Resources";

    public static final Path DIR_MAIN = Path.of("").toAbsolutePath();
    public static final Path DIR_EXT_FUNCTIONS = DIR_MAIN.resolve("EXT_FUNCTIONS");

    public static final Path DIR_RES = DIR_MAIN.resolve("res");
    public static final Path DIR_IMAGE = DIR_RES.resolve("image");
    public static final Path APP_ICON = DIR_IMAGE.resolve("icon.png");

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
    public static String getStatusText(boolean loading) {
        return loading? "Loading...": null;
    }


}
