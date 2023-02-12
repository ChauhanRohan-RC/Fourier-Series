package xchart;

import xchart.internal.ChartBuilder;

/** @author Mr14huashao */
public class HeatMapChartBuilder extends ChartBuilder<HeatMapChartBuilder, HeatMapChart> {

  String xAxisTitle = "";
  String yAxisTitle = "";

  public HeatMapChartBuilder() {}

  public HeatMapChartBuilder xAxisTitle(String xAxisTitle) {

    this.xAxisTitle = xAxisTitle;
    return this;
  }

  public HeatMapChartBuilder yAxisTitle(String yAxisTitle) {

    this.yAxisTitle = yAxisTitle;
    return this;
  }

  /**
   * return fully built HeatMapChart
   *
   * @return a HeatMapChart
   */
  @Override
  public HeatMapChart build() {

    return new HeatMapChart(this);
  }
}
