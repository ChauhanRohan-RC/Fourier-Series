package xchart;

import java.awt.Graphics2D;
import xchart.internal.chartpart.Chart;
import xchart.internal.chartpart.Legend_Marker;
import xchart.internal.chartpart.Plot_Radar;
import xchart.internal.style.SeriesColorMarkerLineStyle;
import xchart.internal.style.SeriesColorMarkerLineStyleCycler;
import xchart.style.RadarStyler;
import xchart.style.theme.Theme;
import xchart.style.Styler;

public class RadarChart extends Chart<RadarStyler, RadarSeries> {

  private String[] radiiLabels;

  /**
   * Constructor - the default Chart Theme will be used (XChartTheme)
   *
   * @param width
   * @param height
   */
  public RadarChart(int width, int height) {

    super(width, height, new RadarStyler());
    plot = new Plot_Radar<>(this);
    legend = new Legend_Marker<RadarStyler, RadarSeries>(this);
  }

  /**
   * Constructor
   *
   * @param width
   * @param height
   * @param theme - pass in a instance of Theme class, probably a custom Theme.
   */
  public RadarChart(int width, int height, Theme theme) {

    this(width, height);
    styler.setTheme(theme);
  }

  /**
   * Constructor
   *
   * @param width
   * @param height
   * @param chartTheme - pass in the desired ChartTheme enum
   */
  public RadarChart(int width, int height, Styler.ChartTheme chartTheme) {

    this(width, height, chartTheme.newInstance(chartTheme));
  }

  /**
   * Constructor
   *
   * @param radarChartBuilder
   */
  public RadarChart(RadarChartBuilder radarChartBuilder) {

    this(radarChartBuilder.width, radarChartBuilder.height, radarChartBuilder.chartTheme);
    setTitle(radarChartBuilder.title);
  }

  public String[] getRadiiLabels() {

    return radiiLabels;
  }

  /**
   * Sets the radii labels
   *
   * @param radiiLabels
   */
  public void setRadiiLabels(String[] radiiLabels) {

    this.radiiLabels = radiiLabels;
  }
  /**
   * Add a graphSeries for a Radar type chart
   *
   * @param seriesName
   * @param values
   * @return
   */
  public RadarSeries addSeries(String seriesName, double[] values) {

    return addSeries(seriesName, values, null);
  }

  /**
   * Add a graphSeries for a Radar type chart
   *
   * @param seriesName
   * @param values
   * @param tooltipOverrides
   * @return
   */
  public RadarSeries addSeries(String seriesName, double[] values, String[] tooltipOverrides) {

    // Sanity checks
    sanityCheck(seriesName, values, tooltipOverrides);

    RadarSeries series = new RadarSeries(seriesName, values, tooltipOverrides);

    seriesMap.put(seriesName, series);

    return series;
  }

  private void sanityCheck(String seriesName, double[] values, String[] annotations) {

    if (radiiLabels == null) {
      throw new IllegalArgumentException("Variable labels cannot be null!!!");
    }

    if (seriesMap.containsKey(seriesName)) {
      throw new IllegalArgumentException(
          "Series name >"
              + seriesName
              + "< has already been used. Use unique names for each graphSeries!!!");
    }
    if (values == null) {
      throw new IllegalArgumentException("Values data cannot be null!!!");
    }
    if (values.length < radiiLabels.length) {
      throw new IllegalArgumentException("Too few values!!!");
    }
    for (double d : values) {
      if (d < 0 || d > 1) {
        throw new IllegalArgumentException("Values must be in [0, 1] range!!!");
      }
    }

    if (annotations != null && annotations.length < radiiLabels.length) {
      throw new IllegalArgumentException("Too few tool tips!!!");
    }
  }

  @Override
  public void paint(Graphics2D g, int width, int height) {

    setWidth(width);
    setHeight(height);

    setSeriesStyles();

    paintBackground(g);

    plot.paint(g);
    chartTitle.paint(g);
    legend.paint(g);
    annotations.forEach(x -> x.paint(g));
  }

  /** set the graphSeries color based on theme */
  private void setSeriesStyles() {

    SeriesColorMarkerLineStyleCycler seriesColorMarkerLineStyleCycler =
        new SeriesColorMarkerLineStyleCycler(
            getStyler().getSeriesColors(),
            getStyler().getSeriesMarkers(),
            getStyler().getSeriesLines());
    for (RadarSeries series : getSeriesMap().values()) {

      SeriesColorMarkerLineStyle seriesColorMarkerLineStyle =
          seriesColorMarkerLineStyleCycler.getNextSeriesColorMarkerLineStyle();

      if (series.getLineStyle() == null) { // wasn't set manually
        series.setLineStyle(seriesColorMarkerLineStyle.getStroke());
      }
      if (series.getLineColor() == null) { // wasn't set manually
        series.setLineColor(seriesColorMarkerLineStyle.getColor());
      }
      if (series.getFillColor() == null) { // wasn't set manually
        series.setFillColor(seriesColorMarkerLineStyle.getColor());
      }
      if (series.getMarker() == null) { // wasn't set manually
        series.setMarker(seriesColorMarkerLineStyle.getMarker());
      }
      if (series.getMarkerColor() == null) { // wasn't set manually
        series.setMarkerColor(seriesColorMarkerLineStyle.getColor());
      }
    }
  }
}
