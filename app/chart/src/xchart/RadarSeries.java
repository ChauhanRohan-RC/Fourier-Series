package xchart;

import java.awt.BasicStroke;
import java.awt.Color;

import xchart.internal.series.MarkerSeries;
import xchart.style.markers.Marker;
import xchart.internal.chartpart.RenderableSeries;

/** A Series containing Radar data to be plotted on a Chart */
public class RadarSeries extends MarkerSeries {

  /** Line Style */
  private BasicStroke stroke;

  /** Line Color */
  private Color lineColor;

  /** Line Width */
  private float lineWidth;

  /** Marker */
  private Marker marker;

  /** Marker Color */
  private Color markerColor;

  private double[] values;
  private String[] tooltipOverrides;

  // TODO refactor tooltips overrride
  /**
   * @param tooltipOverrides Adds custom tooltipOverrides for graphSeries. If tooltipOverrides is null,
   *     they are automatically generated.
   */
  public RadarSeries(String name, double[] values, String[] tooltipOverrides) {

    super(name, DataType.Number);
    this.values = values;
    this.tooltipOverrides = tooltipOverrides;
  }

  public double[] getValues() {

    return values;
  }

  public void setValues(double[] values) {

    this.values = values;
  }

  public String[] getTooltipOverrides() {

    return tooltipOverrides;
  }

  @Override
  protected void calculateMinMax() {}

  public BasicStroke getLineStyle() {

    return stroke;
  }

  /**
   * Set the line style of the graphSeries
   *
   * @param basicStroke
   */
  public RadarSeries setLineStyle(BasicStroke basicStroke) {

    stroke = basicStroke;
    if (this.lineWidth > 0.0f) {
      stroke =
          new BasicStroke(
              lineWidth,
              this.stroke.getEndCap(),
              this.stroke.getLineJoin(),
              this.stroke.getMiterLimit(),
              this.stroke.getDashArray(),
              this.stroke.getDashPhase());
    }
    return this;
  }

  public Color getLineColor() {

    return lineColor;
  }

  /**
   * Set the line color of the graphSeries
   *
   * @param color
   */
  public RadarSeries setLineColor(java.awt.Color color) {

    this.lineColor = color;
    return this;
  }

  public float getLineWidth() {

    return lineWidth;
  }

  /**
   * Set the line width of the graphSeries
   *
   * @param lineWidth
   */
  public RadarSeries setLineWidth(float lineWidth) {

    this.lineWidth = lineWidth;
    return this;
  }

  public Marker getMarker() {

    return marker;
  }

  /**
   * Sets the marker for the graphSeries
   *
   * @param marker
   */
  public RadarSeries setMarker(Marker marker) {

    this.marker = marker;
    return this;
  }

  public Color getMarkerColor() {

    return markerColor;
  }

  /**
   * Sets the marker color for the graphSeries
   *
   * @param color
   */
  public RadarSeries setMarkerColor(java.awt.Color color) {

    this.markerColor = color;
    return this;
  }

  @Override
  public RenderableSeries.LegendRenderType getLegendRenderType() {

    return RenderableSeries.LegendRenderType.Line;
  }

  public void setTooltipOverrides(String[] tooltipOverrides) {

    this.tooltipOverrides = tooltipOverrides;
  }
}
