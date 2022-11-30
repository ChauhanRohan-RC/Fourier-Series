package ui;

import java.awt.*;

import app.Colors;
import org.jetbrains.annotations.NotNull;
import xchart.style.Styler;
import xchart.style.XYStyler;
import xchart.style.markers.Marker;
import xchart.style.markers.XChartSeriesMarkers;
import xchart.style.theme.AbstractBaseTheme;

public class DarkChartTheme extends AbstractBaseTheme {

    public static final BasicStroke NONE = new BasicStroke();
    public static final BasicStroke SOLID = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);

    public static final BasicStroke DASH_DOT =
            new BasicStroke(
                    1.0f,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER,
                    10.0f,
                    new float[]{3.0f, 1.0f},
                    0.0f);

    public static final BasicStroke DASH_DASH =
            new BasicStroke(
                    1.0f,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER,
                    10.0f,
                    new float[]{3.0f, 3.0f},
                    0.0f);

    public static final BasicStroke DOT_DOT =
            new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, new float[]{2.0f}, 0.0f);


    public static final BasicStroke[] SERIES_LINE_STROKES = {
            SOLID,
            DASH_DOT,
            DASH_DASH,
            DOT_DOT
    };


    public static final Font FONT_BASE = BASE_FONT;
    public static final Font FONT_CURSOR = FONT_BASE.deriveFont(12f);
    public static final Font FONT_BUTTON = FONT_BASE.deriveFont(12f);
    public static final Font FONT_LEGEND = FONT_BASE.deriveFont(10f);

    public static final Color COLOR_AXIS = new Color(108, 201, 255);

    @NotNull
    public Styler configureStyler(Styler styler) {
        if (styler instanceof final XYStyler xy) {
            xy.setTheme(this);
            xy.setZoomEnabled(isZoomEnabled());
            xy.setCursorEnabled(isCursorEnabled());
        }

        styler.setChartButtonBackgroundColor(getChartButtonBackgroundColor())
                .setChartButtonFontColor(getChartButtonFontColor())
                .setChartButtonBorderColor(getChartButtonBorderColor())
                .setChartButtonFontColor(getChartButtonFontColor())
                .setChartButtonFont(getChartButtonFont());

        return styler;
    }

    // Chart Style ///////////////////////////////

    @Override
    public Color getChartBackgroundColor() {
        return Colors.BG_DARK;
    }

    @Override
    public Color getChartFontColor() {
        return Colors.FG_DARK;
    }

    @Override
    public int getChartPadding() {
        return 15;
    }

    @Override
    public Color getAxisTickLabelsColor() {
        return Colors.FG_MEDIUM;
    }

    @Override
    public Color getAxisTickMarksColor() {
        return COLOR_AXIS;
    }


    @Override
    public Color getPlotBackgroundColor() {
        return Colors.BG_DARK;
    }


    @Override
    public Color getPlotBorderColor() {
        return Colors.ACCENT_FG_MEDIUM;
    }

    @Override
    public boolean isPlotBorderVisible() {
        return false;
    }

    @Override
    public int getPlotMargin() {
        return 0;
    }

    @Override
    public BasicStroke getPlotGridLinesStroke() {
        return new BasicStroke(0.4f);
    }

    @Override
    public Color getPlotGridLinesColor() {
        return Colors.BG_LIGHT;
    }

    // SeriesMarkers, SeriesLines, SeriesColors ///////////////////////////////

    @Override
    public Color[] getSeriesColors() {
        return Colors.THEME_COLORS;
    }

    @Override
    public Marker[] getSeriesMarkers() {
        return new XChartSeriesMarkers().getSeriesMarkers();
    }

    @Override
    public BasicStroke[] getSeriesLines() {
        return SERIES_LINE_STROKES;
    }

    // Chart Title ///////////////////////////////

    @Override
    public boolean isChartTitleBoxVisible() {
        return false;
    }

    @Override
    public Color getChartTitleBoxBackgroundColor() {
        return getChartBackgroundColor();
    }

    @Override
    public Color getChartTitleBoxBorderColor() {
        return getChartBackgroundColor();
    }

    // Chart Legend ///////////////////////////////

    // Chart Axes ///////////////////////////////

    // Chart Plot Area ///////////////////////////////


    @Override
    public boolean isPlotTicksMarksVisible() {
        return false;
    }

    // Tool Tips ///////////////////////////////

    @Override
    public boolean isToolTipsEnabled() {
        return false;
    }

    @Override
    public Styler.ToolTipType getToolTipType() {
        return Styler.ToolTipType.xAndYLabels;
    }


    @Override
    public Color getToolTipBackgroundColor() {
        return Colors.ACCENT_BG_DARK;
    }

    @Override
    public Color getToolTipHighlightColor() {
        return Colors.ACCENT_FG_MEDIUM;
    }

    @Override
    public Color getToolTipBorderColor() {
        return Colors.ACCENT_FG_DARK;
    }

    @Override
    public boolean isCursorEnabled() {
        return true;
    }

    @Override
    public Color getCursorColor() {
        return Colors.ACCENT_FG_DARK;
    }

    @Override
    public float getCursorSize() {
        return 1;
    }

    @Override
    public Color getCursorBackgroundColor() {
        return Colors.ACCENT_BG_DARK;
    }

    @Override
    public Color getCursorFontColor() {
        return Colors.FG_DARK;
    }

    @Override
    public Font getCursorFont() {
        return FONT_CURSOR;
    }

    @Override
    public boolean isZoomEnabled() {
        return true;
    }


    @Override
    public int getChartButtonMargin() {
        return super.getChartButtonMargin();
    }


    @Override
    public Color getChartButtonBackgroundColor() {
        return Colors.ACCENT_BG_DARK;
    }

    @Override
    public Color getChartButtonFontColor() {
        return Colors.FG_DARK;
    }

    @Override
    public Color getChartButtonBorderColor() {
        return Colors.ACCENT_FG_DARK;
    }

    @Override
    public Font getChartButtonFont() {
        return FONT_BUTTON;
    }

    @Override
    public boolean isLegendVisible() {
        return false;
    }

    @Override
    public Color getLegendBackgroundColor() {
        return Colors.BG_MEDIUM;
    }

    @Override
    public Color getLegendBorderColor() {
        return Colors.ACCENT_BG_MEDIUM;
    }

    @Override
    public Styler.LegendPosition getLegendPosition() {
        return Styler.LegendPosition.InsideSE;
    }

    @Override
    public Font getLegendFont() {
        return FONT_LEGEND;
    }

    @Override
    public int getLegendPadding() {
        return 5;
    }

    // Category Charts ///////////////////////////////

    // Pie Charts ///////////////////////////////

    // Line, Scatter, Area Charts ///////////////////////////////

    // Error Bars ///////////////////////////////

    // Chart Annotations ///////////////////////////////

}
