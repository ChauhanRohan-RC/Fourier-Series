package app;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatArcOrangeIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatDarkPurpleIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDarkerIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialLighterIJTheme;
import function.definition.ComplexDomainFunctionI;
import misc.ExternalJava;
import misc.FileUtil;
import misc.Format;
import misc.Log;
import models.OpenFileFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FontUIResource;
import javax.tools.JavaFileObject;
import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;

public class R {

    public static final String TAG = "Resources";

    public static final Random RANDOM = new Random();

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

        // preload
        getFontAquire();

//        final Font defaultFont = getFontPdSansRegular();
//        if (defaultFont != null) {
//            setDefaultFont(new FontUIResource(defaultFont.deriveFont(14f)));
//        }
    }

    public static void finish() {

    }

    public static final String LINE_COMMENT_TOKEN = "#";
    
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

    public static final String SVG_FILE_EXTENSION = ".svg";
    public static final String SVG_FILE_DESCRIPTION = "Scalable Vector Graphics";
    public static final FileFilter SVG_FILE_FILTER = new OpenFileFilter(SVG_FILE_EXTENSION, SVG_FILE_DESCRIPTION);

    public static final String PATH_DATA_FILE_EXTENSION = ".pd";
    public static final String PATH_DATA_FILE_DESCRIPTION = "Path Data";
    public static final FileFilter PATH_DATA_FILE_FILTER = new OpenFileFilter(PATH_DATA_FILE_EXTENSION, PATH_DATA_FILE_DESCRIPTION);

//    public static final boolean DEFAULT_VALIDATE_EXTERNAL_FILES = true;
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

    // .............. Images
    public static final Path DIR_IMAGE = DIR_RES.resolve("image");
    public static final Path DIR_IMAGE_ICONS = DIR_IMAGE.resolve("icons");
    public static final Path DIR_IMAGE_APP_ICONS = DIR_IMAGE.resolve("app_icons");
    public static final Path DIR_IMAGE_GRAPHICS = DIR_IMAGE.resolve("graphics");

    // App icons
    public static final Path IMG_APP_ICON = DIR_IMAGE_APP_ICONS.resolve("app_icon.png");
    public static final Path IMG_APP_ICON_COLORFUL = DIR_IMAGE_APP_ICONS.resolve("app_icon_colorful.png");
    public static final Path IMG_SIN_WAVE = DIR_IMAGE_APP_ICONS.resolve("sin_wave.png");
    public static final Path IMG_CIRCLE_WAVE = DIR_IMAGE_APP_ICONS.resolve("circle_wave.png");
    public static final Path IMG_REC = DIR_IMAGE_APP_ICONS.resolve("rec.png");

    // Graphics
    public static final Path IMG_RC_STUDIO500 = DIR_IMAGE_GRAPHICS.resolve("rc_studio500.png");

    // Icons
    public static final Path IMG_CHECK_LIGHT_64 = DIR_IMAGE_ICONS.resolve("check_light_64.png");
    public static final Path IMG_CHECK_DARK_64 = DIR_IMAGE_ICONS.resolve("check_dark_64.png");
    public static final Path IMG_CHECK_ACCENT_64 = DIR_IMAGE_ICONS.resolve("check_accent_64.png");

    public static final Path IMG_CHECK_CIRCLE_LIGHT_64 = DIR_IMAGE_ICONS.resolve("check_circle_light_64.png");
    public static final Path IMG_CHECK_CIRCLE_DARK_64 = DIR_IMAGE_ICONS.resolve("check_circle_dark_64.png");
    public static final Path IMG_CHECK_CIRCLE_ACCENT_64 = DIR_IMAGE_ICONS.resolve("check_circle_accent_64.png");

    public static final Path IMG_ERASE_LIGHT_64 = DIR_IMAGE_ICONS.resolve("erase_light_64.png");
    public static final Path IMG_ERASE_DARK_64 = DIR_IMAGE_ICONS.resolve("erase_dark_64.png");
    public static final Path IMG_ERASE_ACCENT_64 = DIR_IMAGE_ICONS.resolve("erase_accent_64.png");

    public static final Path IMG_DELETE_LIGHT_64 = DIR_IMAGE_ICONS.resolve("delete_light_64.png");
    public static final Path IMG_DELETE_DARK_64 = DIR_IMAGE_ICONS.resolve("delete_dark_64.png");
    public static final Path IMG_DELETE_ACCENT_64 = DIR_IMAGE_ICONS.resolve("delete_accent_64.png");

    public static final Path IMG_POINTS_LIGHT_64 = DIR_IMAGE_ICONS.resolve("points_light_64.png");
    public static final Path IMG_POINTS_DARK_64 = DIR_IMAGE_ICONS.resolve("points_dark_64.png");
    public static final Path IMG_POINTS_ACCENT_64 = DIR_IMAGE_ICONS.resolve("points_accent_64.png");

    public static final Path IMG_EXPAND_LIGHT_64 = DIR_IMAGE_ICONS.resolve("expand_light_64.png");
    public static final Path IMG_EXPAND_DARK_64 = DIR_IMAGE_ICONS.resolve("expand_dark_64.png");
    public static final Path IMG_EXPAND_ACCENT_64 = DIR_IMAGE_ICONS.resolve("expand_accent_64.png");

    public static final Path IMG_COLLAPSE_LIGHT_64 = DIR_IMAGE_ICONS.resolve("collapse_light_64.png");
    public static final Path IMG_COLLAPSE_DARK_64 = DIR_IMAGE_ICONS.resolve("collapse_dark_64.png");
    public static final Path IMG_COLLAPSE_ACCENT_64 = DIR_IMAGE_ICONS.resolve("collapse_accent_64.png");

    public static final Path IMG_MAXIMISE_LIGHT_64 = DIR_IMAGE_ICONS.resolve("maximise_light_64.png");
    public static final Path IMG_MAXIMISE_DARK_64 = DIR_IMAGE_ICONS.resolve("maximise_dark_64.png");
    public static final Path IMG_MAXIMISE_ACCENT_64 = DIR_IMAGE_ICONS.resolve("maximise_accent_64.png");

    public static final Path IMG_MINIMISE_LIGHT_64 = DIR_IMAGE_ICONS.resolve("minimise_light_64.png");
    public static final Path IMG_MINIMISE_DARK_64 = DIR_IMAGE_ICONS.resolve("minimise_dark_64.png");
    public static final Path IMG_MINIMISE_ACCENT_64 = DIR_IMAGE_ICONS.resolve("minimise_accent_64.png");

    public static final Path IMG_CENTER_FOCUS_LIGHT_64 = DIR_IMAGE_ICONS.resolve("center_focus_light_64.png");
    public static final Path IMG_CENTER_FOCUS_DARK_64 = DIR_IMAGE_ICONS.resolve("center_focus_dark_64.png");
    public static final Path IMG_CENTER_FOCUS_ACCENT_64 = DIR_IMAGE_ICONS.resolve("center_focus_accent_64.png");

    public static final Path IMG_UNDO_LIGHT_64 = DIR_IMAGE_ICONS.resolve("undo_light_64.png");
    public static final Path IMG_UNDO_DARK_64 = DIR_IMAGE_ICONS.resolve("undo_dark_64.png");
    public static final Path IMG_UNDO_ACCENT_64 = DIR_IMAGE_ICONS.resolve("undo_accent_64.png");

    public static final Path IMG_REDO_LIGHT_64 = DIR_IMAGE_ICONS.resolve("redo_light_64.png");
    public static final Path IMG_REDO_DARK_64 = DIR_IMAGE_ICONS.resolve("redo_dark_64.png");
    public static final Path IMG_REDO_ACCENT_64 = DIR_IMAGE_ICONS.resolve("redo_accent_64.png");

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


    // Sound TODO: change sounds
    public static final Path DIR_SOUND = DIR_RES.resolve("sound");
    public static final Path DIR_MUSIC = DIR_SOUND.resolve("music");

    public static final Path SOUND_FILE_WINDOW_OPEN = DIR_SOUND.resolve("window_open.wav");
    public static final Path SOUND_FILE_WINDOW_CLOSE = DIR_SOUND.resolve("window_close.wav");
    public static final Path SOUND_FILE_CLICK = DIR_SOUND.resolve("click.wav");
    public static final Path SOUND_FILE_HOVER = DIR_SOUND.resolve("hover.wav");
    public static final Path SOUND_FILE_BEEP = DIR_SOUND.resolve("beep.wav");

    @Nullable
    private static List<Path> sMusicFiles;

    public static void invalidateMusicFiles() {
        synchronized (R.class) {
            sMusicFiles = null;
        }
    }

    public static List<Path> getMusicFiles() {
        List<Path> mfs = sMusicFiles;
        if (mfs == null) {
            synchronized (R.class) {
                mfs = sMusicFiles;
                if (mfs == null) {
                    mfs = FileUtil.listRegularFiles(DIR_MUSIC);
                    sMusicFiles = mfs;
                }
            }
        }

        return mfs;
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

    public static final String APP_NAME = "RFS";
    public static final String APP_SHORT_DESCRIPTION = "RETRO FOURIER SIMULATOR";
    public static final String APP_VERSION = "2023.10.25";
    public static final String BUILD_VERSION = "#IC-223.8214.82";
    public static final String BUILD_DATE = "February 9, 2023";

    public static final String AUTHOR = "Rohan Singh Chauhan";
    public static final String AUTHOR_EMAIL = "com.production.rc@gmail.com";
    @Nullable
    public static final String AUTHOR_DEVELOPER_PAGE_URI = "https://github.com/ChauhanRohan-RC";
    @Nullable
    public static final String PROJECT_SOURCE_URI = "https://github.com/ChauhanRohan-RC/Fourier-Series.git";

    @NotNull
    public static String createAppDescription() {
        String main = String.format("%s %s\n%s\nbuild %s, built on %s\n\nAuthor: %s\nEmail: %s", APP_NAME, APP_VERSION, APP_SHORT_DESCRIPTION, BUILD_VERSION, BUILD_DATE, AUTHOR, AUTHOR_EMAIL);
        if (Format.notEmpty(AUTHOR_DEVELOPER_PAGE_URI)) {
            main += String.format("\nDeveloper Page: %s", AUTHOR_DEVELOPER_PAGE_URI);
        }

        if (Format.notEmpty(PROJECT_SOURCE_URI)) {
            main += String.format("\n\nProject Source: %s", PROJECT_SOURCE_URI);
        }

        return main;
    }

    public static final String TITLE_MAIN = APP_NAME;
    public static final String TITLE_FT = APP_NAME + " Transform";
    public static final String TITLE_MOUSE_PATH_UI = APP_NAME + " Canvas";
    public static final String TITLE_CONFIGURATIONS = "Configuration";

    public static final int SIZE_ICON_SMALL = 15;
    public static final int SIZE_ICON_LARGE = 24;

    @Nullable
    public static Image createImage(@NotNull Path path) {
        if (Files.exists(path)) {
            try {
                return Toolkit.getDefaultToolkit().createImage(path.toString());
            } catch (Throwable t) {
                Log.e(TAG, "failed to create image from file <" + path + ">", t);
            }
        }

        return null;
    }

    @NotNull
    public static Image resizeImage(@NotNull Image image, int width, int height, boolean highQuality) {
        return image.getScaledInstance(width, height, highQuality? Image.SCALE_SMOOTH: Image.SCALE_FAST);
    }

    @Nullable
    public static Image createImage(@NotNull Path path, int width, int height, boolean highQuality) {
        final Image img = createImage(path);
        if (img == null)
            return null;

        return resizeImage(img, width, height, highQuality);
    }

    @Nullable
    public static ImageIcon createIcon(@NotNull Path path) {
        if (Files.exists(path)) {
            try {
                return new ImageIcon(path.toString());
            } catch (Throwable t) {
                Log.e(TAG, "failed to create imageIcon from file <" + path + ">", t);
            }
        }

        return null;
    }

    @Nullable
    public static ImageIcon createIcon(@NotNull Path path, int width, int height, boolean highQuality) {
        Image image = createImage(path);
        if (image != null) {
            return new ImageIcon(resizeImage(image, width, height, highQuality));
        }

        return null;
    }

    @Nullable
    public static ImageIcon createIcon(@NotNull Path path, int width, int height) {
        return createIcon(path, width, height, false);
    }

//    @NotNull
//    public static ImageIcon resizeIcon(@NotNull ImageIcon icon, int width, int height) {
//        return new ImageIcon(resizeImage(icon.getImage(), width, height));
//    }


    @Nullable
    public static ImageIcon createIcon(@NotNull Path path, int size, boolean highQuality) {
        return createIcon(path, size, size, highQuality);
    }

    @Nullable
    public static ImageIcon createIcon(@NotNull Path path, int size) {
        return createIcon(path, size, false);
    }

    @Nullable
    public static ImageIcon createSmallIcon(@NotNull Path path, boolean highQuality) {
        return createIcon(path, SIZE_ICON_SMALL, highQuality);
    }

    @Nullable
    public static ImageIcon createSmallIcon(@NotNull Path path) {
        return createSmallIcon(path, false);
    }

    @Nullable
    public static ImageIcon createLargeIcon(@NotNull Path path, boolean highQuality) {
        return createIcon(path, SIZE_ICON_LARGE, highQuality);
    }

    @Nullable
    public static ImageIcon createLargeIcon(@NotNull Path path) {
        return createLargeIcon(path, false);
    }


    @Nullable
    public static Image createAppIcon() {
        return createImage(IMG_APP_ICON);
    }

    @Nullable
    public static Image createAppIconColorful() {
        return createImage(IMG_APP_ICON_COLORFUL);
    }


    /* .............................. External Functions ..................................... */

    public static final String DISPLAY_NAME_TOKEN_EXT_PATH = "ext_path";
    public static final String DISPLAY_NAME_TOKEN_EXT_PROGRAM = "ext_program";
    public static final String DISPLAY_NAME_TOKEN_EXT_ROTOR_STATE = "ext_rotors";
    public static final String DISPLAY_NAME_TOKEN_LOADED = "loaded";

    @NotNull
    public static String displayNameWithToken(@NotNull String fullName, @NotNull String token) {
        String name = FileUtil.getName(fullName);
        if (name.isEmpty()) {
            name = DISPLAY_NAME_FUNCTION_UNKNOWN;
        } else if (name.contains(token)) {
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
    public static String createExternalRotorStateFunctionDisplayName(@NotNull String fullName) {
        return displayNameWithToken(fullName, DISPLAY_NAME_TOKEN_EXT_ROTOR_STATE);
    }


    @NotNull
    public static String createExternallyLoadedFunctionDisplayName(@NotNull String fullName) {
        return displayNameWithToken(fullName, DISPLAY_NAME_TOKEN_LOADED);
    }


    //    public static boolean isValidRotorStatesFile(@NotNull Path path) {
//        return path.toString().endsWith(FUNCTION_STATE_SAVE_FILE_EXTENSION);
//    }


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
        return (controlsShown? "Hide": "Show") + " Controls Dock [Shift-C]";
    }

    @NotNull
    public static String getToggleMenuBarText(boolean menuVisible) {
        return (menuVisible? "Hide": "Show") + " Menu";
    }

    @NotNull
    public static String getToggleMenuBarShortDescription(boolean menuVisible) {
        return (menuVisible? "Hide": "Show") + " Menu [Shift-M]";
    }

    @NotNull
    public static String getTogglePresentationModeText(boolean presenting) {
        return (presenting? "Exit": "Enter") + " Presentation Mode";
    }


    public static String getTogglePresentationModeShortDescription(boolean presenting) {
        return (presenting? "Leave": "Enter") + " Presentation [Shift-P]";
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
        return "Reset Zoom Scale";
    }

    @NotNull
    public static String getResetDragText() {
        return "Recenter";
    }

    @NotNull
    public static String getResetDragShortDescription() {
        return "Recenter Origin";
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
    public static String getUndoText() {
        return "Undo";
    }

    @NotNull
    public static String getUndoShortDescription() {
        return "Undo last action [ctrl-Z]";
    }

    @NotNull
    public static String getRedoText() {
        return "Redo";
    }

    @NotNull
    public static String getRedoShortDescription() {
        return "Redo last action [ctrl-X]";
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
