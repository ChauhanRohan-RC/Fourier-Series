package util;

import app.R;
import async.*;
import misc.CollectionUtil;
import misc.FileUtil;
import misc.Log;
import models.DirStat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.xml.sax.SAXException;
import provider.FunctionMeta;
import provider.FunctionProviderI;
import provider.FunctionType;
import provider.PathFunctionProvider;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PathFunctionManager {

    public static final String TAG = "PathFunctionManager";

    /**
     * Delimits multiple shapes path data in a single file
     * */
    public static final String PATH_DATA_SHAPES_DELIMITER = "|";
    public static final String PATH_DATA_SHAPES_DELIMITER_REGEX = "[|]";

    private static final SvgPathParser sSvgPathParser = new SvgPathParser(
            R.LINE_COMMENT_TOKEN,
            PATH_DATA_SHAPES_DELIMITER,
            PATH_DATA_SHAPES_DELIMITER_REGEX,
            R.ENCODING
    );

    public static boolean isPathDataFile(@NotNull Path path) {
        return path.toString().endsWith(R.PATH_DATA_FILE_EXTENSION);
    }

    public static boolean isSvgFile(@NotNull Path path) {
        return path.toString().endsWith(R.SVG_FILE_EXTENSION);
    }

    @NotNull
    public static Path convertSvgToPathDataFile(@NotNull Path svgFile, @NotNull Path outFile, boolean pretty) throws ParserConfigurationException, IOException, SAXException {
        outFile = FileUtil.getNonExistingFile(FileUtil.ensureExtension(outFile, R.PATH_DATA_FILE_EXTENSION));
        sSvgPathParser.writeSvgPaths(svgFile, pretty, outFile);
        return outFile;
    }

    @NotNull
    public static Canceller convertSvgToPathDataFileAsync(@NotNull Path svgFile, @NotNull Path outFile, boolean pretty, @Nullable TaskConsumer<Path> callback) {
        return Async.execute(() -> convertSvgToPathDataFile(svgFile, outFile, pretty), callback);
    }


    @NotNull
    private static FunctionProviderI createPathFunctionProvider(@NotNull String functionName, @NotNull Collection<String> paths) {
        return new PathFunctionProvider(new FunctionMeta(FunctionType.EXTERNAL_PATH, functionName), paths.toArray(new String[0]));
    }

    @Nullable
    public static FunctionProviderI loadExternalPathFunctionFromPathData(@NotNull String functionName, @NotNull String pathData) {
        final List<String> paths = sSvgPathParser.extractPathsFromPathDataFile(pathData);
        if (CollectionUtil.isEmpty(paths))
            return null;

        return createPathFunctionProvider(functionName, paths);
    }

    @Nullable
    public static FunctionProviderI loadExternalPathFunctionFromPathDataFile(@NotNull Path pathDataFile) throws IOException {
        final List<String> paths = sSvgPathParser.extractPathsFromPathDataFile(pathDataFile);
        if (CollectionUtil.isEmpty(paths))
            return null;

        return createPathFunctionProvider(extPathFunctionDisplayName(pathDataFile), paths);
    }

    @Nullable
    public static FunctionProviderI loadExternalPathFunctionFromSvgFile(@NotNull Path svgFile) throws ParserConfigurationException, SAXException, IOException {
        final List<String> paths = sSvgPathParser.extractPathsFromSvg(svgFile);
        if (CollectionUtil.isEmpty(paths))
            return null;

        return createPathFunctionProvider(extPathFunctionDisplayName(svgFile), paths);
    }

    @NotNull
    private static String extPathFunctionDisplayName(@NotNull Path file) {
        return R.createExternalPathFunctionDisplayName(file.getFileName().toString());
    }

    @Nullable
    public static FunctionProviderI loadExternalPathFunctionNoThrow(@NotNull Path file) {
        try {
            if (isPathDataFile(file)) {
                return loadExternalPathFunctionFromPathDataFile(file);
            }

            if (isSvgFile(file)) {
                return loadExternalPathFunctionFromSvgFile(file);
            }

            throw new IllegalArgumentException("Invalid Path Data File Type, supported types: " + R.SVG_FILE_EXTENSION + ", " + R.PATH_DATA_FILE_EXTENSION);
        } catch (Throwable t) {
            Log.e(R.TAG, "Exception while loading external function from file <" + file + ">", t);
        }

        return null;
    }

    @Nullable
    public static FunctionProviderI[] loadExternalPathFunctions(Path[] files, @Nullable CancellationProvider c) {
        if (files == null || files.length == 0)
            return null;

        final FunctionProviderI[] providers = new FunctionProviderI[files.length];
        for (int i=0; i < files.length; i++) {
            if (c != null && c.isCancelled())
                break;
            providers[i] = loadExternalPathFunctionNoThrow(files[i]);
        }

        return providers;
    }

    @NotNull
    public static Canceller loadExternalPathFunctionsAsync(Path[] files, @NotNull Consumer<FunctionProviderI[]> callback) {
        final Async.CExecutor exe = new Async.CExecutor();
        exe.execute(c -> loadExternalPathFunctions(files, c), callback);
        return exe;
    }

    @Nullable
    @Unmodifiable
    public static LoadResult loadExternalPathFunctions(@NotNull Path dir, @Nullable CancellationProvider c) {
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
                final FunctionProviderI func = loadExternalPathFunctionNoThrow(file);
                if (func != null) {
                    result.addFunctionProvider(func);
                    success = true;
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
            Log.e(R.TAG, "Exception while loading external functions from dir <" + dir + ">", t);
        }

        return result;
    }

    @NotNull
    public static Canceller loadExternalPathFunctionsAsync(@NotNull Path dir, @NotNull Consumer<LoadResult> callback) {
        final Async.CExecutor exe = new Async.CExecutor();
        exe.execute((CancellationProvider c) -> loadExternalPathFunctions(dir, c), callback);
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
}
