package xchart.internal.chartpart;

import xchart.BoxSeries;
import xchart.style.BoxStyler;

public class Plot_Box<ST extends BoxStyler, S extends BoxSeries> extends Plot_AxesChart<ST, S> {

  public Plot_Box(Chart<ST, S> chart) {

    super(chart);
    this.plotContent = new PlotContent_Box<ST, S>(chart);
  }
}
