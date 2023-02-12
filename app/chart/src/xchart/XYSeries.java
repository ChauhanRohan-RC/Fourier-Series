package xchart;

import org.jetbrains.annotations.Nullable;
import xchart.internal.chartpart.RenderableSeries;
import xchart.internal.series.AxesChartSeriesNumericalNoErrorBars;

import java.awt.*;
import java.util.function.IntFunction;


/**
 * A Series containing X and Y data to be plotted on a Chart
 *
 * @author timmolter
 */
public class XYSeries extends AxesChartSeriesNumericalNoErrorBars {

    private XYSeriesRenderStyle xySeriesRenderStyle = null;
    // smooth curve
    private boolean smooth;

    @Nullable
    private IntFunction<Color> colorFilter;

    @Nullable
    private Object tag;

    /**
     * Constructor
     *
     * @param name
     * @param xData
     * @param yData
     * @param errorBars
     */
    public XYSeries(
            String name, double[] xData, double[] yData, double[] errorBars, DataType axisType) {

        super(name, xData, yData, errorBars, axisType);
    }

    public XYSeries setColorFilter(@Nullable IntFunction<Color> colorFilter) {
        this.colorFilter = colorFilter;
        return this;
    }

    @Nullable
    public IntFunction<Color> getColorFilter() {
        return colorFilter;
    }

    public Object getTag() {
        return tag;
    }

    public XYSeries setTag(Object tag) {
        this.tag = tag;
        return this;
    }

    @Nullable
    public Color getLineColor(int index) {
        Color c = null;
        if (colorFilter != null) {
            c = colorFilter.apply(index);
        }

        if (c == null) {
            c = getLineColor();
        }

        return c;
    }


    public XYSeriesRenderStyle getXYSeriesRenderStyle() {

        return xySeriesRenderStyle;
    }

    public XYSeries setXYSeriesRenderStyle(XYSeriesRenderStyle chartXYSeriesRenderStyle) {

        this.xySeriesRenderStyle = chartXYSeriesRenderStyle;
        return this;
    }

    @Override
    public RenderableSeries.LegendRenderType getLegendRenderType() {

        return xySeriesRenderStyle.getLegendRenderType();
    }

    // TODO what is this again?
    public boolean isSmooth() {
        return smooth;
    }

    public void setSmooth(boolean smooth) {
        this.smooth = smooth;
    }

    public enum XYSeriesRenderStyle implements RenderableSeries {
        Line(LegendRenderType.Line),

        Area(LegendRenderType.Line),

        Step(LegendRenderType.Line),

        StepArea(LegendRenderType.Line),

        PolygonArea(LegendRenderType.Box),

        Scatter(LegendRenderType.Scatter);

        private final LegendRenderType legendRenderType;

        XYSeriesRenderStyle(LegendRenderType legendRenderType) {

            this.legendRenderType = legendRenderType;
        }

        @Override
        public LegendRenderType getLegendRenderType() {

            return legendRenderType;
        }
    }
}
