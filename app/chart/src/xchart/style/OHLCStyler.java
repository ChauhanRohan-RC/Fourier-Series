package xchart.style;

import xchart.OHLCSeries;
import xchart.OHLCSeries.OHLCSeriesRenderStyle;
import xchart.style.theme.Theme;

/** @author arthurmcgibbon */
public class OHLCStyler extends AxesChartStyler {

  private OHLCSeriesRenderStyle ohlcSeriesRenderStyle;

  /** Constructor */
  public OHLCStyler() {

    setAllStyles();
  }

  @Override
  protected void setAllStyles() {

    super.setAllStyles();
    ohlcSeriesRenderStyle = OHLCSeriesRenderStyle.Candle; // set default to candle
  }

  public OHLCSeries.OHLCSeriesRenderStyle getDefaultSeriesRenderStyle() {

    return ohlcSeriesRenderStyle;
  }

  /**
   * Sets the default graphSeries render style for the chart (candle, hilo, etc.) You can override the
   * graphSeries render style individually on each Series object.
   *
   * @param ohlcSeriesRenderStyle
   */
  public OHLCStyler setDefaultSeriesRenderStyle(OHLCSeriesRenderStyle ohlcSeriesRenderStyle) {

    this.ohlcSeriesRenderStyle = ohlcSeriesRenderStyle;
    return this;
  }

  /**
   * Set the theme the styler should use
   *
   * @param theme
   */
  public void setTheme(Theme theme) {

    this.theme = theme;
    setAllStyles();
  }
}
