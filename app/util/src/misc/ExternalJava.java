package misc;

import models.OpenFileFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.filechooser.FileFilter;
import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ExternalJava {

    public static final String TAG = "ExternalJava";
    public static final String MY_CLASSPATH = System.getProperty("java.class.path");
    public static final String CLASSPATH_SEPARATOR = System.getProperty("path.separator");

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public static final String FILE_JAVA_SOURCE_EXTENSION = ".java";
    public static final String FILE_JAVA_SOURCE_DESCRIPTION = "Java Source";
    public static final FileFilter FILE_FILTER_JAVA_SOURCE = new OpenFileFilter(FILE_JAVA_SOURCE_EXTENSION, FILE_JAVA_SOURCE_DESCRIPTION);

    public static final String FILE_JAVA_CLASS_EXTENSION = ".class";
    public static final String FILE_JAVA_CLASS_DESCRIPTION = "Java Bytecode";
    public static final FileFilter FILE_FILTER_JAVA_CLASS = new OpenFileFilter(FILE_JAVA_CLASS_EXTENSION, FILE_JAVA_CLASS_DESCRIPTION);

    public static final String FILE_JAVA_ARCHIVE_EXTENSION = ".jar";
    public static final String FILE_JAVA_ARCHIVE_DESCRIPTION = "Java Archive";
    public static final FileFilter FILE_FILTER_JAVA_ARCHIVE = new OpenFileFilter(FILE_JAVA_ARCHIVE_EXTENSION, FILE_JAVA_ARCHIVE_DESCRIPTION);
    public static final Predicate<FileUtil.PathInfo> PATH_FILTER_JAVA_ARCHIVE = (FileUtil.PathInfo pi) -> pi.path().toString().toLowerCase().endsWith(FILE_JAVA_ARCHIVE_EXTENSION);


    @NotNull
    public static String toClassName(@NotNull String relSrcPath, boolean removeExt) {
        if (removeExt) {
            relSrcPath = FileUtil.splitExtensionFromPath(relSrcPath, true).first;
        }

        return relSrcPath.replace(File.separatorChar, '.');
    }

    @NotNull
    public static String toClassName(@NotNull String relSrcPath) {
        return toClassName(relSrcPath, true);
    }

    @NotNull
    public static String toPath(@NotNull String className, @Nullable String extension) {
        return className.replace('.', File.separatorChar) + (extension != null? extension: "");
    }

    @NotNull
    public static String toPath(@NotNull String className, @NotNull JavaFileObject.Kind kind) {
        return toPath(className, kind.extension);
    }



    public static class Location {

        @NotNull
        public static Location fromClassName(@NotNull Path classpath, @NotNull String className, @NotNull JavaFileObject.Kind kInd) {
            final Location loc = new Location(classpath, toPath(className, kInd));
            loc.className = className;
            return loc;
        }

        @NotNull
        public Location withClassName(@NotNull String className, @NotNull JavaFileObject.Kind kind) {
            return fromClassName(this.classpath, className, kind);
        }

        @NotNull
        public final Path classpath;
        @NotNull
        public final String relativeSourcePath;

        @Nullable
        private transient Path sourcePath;
        @Nullable
        private transient String className;

        public Location(@NotNull Path classpath, @NotNull String relativeSourcePath, @Nullable Path sourcePath) {
            this.classpath = classpath;
            this.relativeSourcePath = relativeSourcePath;
            this.sourcePath = sourcePath;
        }

        public Location(@NotNull Path classpath, @NotNull String relativeSourcePath) {
            this(classpath, relativeSourcePath, null);
        }

        @NotNull
        public Path getSourcePath() {
            if (sourcePath == null) {
                sourcePath = classpath.resolve(relativeSourcePath);
            }

            return sourcePath;
        }

        @NotNull
        public String getClassName() {
            if (className == null) {
                className = toClassName(relativeSourcePath);
            }

            return className;
        }

    }


    public static class JavaObject extends SimpleJavaFileObject {

        @NotNull
        private final Location location;

        @Nullable
        private Charset mCharSet;

        public JavaObject(@NotNull Location location, @NotNull Kind kind) {
            super(location.getSourcePath().toUri(), kind);
            this.location = location;
        }

        @NotNull
        public URI getUri() {
            return uri;
        }

        @NotNull
        public Location getLocation() {
            return location;
        }

        public JavaObject setCharSet(@Nullable Charset charSet) {
            mCharSet = charSet;
            return this;
        }

        @Nullable
        public Charset getCharSet() {
            return mCharSet;
        }

        @NotNull
        public Charset getCharSetOrDefault() {
            return mCharSet != null? mCharSet: DEFAULT_CHARSET;
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return new BufferedInputStream(Files.newInputStream(location.getSourcePath()));
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return Files.readString(location.getSourcePath(), getCharSetOrDefault());
        }

        @Override
        public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
            return new InputStreamReader(openInputStream());
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return new BufferedOutputStream(Files.newOutputStream(location.getSourcePath()));
        }

        @Override
        public Writer openWriter() throws IOException {
            return new OutputStreamWriter(openOutputStream());
        }
    }

    public static class CompilationException extends RuntimeException {
        public CompilationException() {
            super();
        }

        public CompilationException(String message) {
            super(message);
        }

        public CompilationException(String message, Throwable cause) {
            super(message, cause);
        }

        public CompilationException(Throwable cause) {
            super(cause);
        }

        protected CompilationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }


    @NotNull
    public static Class<?> compileAndLoadClass(@NotNull JavaObject source, @Nullable Collection<String> jars) throws CompilationException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new CompilationException("No JAVA compiler found on the system");
        }

        final ForwardingJavaFileManager<StandardJavaFileManager> fm = new ForwardingJavaFileManager<>(compiler.getStandardFileManager(null, null, source.getCharSetOrDefault())) {
            @Override
            public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
                return new JavaObject(source.location.withClassName(className, kind), kind);
            }
        };

        final StringJoiner cpJoiner = new StringJoiner(CLASSPATH_SEPARATOR);
        cpJoiner.add(MY_CLASSPATH);

        final String srcCp = source.location.classpath.toString();
        if (!MY_CLASSPATH.equals(srcCp)) {
            cpJoiner.add(srcCp);
        }

        if (!(jars == null || jars.isEmpty())) {
            jars.forEach(cpJoiner::add);
        }

        final List<String> options = List.of("-classpath", cpJoiner.toString());
        final StringWriter errorwriter = new StringWriter();

        try {
            final JavaCompiler.CompilationTask task = compiler.getTask(errorwriter, fm, null, options, null, List.of(source));
            if (!task.call()) {
                throw new CompilationException(errorwriter.toString());
            }

            try (final URLClassLoader cl = new URLClassLoader(new URL[] { source.location.classpath.toUri().toURL() }, ExternalJava.class.getClassLoader())) {
                return cl.loadClass(source.location.getClassName());
            }
        } catch (Throwable t) {
            throw new CompilationException(t);
        }
    }

    @NotNull
    public static Class<?> compileAndLoadClass(@NotNull JavaObject source, boolean loadExternalJars) throws CompilationException {
        List<String> jars = null;
        if (loadExternalJars) {
            try {
                jars = FileUtil.scanRegularFiles(source.location.classpath, PATH_FILTER_JAVA_ARCHIVE, null)
                        .stream()
                        .map(pi -> pi.path().toString())
                        .collect(Collectors.toList());
            } catch (Exception exc) {
                Log.e(TAG, "Failed to scan external jar files in <" + source.location.classpath + ">", exc);
            }
        }

        return compileAndLoadClass(source, jars);
    }


//    public static void main(String[] args) {
//
//        final Path cp = Path.of("E:\\test project\\test - Copy");
//        final String relSrcPath = "pkg\\ExtTest.java";
//
//
//        final Class<?> clazz  = compileAndLoadClass(new JavaObject(new Location(cp, relSrcPath), JavaFileObject.Kind.SOURCE), true);
//
//        try {
//            final Complex result = ((ComplexFunctionI) clazz.getDeclaredConstructor().newInstance()).compute(2);
//            Log.d(TAG, "Result: " + result);
//        } catch (Throwable t) {
//            Log.e(TAG, "Test failed", t);
//        }
//    }
}
