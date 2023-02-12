package app;

import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class Colors {

    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    public static final Color BG_DARK = new Color(0, 0, 0);
    public static final Color BG_MEDIUM = new Color(42, 42, 42);
    public static final Color BG_LIGHT = new Color(68, 68, 68);

    public static final Color FG_DARK = new Color(255, 255, 255);
    public static final Color FG_MEDIUM = new Color(218, 218, 218);
    public static final Color FG_LIGHT = new Color(168, 168, 168);

    public static final Color ACCENT_FG_DARK = new Color(255, 160, 219);
    public static final Color ACCENT_FG_MEDIUM = new Color(234, 149, 196);
    public static final Color ACCENT_FG_LIGHT = new Color(204, 122, 173);
    public static final Color ACCENT_BG_DARK = new Color(100, 0, 57);
    public static final Color ACCENT_BG_MEDIUM = new Color(131, 0, 76);
    public static final Color ACCENT_BG_LIGHT = new Color(169, 0, 98);

    public static final Color[] THEME_COLORS = {
            new Color(255, 221, 58, 255),
            new Color(75, 255, 255),
            new Color(98, 255, 98),
            new Color(255, 58, 247),
            new Color(92, 130, 255),
            new Color(175, 84, 255),
            new Color(255, 164, 72),
            new Color(255, 83, 83),
    };

    /* Ui colors main */

    public static final Color COLOR_CIRCLE = new Color(222, 222, 222, 204);
    public static final Color COLOR_TIP = new Color(255, 255, 255, 255);
    public static final Color COLOR_RADIUS = new Color(255, 255, 255, 255);
    public static final Color COLOR_TIP_TO_WAVE_JOINT = new Color(115, 250, 255, 217);
    public static final Color COLOR_WAVE = new Color(255, 221, 58, 255);
    public static final Color COLOR_AXIS = new Color(94, 94, 94);
    public static final Color COLOR_CENTER_OF_MASS =   new Color(255, 73, 73);

    /* Ui colors when graphing in center */
    public static final Color CENTER_COLOR_CIRCLE = new Color(190, 190, 190, 122);
    public static final Color CENTER_COLOR_TIP = new Color(246, 246, 246, 230);
    public static final Color CENTER_COLOR_RADIUS = new Color(250, 250, 250, 230);
    public static final Color CENTER_COLOR_TIP_TO_WAVE_JOINT = new Color(115, 250, 255, 204);
//    public static final Color CENTER_COLOR_WAVE = new Color(255, 218, 77, 255);


    public static Color getCircleColor(boolean graphingInCenter) {
        return graphingInCenter? CENTER_COLOR_CIRCLE: COLOR_CIRCLE;
    }

    public static Color getTipColor(boolean graphingInCenter) {
        return graphingInCenter? CENTER_COLOR_TIP: COLOR_TIP;
    }

    public static Color getRadiusColor(boolean graphingInCenter) {
        return graphingInCenter? CENTER_COLOR_RADIUS: COLOR_RADIUS;
    }

    public static Color getTipToWaveJointColor(boolean graphingInCenter) {
        return graphingInCenter? CENTER_COLOR_TIP_TO_WAVE_JOINT: COLOR_TIP_TO_WAVE_JOINT;
    }


    public static Color getStaticWaveColor() {
        return COLOR_WAVE;
    }

    @NotNull
    public static Color getDynamicWaveColor(int id) {
        if (!Settings.getSingleton().getDynamicColorsEnabledOrDefault()) {
            return getStaticWaveColor();
        }

        if (id < 0) {
            id = -id;
        }

        return THEME_COLORS[id % THEME_COLORS.length];
    }
}
