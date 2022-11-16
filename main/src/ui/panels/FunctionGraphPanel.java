package ui.panels;

import app.R;
import function.definition.ComplexDomainFunctionI;
import models.RealTransform;
import models.graph.FunctionGraphData;
import models.graph.FunctionGraphMode;
import models.graph.GraphSeries;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.XYStyler;
import org.knowm.xchart.style.markers.None;
import provider.FunctionMeta;
import ui.DarkChartTheme;
import ui.action.BaseAction;
import util.CollectionUtil;
import util.async.Async;
import util.async.Canceller;
import util.live.Listeners;
import util.main.ComplexUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.EnumMap;
import java.util.Map;

public class FunctionGraphPanel extends XChartPanel<XYChart> {

    private static final int DEFAULT_SAMPLE_COUNT = 2000;
    private static final FunctionGraphMode DEFAULT_GRAPH_MODE = FunctionGraphMode.OUTPUT_SPACE;

//    private final BaseLive.Observer<FunctionGraphMode> graphModeObserver = new BaseLive.Observer<>() {
//        @Override
//        public void onChanged(@NotNull BaseLive<? extends FunctionGraphMode, ?> live, FunctionGraphMode old) {
//            drawChart(0);
//        }
//
//        @Override
//        public void onActiveStateChanged(@NotNull BaseLive<? extends FunctionGraphMode, ?> live, boolean nowActive) {
//        }
//    };

    public interface Listener {

        void onFunctionGraphModeChanged(@NotNull FunctionGraphPanel graph, @NotNull FunctionGraphMode old);

        void onInvertXChanged(@NotNull FunctionGraphPanel graph);

        void onInvertYChanged(@NotNull FunctionGraphPanel graph);
    }

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


    @NotNull
    private final ComplexDomainFunctionI function;
    @NotNull
    private final FunctionMeta functionMeta;
    @NotNull
    private volatile FunctionGraphMode mGraphMode;
    private volatile boolean mInvertX;
    private volatile boolean mInvertY;

    private final Listeners<Listener> listeners = new Listeners<>();

    private double @Nullable [] cacheDomain;
    private Complex @Nullable [] cacheRange;
    @Nullable
    private Canceller mGraphDataLoader;

    private final BaseAction invertXAction;
    private final BaseAction invertYAction;
    private final JMenu graphModeMenu;
    private final ButtonGroup graphModeGroup;
    private final EnumMap<FunctionGraphMode, ButtonModel> graphModeButtons;

    public FunctionGraphPanel(@NotNull ComplexDomainFunctionI _function,
                              @NotNull FunctionMeta _functionMeta,
                              @Nullable FunctionGraphMode functionGraphMode) {
        super(createChart());

        if (functionGraphMode == null) {
            functionGraphMode = _function.getDefaultGraphMode();
            if (functionGraphMode == null) {
                functionGraphMode = DEFAULT_GRAPH_MODE;
            }
        }

        this.function = _function;
        this.functionMeta = _functionMeta;
        mGraphMode = functionGraphMode;

        // Styling
        chart.setTitle(getChartTitle(true));        // initially
        chart.getStyler().setCustomCursorXDataFormattingFunction(val -> String.format("%s: %.1f", mGraphMode.xAxisTitle, val));

        // Actions
        invertXAction = new InvertXAction();
        invertYAction = new InvertYAction();

        graphModeMenu = new JMenu("Graph Mode");
        graphModeGroup = new ButtonGroup();
        graphModeButtons = new EnumMap<>(FunctionGraphMode.class);

        for (FunctionGraphMode _mode: FunctionGraphMode.values()) {
            final JRadioButtonMenuItem item = new JRadioButtonMenuItem(new GraphModeAction(_mode));

            graphModeButtons.put(_mode, item.getModel());
            graphModeGroup.add(item);
            graphModeMenu.add(item);
        }

        graphModeButtons.get(getGraphMode()).setSelected(true);
        addExtraMenuBinder(menu -> {
            menu.add(graphModeMenu);
            menu.addSeparator();
            menu.add(new JCheckBoxMenuItem(invertXAction));
            menu.add(new JCheckBoxMenuItem(invertYAction));
        });

        // Draw
//        drawChart(100);
    }

    public FunctionGraphPanel(@NotNull ComplexDomainFunctionI _function, @NotNull FunctionMeta _functionMeta) {
        this(_function, _functionMeta, null);
    }

    @NotNull
    public ComplexDomainFunctionI getFunction() {
        return function;
    }

    @NotNull
    public FunctionMeta getFunctionMeta() {
        return functionMeta;
    }

    @NotNull
    public FunctionGraphMode getGraphMode() {
        return mGraphMode;
    }

    @NotNull
    public String getChartTitle(boolean loading) {
        String title = functionMeta.displayName();
        if (loading) {
            title = "Loading " + title;
        }

        return title;
    }

    public boolean addListener(@NotNull Listener listener) {
        return listeners.addListener(listener);
    }

    public boolean removeListener(@NotNull Listener listener) {
        return listeners.removeListener(listener);
    }

    public boolean ensureListener(@NotNull Listener listener) {
        return listeners.ensureListener(listener);
    }



    protected void onGraphModeChanged(@NotNull FunctionGraphMode old) {
        drawChart();
        graphModeButtons.get(getGraphMode()).setSelected(true);
        listeners.dispatchOnMainThread(l -> l.onFunctionGraphModeChanged(FunctionGraphPanel.this, old));
    }

    public void setGraphMode(@NotNull FunctionGraphMode graphMode) {
        final FunctionGraphMode old = mGraphMode;
        if (old == graphMode)
            return;

        mGraphMode = graphMode;
        onGraphModeChanged(old);
    }

    public boolean isXInverted() {
        return mInvertX;
    }

    protected void onInvertXChanged() {
        drawChart();
        invertXAction.setSelected(isXInverted());
        listeners.dispatchOnMainThread(l -> l.onInvertXChanged(FunctionGraphPanel.this));
    }

    public void setInvertX(boolean invertX) {
        final boolean old = mInvertX;
        if (old == invertX)
            return;

        mInvertX = invertX;
        onInvertXChanged();
    }

    public boolean toggleInvertX() {
        final boolean newState = !mInvertX;
        setInvertX(newState);
        return newState;
    }


    public boolean isYInverted() {
        return mInvertY;
    }

    protected void onInvertYChanged() {
        drawChart();
        invertYAction.setSelected(isYInverted());
        listeners.dispatchOnMainThread(l -> l.onInvertYChanged(FunctionGraphPanel.this));
    }

    public void setInvertY(boolean invertY) {
        final boolean old = mInvertY;
        if (old == invertY)
            return;

        mInvertY = invertY;
        onInvertYChanged();
    }

    public boolean toggleInvertY() {
        final boolean newState = !mInvertY;
        setInvertY(newState);
        return newState;
    }



    @NotNull
    private FunctionGraphData createGraphData(@NotNull FunctionGraphMode mode, int sampleCount) {
        if (cacheDomain != null && cacheDomain.length != sampleCount) {
            cacheDomain = null;     // invalidate
        }

        double[] domain = cacheDomain;
        if (domain == null) {
            domain = function.createSamplesDomain(sampleCount);
            cacheDomain = domain;
            cacheRange = null;       // invalidate
        }

        Complex[] range = cacheRange;
        if (range == null) {
            range = function.createSamplesRange(domain);
            cacheRange = range;
        }

        final boolean invertX = mInvertX;
        final boolean invertY = mInvertY;

        final GraphSeries[] series = switch (mode) {
            case INPUT_VS_OUT_REAL -> {
                if (invertX) {
                    domain = ComplexUtil.negateCopy(domain);
                }

                yield new GraphSeries[] {
                        new GraphSeries("Real", domain, RealTransform.apply(range, RealTransform.REAL, invertY))
                };
            }
            case INPUT_VS_OUT_IMAGINARY -> {
                if (invertX) {
                    domain = ComplexUtil.negateCopy(domain);
                }

                yield new GraphSeries[] {
                        new GraphSeries("Imaginary", domain, RealTransform.apply(range, RealTransform.IMAGINARY, invertY))
                };
            }
            case INPUT_VS_OUT_REAL_AND_IMG -> {
                if (invertX) {
                    domain = ComplexUtil.negateCopy(domain);
                }

                yield new GraphSeries[] {
                        new GraphSeries("Real", domain, RealTransform.apply(range, RealTransform.REAL, invertY)),
                        new GraphSeries("Imaginary", domain, RealTransform.apply(range, RealTransform.IMAGINARY, invertY))
                };
            }
            case INPUT_VS_OUT_MAGNITUDE -> {
                if (invertX) {
                    domain = ComplexUtil.negateCopy(domain);
                }

                yield new GraphSeries[] {
                        new GraphSeries("Magnitude", domain, RealTransform.apply(range, RealTransform.MAGNITUDE, invertY))
                };
            }
            case INPUT_VS_OUT_ARGUMENT -> {
                if (invertX) {
                    domain = ComplexUtil.negateCopy(domain);
                }

                yield new GraphSeries[] {
                        new GraphSeries("Argument", domain, RealTransform.apply(range, RealTransform.ARGUMENT, invertY))
                };
            }
            case INPUT_VS_OUT_MAG_AND_ARG -> {
                if (invertX) {
                    domain = ComplexUtil.negateCopy(domain);
                }

                yield new GraphSeries[] {
                        new GraphSeries("Magnitude", domain, RealTransform.apply(range, RealTransform.MAGNITUDE, invertY)),
                        new GraphSeries("Argument", domain, RealTransform.apply(range, RealTransform.ARGUMENT, invertY))
                };
            }

            // DEFAULT: Output space
            default -> new GraphSeries[] {
                    new GraphSeries("Imaginary", RealTransform.apply(range, RealTransform.REAL, invertX), RealTransform.apply(range, RealTransform.IMAGINARY, invertY))
            };
        };

        return new FunctionGraphData(mode, series);
    }

    private void cancelGraphDataLoad() {
        final Canceller c = mGraphDataLoader;
        if (c != null) {
            c.cancel(true);
        }

        mGraphDataLoader = null;
    }


    private void onGraphDataLoaded(@NotNull FunctionGraphData data) {
        chart.setTitle(getChartTitle(false));
        chart.setXAxisTitle(data.graphMode().xAxisTitle);
        chart.setYAxisTitle(data.graphMode().yAxisTitle);

        final Map<String, XYSeries> seriesMap = chart.getSeriesMap();
        if (CollectionUtil.notEmpty(seriesMap)) {
            seriesMap.clear();
        }

        final GraphSeries[] seriesArr = data.graphSeries();
        for (GraphSeries series: seriesArr) {
//            final boolean hasSeries = chart.getSeriesMap().containsKey(graphSeries.name());
//            final XYSeries xySeries;
//            if (hasSeries) {
//                xySeries = chart.updateXYSeries(graphSeries.name(), graphSeries.xData(), graphSeries.yData(), null);
//            } else {
//                xySeries = chart.addSeries(graphSeries.name(), graphSeries.xData(), graphSeries.yData(), null);
//            }

            final XYSeries xySeries = addXYSeries(series);

            // Style Series
            xySeries.setMarker(new None());
        }

        chart.getStyler().setLegendVisible(seriesArr.length > 1);
        repaint();
    }

    public void drawChart(int delayMs) {
        cancelGraphDataLoad();

        final FunctionGraphMode mode = mGraphMode;
        final String chartTitle = getChartTitle(true);
        chart.setTitle(chartTitle);
        repaint();

        mGraphDataLoader = Async.execute(() -> createGraphData(mode, DEFAULT_SAMPLE_COUNT), this::onGraphDataLoaded, delayMs);
    }

    public void drawChart() {
        drawChart(0);
    }




    private class InvertXAction extends BaseAction {

        public InvertXAction() {
            setName(R.getInvertXText());
            setShortDescription(R.getInvertXShortDescription());
            setSelected(isXInverted());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            toggleInvertX();
        }
    }

    private class InvertYAction extends BaseAction {

        public InvertYAction() {
            setName(R.getInvertYText());
            setShortDescription(R.getInvertYShortDescription());
            setSelected(isYInverted());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            toggleInvertY();
        }
    }

    private class GraphModeAction extends BaseAction {

        @NotNull
        private final FunctionGraphMode graphMode;

        public GraphModeAction(@NotNull FunctionGraphMode graphMode) {
            this.graphMode = graphMode;
            setName(graphMode.displayName);
            setActionCommand(graphMode.toString());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setGraphMode(graphMode);
        }
    }
}
