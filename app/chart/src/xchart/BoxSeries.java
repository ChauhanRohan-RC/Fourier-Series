package xchart;

import java.util.List;

import xchart.internal.series.AxesChartSeriesCategory;
import xchart.internal.chartpart.RenderableSeries;

public class BoxSeries extends AxesChartSeriesCategory {

  public BoxSeries(
      String name,
      List<?> xData,
      List<? extends Number> yData,
      List<? extends Number> extraValues,
      DataType xAxisDataType) {

    super(name, xData, yData, extraValues, xAxisDataType);
  }

  @Override
  public RenderableSeries.LegendRenderType getLegendRenderType() {

    return null;
  }
}
