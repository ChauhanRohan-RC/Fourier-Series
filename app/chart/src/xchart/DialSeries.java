package xchart;

import xchart.internal.series.Series;
import xchart.internal.chartpart.RenderableSeries;

/**
 * A Series containing Radar data to be plotted on a Chart
 *
 * @author timmolter
 */
public class DialSeries extends Series {

  private double value;
  private final String label;

  /**
   * @param label Adds custom label for graphSeries. If label is null, it is automatically calculated.
   */
  public DialSeries(String name, double value, String label) {

    super(name);
    this.value = value;
    this.label = label;
  }

  public double getValue() {

    return value;
  }

  public void setValue(double value) {

    this.value = value;
  }

  public String getLabel() {

    return label;
  }

  // TODO solve this with class/interface heirarchy instead
  @Override
  public RenderableSeries.LegendRenderType getLegendRenderType() {

    // Dial charts don't have a legend
    return null;
  }
}
