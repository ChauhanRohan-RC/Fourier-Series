package xchart;

import xchart.internal.chartpart.RenderableSeries;
import xchart.internal.series.NoMarkersSeries;

/**
 * A Series containing X, Y and bubble size data to be plotted on a Chart
 *
 * @author timmolter
 */
public class BubbleSeries extends NoMarkersSeries {

  private BubbleSeriesRenderStyle bubbleSeriesRenderStyle = null;

  /**
   * Constructor
   *
   * @param name
   * @param xData
   * @param yData
   * @param bubbleSizes
   */
  public BubbleSeries(String name, double[] xData, double[] yData, double[] bubbleSizes) {

    super(name, xData, yData, bubbleSizes, DataType.Number);
  }

  public BubbleSeriesRenderStyle getBubbleSeriesRenderStyle() {

    return bubbleSeriesRenderStyle;
  }

  public void setBubbleSeriesRenderStyle(BubbleSeriesRenderStyle bubbleSeriesRenderStyle) {

    this.bubbleSeriesRenderStyle = bubbleSeriesRenderStyle;
  }

  @Override
  public RenderableSeries.LegendRenderType getLegendRenderType() {

    return bubbleSeriesRenderStyle.getLegendRenderType();
  }

  public enum BubbleSeriesRenderStyle implements RenderableSeries {
    Round(LegendRenderType.Box);

    private final LegendRenderType legendRenderType;

    BubbleSeriesRenderStyle(LegendRenderType legendRenderType) {

      this.legendRenderType = legendRenderType;
    }

    @Override
    public LegendRenderType getLegendRenderType() {

      return legendRenderType;
    }
  }
}
