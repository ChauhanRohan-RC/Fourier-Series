package xchart.style.theme;

import java.awt.*;
import xchart.style.PieStyler.LabelType;
import xchart.style.colors.ChartColor;
import xchart.style.colors.MatlabSeriesColors;
import xchart.style.lines.MatlabSeriesLines;
import xchart.style.markers.Marker;
import xchart.style.markers.MatlabSeriesMarkers;

/** @author timmolter */
public class MatlabTheme extends AbstractBaseTheme {

  // Chart Style ///////////////////////////////

  // SeriesMarkers, SeriesLines, SeriesColors ///////////////////////////////

  @Override
  public Marker[] getSeriesMarkers() {

    return new MatlabSeriesMarkers().getSeriesMarkers();
  }

  @Override
  public BasicStroke[] getSeriesLines() {

    return new MatlabSeriesLines().getSeriesLines();
  }

  @Override
  public Color[] getSeriesColors() {

    return new MatlabSeriesColors().getSeriesColors();
  }

  // Chart Title ///////////////////////////////

  @Override
  public boolean isChartTitleBoxVisible() {

    return false;
  }

  // Chart Legend ///////////////////////////////

  @Override
  public Color getLegendBorderColor() {

    return ChartColor.BLACK.getColor();
  }

  // Chart Axes ///////////////////////////////

  @Override
  public Font getAxisTitleFont() {

    return getBaseFont().deriveFont(12f);
  }

  @Override
  public int getAxisTickMarkLength() {

    return 5;
  }

  @Override
  public Color getAxisTickMarksColor() {

    return ChartColor.BLACK.getColor();
  }

  @Override
  public BasicStroke getAxisTickMarksStroke() {

    return new BasicStroke(.5f);
  }

  @Override
  public boolean isAxisTicksLineVisible() {

    return false;
  }

  @Override
  public boolean isAxisTicksMarksVisible() {

    return false;
  }

  // Chart Plot Area ///////////////////////////////

  @Override
  public Color getPlotBorderColor() {

    return ChartColor.BLACK.getColor();
  }

  @Override
  public Color getPlotGridLinesColor() {

    return ChartColor.BLACK.getColor();
  }

  @Override
  public BasicStroke getPlotGridLinesStroke() {

    return new BasicStroke(
        .5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, new float[] {1f, 3.0f}, 0.0f);
  }

  @Override
  public int getPlotMargin() {

    return 3;
  }

  // Tool Tips ///////////////////////////////

  @Override
  public Color getToolTipBackgroundColor() {

    return new Color(255, 255, 220);
  }

  @Override
  public Color getToolTipBorderColor() {

    return ChartColor.BLACK.getColor();
  }

  @Override
  public Color getToolTipHighlightColor() {

    return ChartColor.BLACK.getColor();
  }

  // Category Charts ///////////////////////////////

  // Pie Charts ///////////////////////////////

  @Override
  public LabelType getLabelType() {

    return LabelType.Name;
  }

  // Line, Scatter, Area Charts ///////////////////////////////

  // Error Bars ///////////////////////////////

  // Chart Annotations ///////////////////////////////

}
