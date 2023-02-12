package xchart.style.theme;

import java.awt.BasicStroke;
import java.awt.Color;
import xchart.style.colors.ChartColor;
import xchart.style.colors.XChartSeriesColors;
import xchart.style.lines.XChartSeriesLines;
import xchart.style.markers.Marker;
import xchart.style.markers.XChartSeriesMarkers;

/** @author timmolter */
public class XChartTheme extends AbstractBaseTheme {

  // Chart Style ///////////////////////////////

  @Override
  public Color getChartBackgroundColor() {

    return ChartColor.GREY.getColor();
  }

  // SeriesMarkers, SeriesLines, SeriesColors ///////////////////////////////

  @Override
  public Color[] getSeriesColors() {

    return new XChartSeriesColors().getSeriesColors();
  }

  @Override
  public Marker[] getSeriesMarkers() {

    return new XChartSeriesMarkers().getSeriesMarkers();
  }

  @Override
  public BasicStroke[] getSeriesLines() {

    return new XChartSeriesLines().getSeriesLines();
  }

  // Chart Title ///////////////////////////////

  @Override
  public boolean isChartTitleBoxVisible() {

    return false;
  }

  @Override
  public Color getChartTitleBoxBackgroundColor() {

    return ChartColor.GREY.getColor();
  }

  @Override
  public Color getChartTitleBoxBorderColor() {

    return ChartColor.GREY.getColor();
  }

  // Chart Legend ///////////////////////////////

  // Chart Axes ///////////////////////////////

  // Chart Plot Area ///////////////////////////////

  @Override
  public boolean isPlotTicksMarksVisible() {

    return false;
  }

  // Tool Tips ///////////////////////////////

  // Category Charts ///////////////////////////////

  // Pie Charts ///////////////////////////////

  // Line, Scatter, Area Charts ///////////////////////////////

  // Error Bars ///////////////////////////////

  // Chart Annotations ///////////////////////////////

}
