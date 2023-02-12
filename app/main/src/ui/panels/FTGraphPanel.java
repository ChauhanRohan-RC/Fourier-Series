package ui.panels;

import action.BaseAction;
import animation.animator.AbstractAnimator;
import app.R;
import misc.CollectionUtil;
import misc.Format;
import models.graph.FTGraphData;
import models.graph.FTGraphMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import provider.FunctionMeta;
import rotor.RotorState;
import rotor.RotorStateManager;
import rotor.frequency.RotorFrequencyProviderI;
import ui.DarkChartTheme;
import async.Async;
import async.Canceller;
import async.Consumer;
import async.Function;
import live.Listeners;
import xchart.*;
import xchart.style.Styler;
import xchart.style.XYStyler;
import xchart.style.markers.Circle;
import xchart.style.markers.Marker;
import xchart.style.markers.None;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.ToDoubleFunction;

public class FTGraphPanel extends XChartPanel<XYChart> {

    public static final FTGraphMode DEFAULT_GRAPH_MODE = FTGraphMode.MAG;

    private static final DecimalFormat FORMAT_X_AXIS = Format.createScientificDecimalFormat(2, 2);     // Frequency
    private static final DecimalFormat FORMAT_Y_AXIS = Format.createScientificDecimalFormat(3, 2);

    private static final DecimalFormat FORMAT_X_CURSOR = Format.createScientificDecimalFormat(4, 3);     // Frequency
    private static final DecimalFormat FORMAT_Y_CURSOR = Format.createScientificDecimalFormat(4, 3);


    /* Live */
    private static final boolean DEFAULT_SMOOTH = false;
    private static final boolean DEFAULT_DRAW_AS_LIVE = true;
    private static final Color COLOR_LIVE_POST_CURRENT = new Color(100, 100, 100);

    @Nullable
    private static IntFunction<Color> getMainSeriesLiveColorFilter(int curRotorIndex) {
        if (curRotorIndex == -1)
            return null;

        return i -> i <= curRotorIndex? null: COLOR_LIVE_POST_CURRENT;
    }

    /* Series Type */

    private enum SeriesType {
        MAIN(name -> name,
                true,
                new None(),
                null,
                null,
                null
        ),

        CURRENT(name -> "Current " + name,
                false,
                new Circle(),
                null,
                null,
                null
        );

        @NotNull
        private final Function<String, String> mapperNameToSeriesName;

        public final boolean showInLegend;
        @Nullable
        public final Marker marker;
        @Nullable
        public final XYSeries.XYSeriesRenderStyle renderStyle;
        @Nullable
        public final Color lineColor;
        @Nullable
        public final Color markerColor;

        SeriesType(@NotNull Function<String, String> mapperNameToSeriesName, boolean showInLegend, @Nullable Marker marker, XYSeries.XYSeriesRenderStyle renderStyle, @Nullable Color lineColor, @Nullable Color markerColor) {
            this.mapperNameToSeriesName = mapperNameToSeriesName;
            this.showInLegend = showInLegend;
            this.marker = marker;
            this.renderStyle = renderStyle;
            this.lineColor = lineColor;
            this.markerColor = markerColor;
        }

        @NotNull
        public String getSeriesName(@NotNull String mapperName) {
            return mapperNameToSeriesName.apply(mapperName);
        }
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
    private static MapperInfo @NotNull[] getMappers(@NotNull FTGraphMode graphMode, double domainRange, boolean negate) {
        final String series1Name;
        ToDoubleFunction<RotorState> series1Mapper;

        String series2Name = null;
        ToDoubleFunction<RotorState> series2Mapper = null;

        switch (graphMode) {
            case REAL -> {
                series1Name = "Real";
                series1Mapper = rs -> rs.getFourierTransformOutput(domainRange).getReal();
            } case IMG -> {
                series1Name = "Imaginary";
                series1Mapper = rs -> rs.getFourierTransformOutput(domainRange).getImaginary();
            } case MAG -> {
                series1Name = "Magnitude";
                series1Mapper = rs -> rs.getFourierTransformOutput(domainRange).abs();
            } case PHASE -> {
                series1Name = "Phase";
                series1Mapper = rs -> rs.getFourierTransformOutput(domainRange).getArgument();
            } case REAL_AND_IMG -> {
                series1Name = "Real";
                series1Mapper = rs -> rs.getFourierTransformOutput(domainRange).getReal();

                series2Name = "Imaginary";
                series2Mapper = rs -> rs.getFourierTransformOutput(domainRange).getImaginary();
            } default -> {      // Mag and Phase
                series1Name = "Magnitude";
                series1Mapper = rs -> rs.getFourierTransformOutput(domainRange).abs();

                series2Name = "Phase";
                series2Mapper = rs -> rs.getFourierTransformOutput(domainRange).getArgument();
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
        styler.setxAxisTickLabelsFormattingFunction(x -> Format.formatScientific(FORMAT_X_AXIS, x));
        styler.setyAxisTickLabelsFormattingFunction(y -> Format.formatScientific(FORMAT_Y_AXIS, y));
        styler.setCustomCursorYDataFormattingFunction(y -> Format.formatScientific(FORMAT_Y_CURSOR, y));
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

        void onSmoothChanged(@NotNull FTGraphPanel graph);

        void onDrawASLiveChanged(@NotNull FTGraphPanel graph);
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
        public void onRotorsFrequencyProviderChanged(@NotNull FTWinderPanel panel, @Nullable RotorFrequencyProviderI old, @Nullable RotorFrequencyProviderI _new) {
            prevData = null;          // invalidate data
            drawChart();
        }

        @Override
        public void onFlagsChanged(@NotNull FTWinderPanel panel, int oldFlags, int newFlags) {

        }
    };


    @Nullable
    private FTWinderPanel mPanel;
    @NotNull
    private volatile FTGraphMode mGraphMode;

    private volatile boolean mInvertX;
    private volatile boolean mInvertY;
    private volatile boolean mSmooth = DEFAULT_SMOOTH;
    private volatile boolean mDrawASLive = DEFAULT_DRAW_AS_LIVE;
    private final Listeners<Listener> listeners = new Listeners<>();

    @Nullable
    private volatile FTGraphData prevData;
    @Nullable
    private volatile Canceller mGraphDataLoader;

    private final BaseAction invertXAction;
    private final BaseAction invertYAction;
    private final BaseAction smoothAction;
    private final BaseAction drawAsLiveAction;

    private final JMenu graphModeMenu;
    private final ButtonGroup graphModeGroup;
    private final EnumMap<FTGraphMode, ButtonModel> graphModeButtons;

    public FTGraphPanel(@Nullable FTWinderPanel panel) {
        this(panel, null);
    }
    
    public FTGraphPanel(@Nullable FTWinderPanel panel, @Nullable FTGraphMode graphMode) {
        super(createChart());
        setExportsDir(R.DIR_EXPORTS);

        if (graphMode == null) {
            graphMode = DEFAULT_GRAPH_MODE;
        }
        
        mPanel = panel;
        mGraphMode = graphMode;
        
        // Styling
        chart.setTitle(getChartTitle(true));        // initially
        chart.getStyler().setCustomCursorXDataFormattingFunction(x -> mGraphMode.xAxisTitle + ": " + Format.formatScientific(FORMAT_X_CURSOR, x));      // depends upon graph mode

        // Actions
        invertXAction = new InvertXAction();
        invertYAction = new InvertYAction();
        smoothAction = new SmoothAction();
        drawAsLiveAction = new DrawAsLiveAction();

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
            menu.add(new JCheckBoxMenuItem(drawAsLiveAction));
            menu.add(new JCheckBoxMenuItem(smoothAction));
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

        if (draw) {
            drawChart();
        }
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

    public double getDomainRange() {
        final RotorStateManager manager = getRotorStateManager();
        return manager != null? manager.getFunction().getDomainRange(): 1;          // default
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



    public boolean isSmooth() {
        return mSmooth;
    }

    protected void onSmoothChanged() {
        final boolean smooth = mSmooth;
        setSmoothInternal(smooth);

        smoothAction.setSelected(smooth);
        listeners.dispatchOnMainThread(l -> l.onSmoothChanged(FTGraphPanel.this));
    }

    public void setSmooth(boolean smooth) {
        final boolean old = mSmooth;
        if (old == smooth)
            return;

        mSmooth = smooth;
        onSmoothChanged();
    }

    public boolean toggleSmooth() {
        final boolean newState = !mSmooth;
        setSmooth(newState);
        return newState;
    }




    public boolean isDrawingAsLive() {
        return mDrawASLive;
    }

    protected void onDrawAsLiveChanged() {
        final boolean live = mDrawASLive;
        setDrawAsLiveInternal(live);

        drawAsLiveAction.setSelected(live);
        listeners.dispatchOnMainThread(l -> l.onDrawASLiveChanged(FTGraphPanel.this));
    }

    public void setDrawAsLive(boolean drawAsLive) {
        final boolean old = mDrawASLive;
        if (old == drawAsLive)
            return;

        mDrawASLive = drawAsLive;
        onDrawAsLiveChanged();
    }

    public boolean toggleDrawAsLive() {
        final boolean newState = !mDrawASLive;
        setDrawAsLive(newState);
        return newState;
    }

    @NotNull
    private FTGraphData createGraphData(@NotNull FTGraphMode graphMode, int rotorCount, int currentRotorIndex) {
        final RotorStateManager manager = getRotorStateManager();
        final MapperInfo[] mappers;
        final boolean invertX = mInvertX;
        final boolean invertY = mInvertY;

        if (manager == null || manager.isNoOp() || rotorCount < 1 || (mappers = getMappers(graphMode, getDomainRange(), invertY)) == null || mappers.length == 0) {
            return FTGraphData.empty(graphMode);
        }
        
        final ToDoubleFunction<RotorState> domainMapper = getDomainMapper(invertX);

        if (currentRotorIndex < 0 || currentRotorIndex >= rotorCount) {
            currentRotorIndex = -1;
        }

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

        if (currentRotorIndex != -1) {
            final RotorState state = manager.getRotorState(currentRotorIndex);
            curDomain = new double[] { domainMapper.applyAsDouble(state) };
            curRanges = new double[mappers.length][1];
            
            for (int i=0; i < mappers.length; i++) {
                curRanges[i][0] = mappers[i].applyAsDouble(state);
            }
        } else {
            curDomain = null;
            curRanges = null;
        }

        final LinkedList<XYSeriesData> list = new LinkedList<>();
        for (int i=0; i < mappers.length; i++) {
            final MapperInfo mapper = mappers[i];
            
            // Main Series 1 (Pre Current)
            list.add(new XYSeriesData(SeriesType.MAIN.getSeriesName(mapper.seriesName), domain, ranges[i], null, SeriesType.MAIN));

            // current series
            if (curDomain != null) {
                list.add(new XYSeriesData(SeriesType.CURRENT.getSeriesName(mapper.seriesName), curDomain, curRanges[i], null, SeriesType.CURRENT));
            }
        }
        
        return new FTGraphData(
                graphMode, 
                rotorCount, 
                currentRotorIndex, 
                invertX, 
                invertY, 
                list.toArray(new XYSeriesData[list.size()])
        );
    }

    @NotNull
    private FTGraphData createGraphDataOnSameRotorCount(@NotNull FTGraphData data, int currentRotorIndex) {
        final List<XYSeriesData> series = new ArrayList<>(List.of(data.graphSeries()));
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
                series.removeIf(s -> SeriesType.CURRENT.equals(s.tag()));
            }

            Consumer<XYSeriesData> seriesInverter = null;
            if (data.xInverted() != invertX) {
                seriesInverter = XYSeriesData::negateX;
            }

            if (data.yInverted() != invertY) {
                Consumer<XYSeriesData> c = XYSeriesData::negateY;
                seriesInverter = seriesInverter != null? seriesInverter.andThen(c): c;
            }

            if (seriesInverter != null) {
                series.forEach(seriesInverter.tpLegacy());
            }

            if (currentRotorChanged && currentRotorIndex != -1) {
                final MapperInfo[] mappers = getMappers(data.graphMode(), getDomainRange(), invertY);
                if (mappers != null && mappers.length > 0) {
                    final RotorState state = manager.getRotorState(currentRotorIndex);

                    final ToDoubleFunction<RotorState> domainMapper = getDomainMapper(invertX);
                    final double[] domain = { domainMapper.applyAsDouble(state) };

                    for (final MapperInfo mapper: mappers) {
                        series.add(new XYSeriesData(SeriesType.CURRENT.getSeriesName(mapper.seriesName), domain, new double[]{mapper.applyAsDouble(state)}, null, SeriesType.CURRENT));
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
                series.toArray(new XYSeriesData[series.size()])
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

        final boolean smooth = mSmooth;
        final IntFunction<Color> mainColorFilter = mDrawASLive? getMainSeriesLiveColorFilter(data.currentRotorIndex()): null;

        final XYSeriesData[] seriesArr = data.graphSeries();
        int currentSeries = 0;
        int legendSeries = 0;

        for (XYSeriesData series: seriesArr) {
//            final boolean hasSeries = chart.getSeriesMap().containsKey(graphSeries.name());
//            final XYSeries xySeries;
//            if (hasSeries) {
//                xySeries = chart.updateXYSeries(graphSeries.name(), graphSeries.xData(), graphSeries.yData(), null);
//            } else {
//                xySeries = chart.addSeries(graphSeries.name(), graphSeries.xData(), graphSeries.yData(), null);
//            }

            final XYSeries xySeries = addXYSeries(series);
            xySeries.setTag(series.tag());
            xySeries.setSmooth(smooth);

            if (series.tag() instanceof SeriesType type) {
                xySeries.setShowInLegend(type.showInLegend);
                if (type.showInLegend) {
                    legendSeries++;
                }

                if (type == SeriesType.CURRENT) {
                    currentSeries++;
                }

                if (type.marker != null) {
                    xySeries.setMarker(type.marker);
                }

                if (type.lineColor != null) {
                    xySeries.setLineColor(type.lineColor);
                }

                if (type.markerColor != null) {
                    xySeries.setMarkerColor(type.markerColor);
                }

                if (type.renderStyle != null) {
                    xySeries.setXYSeriesRenderStyle(type.renderStyle);
                }

                if (type == SeriesType.MAIN) {
                    xySeries.setColorFilter(mainColorFilter);
                } else {
                    xySeries.setColorFilter(null);
                }
            }
        }

        chart.getStyler().setLegendVisible(legendSeries > 1);
        repaint();
    }

    private void setDrawAsLiveInternal(boolean drawASLive) {
        final FTGraphData data = prevData;
        if (data == null)
            return;

        final Map<String, XYSeries> map = chart.getSeriesMap();
        if (CollectionUtil.isEmpty(map))
            return;

        final IntFunction<Color> filter = drawASLive? getMainSeriesLiveColorFilter(data.currentRotorIndex()): null;

        for (XYSeries series: map.values()) {
            if (series.getTag() instanceof SeriesType type && SeriesType.MAIN == type) {
                series.setColorFilter(filter);
            }

//            else {
//                series.setColorFilter(null);
//            }
        }

        repaint();
    }

    private void setSmoothInternal(boolean smooth) {
        final Map<String, XYSeries> map = chart.getSeriesMap();
        if (CollectionUtil.isEmpty(map))
            return;

        map.values().forEach(s -> s.setSmooth(smooth));
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

    private class DrawAsLiveAction extends BaseAction {

        public DrawAsLiveAction() {
            setName(R.getFTDrawAsLiveText());
            setShortDescription(R.getFTDrawAsLiveShortDescription());
            setSelected(isDrawingAsLive());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            toggleDrawAsLive();
        }
    }


    private class SmoothAction extends BaseAction {

        public SmoothAction() {
            setName(R.getDrawSmoothCurveText());
            setShortDescription(R.getDrawSmoothCurveShortDescription());
            setSelected(isSmooth());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            toggleSmooth();
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
