package xchart;

import java.awt.Graphics2D;
import xchart.internal.chartpart.Chart;
import xchart.internal.chartpart.Legend_Pie;
import xchart.internal.chartpart.Plot_Dial;
import xchart.style.DialStyler;
import xchart.style.theme.Theme;
import xchart.style.Styler;

public class DialChart extends Chart<DialStyler, DialSeries> {

  /**
   * Constructor - the default Chart Theme will be used (XChartTheme)
   *
   * @param width
   * @param height
   */
  public DialChart(int width, int height) {

    super(width, height, new DialStyler());
    plot = new Plot_Dial<DialStyler, DialSeries>(this);
    legend = new Legend_Pie<DialStyler, DialSeries>(this);
  }

  /**
   * Constructor
   *
   * @param width
   * @param height
   * @param theme - pass in a instance of Theme class, probably a custom Theme.
   */
  public DialChart(int width, int height, Theme theme) {

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
  public DialChart(int width, int height, Styler.ChartTheme chartTheme) {

    this(width, height, chartTheme.newInstance(chartTheme));
  }

  /**
   * Constructor
   *
   * @param chartBuilder
   */
  public DialChart(DialChartBuilder chartBuilder) {

    this(chartBuilder.width, chartBuilder.height, chartBuilder.chartTheme);
    setTitle(chartBuilder.title);
  }

  /**
   * Add a graphSeries for a Dial type chart
   *
   * @param seriesName
   * @param value
   * @return
   */
  public DialSeries addSeries(String seriesName, double value) {

    return addSeries(seriesName, value, null);
  }

  /**
   * Add a graphSeries for a Dial type chart
   *
   * @param seriesName
   * @param value
   * @param label
   * @return
   */
  public DialSeries addSeries(String seriesName, double value, String label) {

    // Sanity checks
    sanityCheck(seriesName, value);

    DialSeries series = new DialSeries(seriesName, value, label);

    seriesMap.clear(); // only allow one graphSeries per dial chart
    seriesMap.put(seriesName, series);

    return series;
  }

  private void sanityCheck(String seriesName, double value) {

    if (seriesMap.containsKey(seriesName)) {
      throw new IllegalArgumentException(
          "Series name >"
              + seriesName
              + "< has already been used. Use unique names for each graphSeries!!!");
    }
    if (value < 0 || value > 1) {
      throw new IllegalArgumentException("Value must be in [0, 1] range!!!");
    }
  }

  @Override
  public void paint(Graphics2D g, int width, int height) {

    setWidth(width);
    setHeight(height);

    paintBackground(g);

    plot.paint(g);
    chartTitle.paint(g);
    //    legend.paint(g); // no legend for dial charts
    annotations.forEach(x -> x.paint(g));
  }
}
