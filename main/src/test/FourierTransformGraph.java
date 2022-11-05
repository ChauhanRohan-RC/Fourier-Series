package test;

import org.jetbrains.annotations.NotNull;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.XYStyler;
import ui.DarkChartTheme;

public class FourierTransformGraph extends XChartPanel<XYChart> {



    @NotNull
    private static XYChart createChart() {
        final XYChart chart = new XYChartBuilder().width(600).height(600).build();

        final XYStyler styler = chart.getStyler();
        final DarkChartTheme theme = new DarkChartTheme();
        theme.configureStyler(styler);
        styler.setyAxisTickLabelsFormattingFunction(d -> String.format("%.1f", d));
        styler.setxAxisTickLabelsFormattingFunction(d -> String.format("%.1f", d));
        styler.setLegendLayout(Styler.LegendLayout.Vertical);
        styler.setLegendVisible(false);
        styler.setAxisTitlesVisible(true);
        styler.setChartTitleVisible(true);

        return chart;
    }

    public FourierTransformGraph(XYChart chart) {
        super(chart);
    }
}
