package ui;

import animation.animator.AbstractAnimator;
import app.R;
import models.graph.FTGraphData;
import models.graph.FTGraphMode;
import models.graph.GraphSeries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.XYStyler;
import org.knowm.xchart.style.markers.Circle;
import org.knowm.xchart.style.markers.Marker;
import org.knowm.xchart.style.markers.None;
import provider.FunctionMeta;
import rotor.RotorState;
import rotor.RotorStateManager;
import ui.action.BaseAction;
import util.CollectionUtil;
import util.Format;
import util.async.Async;
import util.async.Canceller;
import util.async.Consumer;
import util.live.Listeners;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.*;
import java.util.function.ToDoubleFunction;

public class FTGraphPanel extends XChartPanel<XYChart> {


    public static final FTGraphMode DEFAULT_GRAPH_MODE = FTGraphMode.MAG;

    private static final String FORMAT_X_AXIS = "%.1f";     // frequency
    private static final String FORMAT_Y_AXIS = "%.2f";

    private static final String FORMAT_X_AXIS_CURSOR = "%.2f";     // frequency
    private static final String FORMAT_Y_AXIS_CURSOR = "%.2f";

    /* Current Series */

    private static final boolean CURRENT_SERIES_SHOW_IN_LEGEND = false;
    private static final Marker CURRENT_SERIES_MARKER = new Circle();
    @Nullable
    private static final Color CURRENT_SERIES_COLOR = null;
    @Nullable
    private static final XYSeries.XYSeriesRenderStyle CURRENT_SERIES_RENDER_STYLE = null;

    @NotNull
    private static String getCurrentSeriesName(@NotNull String seriesName) {
        return "Current " + seriesName;
    }

    private static boolean isCurrentSeries(@NotNull GraphSeries series) {
        return series.isCurrent();
    }
    
    /* Mappers */

    private static class MapperInfo implements ToDoubleFunction<RotorState> {

        @NotNull
        private static <T> ToDoubleFunction<T> negate(@NotNull ToDoubleFunction<T> function) {
            return in -> -function.applyAsDouble(in);
        }

        @NotNull
        public final String seriesName;
        @NotNull
        private ToDoubleFunction<RotorState> mapper;

        public MapperInfo(@NotNull String seriesName, @NotNull ToDoubleFunction<RotorState> mapper) {
            this.seriesName = seriesName;
            this.mapper = mapper;
        }

        public void negate() {
            mapper = negate(mapper);
        }

        @Override
        public double applyAsDouble(RotorState value) {
            return mapper.applyAsDouble(value);
        }
    }

    @NotNull
    private static MapperInfo @NotNull[] getMappers(@NotNull FTGraphMode graphMode, boolean negate) {
        final String series1Name;
        ToDoubleFunction<RotorState> series1Mapper;

        String series2Name = null;
        ToDoubleFunction<RotorState> series2Mapper = null;

        switch (graphMode) {
            case REAL -> {
                series1Name = "Real";
                series1Mapper = rs -> rs.getCoefficient().getReal();
            } case IMG -> {
                series1Name = "Imaginary";
                series1Mapper = rs -> rs.getCoefficient().getImaginary();
            } case MAG -> {
                series1Name = "Magnitude";
                series1Mapper = RotorState::getMagnitudeScale;
            } case PHASE -> {
                series1Name = "Phase";
                series1Mapper = RotorState::getCoefficientArgument;
            } case REAL_AND_IMG -> {
                series1Name = "Real";
                series1Mapper = rs -> rs.getCoefficient().getReal();

                series2Name = "Imaginary";
                series2Mapper = rs -> rs.getCoefficient().getImaginary();
            } default -> {      // Mag and Phase
                series1Name = "Magnitude";
                series1Mapper = RotorState::getMagnitudeScale;

                series2Name = "Phase";
                series2Mapper = RotorState::getCoefficientArgument;
            }
        }

        final MapperInfo m1 = new MapperInfo(series1Name, series1Mapper);
        final MapperInfo m2 = series2Mapper != null? new MapperInfo(series2Name, series2Mapper): null;
        if (negate) {
            m1.negate();
            if (m2 != null) {
                m2.negate();
            }
        }

        return m2 != null? new MapperInfo[] { m1, m2 }: new MapperInfo[] { m1 };
    }

    @NotNull
    private static ToDoubleFunction<RotorState> getDomainMapper(boolean negate) {
        final ToDoubleFunction<RotorState> domainMapper = RotorState::getFrequency;
        return negate? MapperInfo.negate(domainMapper): domainMapper;
    }
    
    
    /* Chart */
    
    @NotNull
    private static XYChart createChart() {
        final XYChart chart = new XYChartBuilder().width(600).height(600).build();

        final XYStyler styler = chart.getStyler();
        final DarkChartTheme theme = new DarkChartTheme();
        theme.configureStyler(styler);
        styler.setxAxisTickLabelsFormattingFunction(d -> String.format(FORMAT_X_AXIS, d));
        styler.setyAxisTickLabelsFormattingFunction(d -> String.format(FORMAT_Y_AXIS, d));
        styler.setLegendLayout(Styler.LegendLayout.Vertical);
        styler.setLegendVisible(false);
        styler.setAxisTitlesVisible(true);
        styler.setChartTitleVisible(true);

        return chart;
    }


    public interface Listener {

        void onFTGraphModeChanged(@NotNull FTGraphPanel graph, @NotNull FTGraphMode old);

        void onInvertXChanged(@NotNull FTGraphPanel graph);

        void onInvertYChanged(@NotNull FTGraphPanel graph);
    }
    

    private final FTWinderPanel.Listener panelListener = new FTWinderPanel.Listener() {
        @Override
        public void onRotorsCountChanged(@NotNull FTWinderPanel panel, int rotorsCount) {
            drawChart();
        }

        @Override
        public void onIsLoadingChanged(@NotNull FTWinderPanel panel, boolean isLoading) {
            
        }

        @Override
        public void onIsPlayingChanged(@NotNull FTWinderPanel panel, boolean playing) {

        }

        @Override
        public void onRotorsAnimationSpeedChanged(@NotNull FTWinderPanel panel, int speedPercent) {
            
        }

        @Override
        public void onRotorsAnimationRepeatModeChanged(@NotNull FTWinderPanel panel, AbstractAnimator.@NotNull RepeatMode repeatMode) {

        }

        @Override
        public void onCurrentRotorChanged(@NotNull FTWinderPanel panel, int currentRotorIndex) {
            drawChart();
        }

        @Override
        public void onPointsJoiningEnabledChanged(@NotNull FTWinderPanel panel, boolean pointsJoiningEnabled) {
            
        }
    };


    @Nullable
    private FTWinderPanel mPanel;
    @NotNull
    private volatile FTGraphMode mGraphMode;

    private volatile boolean mInvertX;
    private volatile boolean mInvertY;
    private final Listeners<Listener> listeners = new Listeners<>();

    @Nullable
    private volatile FTGraphData prevData;
    @Nullable
    private volatile Canceller mGraphDataLoader;

    private final BaseAction invertXAction;
    private final BaseAction invertYAction;
    private final JMenu graphModeMenu;
    private final ButtonGroup graphModeGroup;
    private final EnumMap<FTGraphMode, ButtonModel> graphModeButtons;
    
    public FTGraphPanel(@Nullable FTWinderPanel panel, @Nullable FTGraphMode graphMode) {
        super(createChart());

        if (graphMode == null) {
            graphMode = DEFAULT_GRAPH_MODE;
        }
        
        mPanel = panel;
        mGraphMode = graphMode;
        
        // Styling
        chart.setTitle(getChartTitle(true));        // initially
        chart.getStyler().setCustomCursorXDataFormattingFunction(val -> String.format("%s: " + FORMAT_X_AXIS_CURSOR, mGraphMode.xAxisTitle, val));

        // Actions
        invertXAction = new InvertXAction();
        invertYAction = new InvertYAction();

        graphModeMenu = new JMenu("Graph Mode");
        graphModeGroup = new ButtonGroup();
        graphModeButtons = new EnumMap<>(FTGraphMode.class);

        for (FTGraphMode _mode: FTGraphMode.values()) {
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
        attachToPanelInternal(panel, false);
//        drawChart(100);
    }
    
    public void attachToPanel(@Nullable FTWinderPanel panel) {
        if (mPanel == panel) {
            return;
        }
        
        attachToPanelInternal(panel, true);
    }

    public void attachToPanelInternal(@Nullable FTWinderPanel panel, boolean draw) {
        final FTWinderPanel cur = mPanel;
        // detach
        if (cur != null) {
            cur.removeListener(panelListener);
        }

        mPanel = panel;
        if (panel != null) {
            panel.ensureListener(panelListener);
        }

        drawChart();
    }
    
    
    @Nullable
    public RotorStateManager getRotorStateManager() {
        final FTWinderPanel p = mPanel;
        return p != null? p.getRotorStateManager(): null;
    }

    @Nullable
    public FunctionMeta getFunctionMeta() {
        final RotorStateManager manager = getRotorStateManager();
        return manager != null? manager.getFunctionMeta(): null;
    }

    @NotNull
    public String getChartTitle(boolean loading) {
        String title = "Fourier Transform";
        final FunctionMeta meta = getFunctionMeta();
        if (meta != null) {
            title += " (" + Format.ellipse(meta.displayName(), 16) + ")";
        } 
        
        if (loading) {
            title = "Loading " + title;
        }

        return title;
    }


    public int getRotorCount() {
        final FTWinderPanel p = mPanel;
        return p != null? p.getRotorCount(): 0;
    }

    public int getCurrentRotorIndex() {
        final FTWinderPanel p = mPanel;
        return p != null? p.getCurrentRotorIndex(): -1;
    }

    @NotNull
    public FTGraphMode getGraphMode() {
        return mGraphMode;
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



    protected void onGraphModeChanged(@NotNull FTGraphMode old) {
        drawChart();
        graphModeButtons.get(getGraphMode()).setSelected(true);
        listeners.dispatchOnMainThread(l -> l.onFTGraphModeChanged(FTGraphPanel.this, old));
    }

    public void setGraphMode(@NotNull FTGraphMode graphMode) {
        final FTGraphMode old = mGraphMode;
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
        listeners.dispatchOnMainThread(l -> l.onInvertXChanged(FTGraphPanel.this));
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
        listeners.dispatchOnMainThread(l -> l.onInvertYChanged(FTGraphPanel.this));
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
    private FTGraphData createGraphData(@NotNull FTGraphMode graphMode, int rotorCount, int currentRotorIndex) {
        final RotorStateManager manager = getRotorStateManager();
        final MapperInfo[] mappers;
        final boolean invertX = mInvertX;
        final boolean invertY = mInvertY;

        if (manager == null || manager.isNoOp() || rotorCount < 1 || (mappers = getMappers(graphMode, invertY)) == null || mappers.length == 0) {
            return FTGraphData.empty(graphMode);
        }
        
        final ToDoubleFunction<RotorState> domainMapper = getDomainMapper(invertX);

        final double[] domain = new double[rotorCount];
        final double[][] ranges = new double[mappers.length][rotorCount];

        for (int i=0; i < rotorCount; i++) {
            final RotorState state = manager.getRotorState(i);
            domain[i] = domainMapper.applyAsDouble(state);
            
            for (int j=0; j < mappers.length; j++) {
                ranges[j][i] = mappers[j].applyAsDouble(state);
            }
        }

        final double[] curDomain;
        final double[][] curRanges;

        if (currentRotorIndex >= 0 && currentRotorIndex < rotorCount) {
            final RotorState state = manager.getRotorState(currentRotorIndex);
            curDomain = new double[] { domainMapper.applyAsDouble(state) };
            curRanges = new double[mappers.length][1];
            
            for (int i=0; i < mappers.length; i++) {
                curRanges[i][0] = mappers[i].applyAsDouble(state);
            }
        } else {
            currentRotorIndex = -1;
            curDomain = null;
            curRanges = null;
        }

        final LinkedList<GraphSeries> list = new LinkedList<>();
        for (int i=0; i < mappers.length; i++) {
            final MapperInfo mapper = mappers[i];
            
            // Main series
            list.add(new GraphSeries(mapper.seriesName, domain, ranges[i], null, false));
            
            // current series
            if (curDomain != null) {
                list.add(new GraphSeries(getCurrentSeriesName(mapper.seriesName), curDomain, curRanges[i], null, true));
            }
        }
        
        return new FTGraphData(
                graphMode, 
                rotorCount, 
                currentRotorIndex, 
                invertX, 
                invertY, 
                list.toArray(new GraphSeries[list.size()])
        );
    }

    @NotNull
    private FTGraphData createGraphDataOnSameRotorCount(@NotNull FTGraphData data, int currentRotorIndex) {
        final List<GraphSeries> series = new ArrayList<>(List.of(data.graphSeries()));
        if (currentRotorIndex < 0 || currentRotorIndex >= data.rotorCount()) {
            currentRotorIndex = -1;
        }

        final boolean invertX = mInvertX;
        final boolean invertY = mInvertY;

        final RotorStateManager manager = getRotorStateManager();
        if (manager == null) {
            series.clear();
        } else {
            final boolean currentRotorChanged = data.currentRotorIndex() != currentRotorIndex;
            if (currentRotorChanged) {
                series.removeIf(GraphSeries::isCurrent);
            }
            
            Consumer<GraphSeries> seriesInverter = null;
            if (data.xInverted() != invertX) {
                seriesInverter = GraphSeries::negateX;
            }

            if (data.yInverted() != invertY) {
                Consumer<GraphSeries> c = GraphSeries::negateY;
                seriesInverter = seriesInverter != null? seriesInverter.andThen(c): c;
            }

            if (seriesInverter != null) {
                series.forEach(seriesInverter.tpLegacy());
            }
            
            if (currentRotorChanged && currentRotorIndex != -1) {
                final MapperInfo[] mappers = getMappers(data.graphMode(), invertY);
                if (mappers != null && mappers.length > 0) {
                    final RotorState state = manager.getRotorState(currentRotorIndex);

                    final ToDoubleFunction<RotorState> domainMapper = getDomainMapper(invertX);
                    final double[] domain = { domainMapper.applyAsDouble(state) };

                    for (final MapperInfo mapper : mappers) {
                        series.add(new GraphSeries(getCurrentSeriesName(mapper.seriesName), domain, new double[]{mapper.applyAsDouble(state)}, null, true));
                    }
                }
            }
        }
        
        if (series.isEmpty()) {
            return FTGraphData.empty(data.graphMode());
        }
        
        return new FTGraphData(
                data.graphMode(), 
                data.rotorCount(), 
                currentRotorIndex, 
                invertX, 
                invertY, 
                series.toArray(new GraphSeries[series.size()])
        );
    }

    @NotNull
    private FTGraphData createGraphDataOnSameRotorCount(@NotNull FTGraphData data) {
        return createGraphDataOnSameRotorCount(data, getCurrentRotorIndex());
    }
    
    private void cancelGraphDataLoad() {
        final Canceller c = mGraphDataLoader;
        if (c != null) {
            c.cancel(true);
        }

        mGraphDataLoader = null;
    }
    
    private void onGraphDataLoaded(@NotNull FTGraphData data) {
        prevData = data;
        
        chart.setTitle(getChartTitle(false));
        chart.setXAxisTitle(data.graphMode().xAxisTitle);
        chart.setYAxisTitle(data.graphMode().yAxisTitle);

        final Map<String, XYSeries> seriesMap = chart.getSeriesMap();
        if (CollectionUtil.notEmpty(seriesMap)) {
            seriesMap.clear();
        }

        final GraphSeries[] seriesArr = data.graphSeries();
        int mainSeries = 0;
        int legendSeries = 0;
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
            if (isCurrentSeries(series)) {
                xySeries.setShowInLegend(CURRENT_SERIES_SHOW_IN_LEGEND);
                xySeries.setMarker(CURRENT_SERIES_MARKER);
                if (CURRENT_SERIES_COLOR != null) {
                    xySeries.setMarkerColor(CURRENT_SERIES_COLOR);
                    xySeries.setLineColor(CURRENT_SERIES_COLOR);
                }
                
                if (CURRENT_SERIES_RENDER_STYLE != null) {
                    xySeries.setXYSeriesRenderStyle(CURRENT_SERIES_RENDER_STYLE); 
                }
                
                if (CURRENT_SERIES_SHOW_IN_LEGEND) {
                    legendSeries++;
                }
            } else {
                xySeries.setMarker(new None());
                mainSeries++;
                legendSeries++;
            }
        }

        chart.getStyler().setLegendVisible(legendSeries > 1);
        repaint();
    }

    public void drawChart(int rotorCount, int currentRotorIndex, boolean force, int delayMs) {
        cancelGraphDataLoad();
        
        final FTGraphData data = prevData;
        final FTGraphMode mode = mGraphMode;
        if (!force && data != null && data.graphMode() == mode && data.rotorCount() == rotorCount) {
            mGraphDataLoader = Async.execute(() -> createGraphDataOnSameRotorCount(data, currentRotorIndex), this::onGraphDataLoaded, delayMs);
            return;
        }
        
        final String chartTitle = getChartTitle(true);
        chart.setTitle(chartTitle);
        repaint();

        mGraphDataLoader = Async.execute(() -> createGraphData(mode, rotorCount, currentRotorIndex), this::onGraphDataLoaded, delayMs);
    }

    public void drawChart(int rotorCount, int currentRotorIndex, boolean force) {
        drawChart(rotorCount, currentRotorIndex, force, 0);
    }
    
    public void drawChart(boolean force) {
        drawChart(getRotorCount(), getCurrentRotorIndex(), force);
    }

    public void drawChart() {
        drawChart(false);
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
        private final FTGraphMode graphMode;

        public GraphModeAction(@NotNull FTGraphMode graphMode) {
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
