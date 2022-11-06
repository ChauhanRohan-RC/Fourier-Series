package app;

import animation.animator.Animator;
import animation.animator.DoubleAnimator;
import function.definition.ComplexDomainFunctionI;
import function.internal.basic.MergedFunction;
import function.internal.basic.SineSignal;
import models.RealTransform;
import models.graph.FunctionGraphMode;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.knowm.xchart.*;
import org.knowm.xchart.style.markers.None;
import provider.FunctionMeta;
import provider.Providers;
import ui.DarkChartTheme;
import ui.FourierUi;
import ui.FunctionGraphPanel;
import ui.util.Ui;
import util.Log;
import util.async.Async;
import util.main.ComplexUtil;

import javax.swing.*;
import java.awt.*;

public class App {

    public static final String TAG = "Main";
    private static int sWindowCount;

    public static void onWindowOpen(@NotNull JFrame frame) {
        sWindowCount++;
        Log.d(TAG, "onWindowOpen: Title: " + frame.getTitle() + " | WindowCount: " + sWindowCount);
    }

    public static void onWindowClose(@NotNull JFrame frame) {
        sWindowCount--;
        Log.d(TAG, "onWindowClose: Title: " + frame.getTitle() + " | WindowCount: " + sWindowCount);
        if (sWindowCount <= 0) {
            // todo: save stuff here (settings, config etc)

            Log.d(TAG, "Quiting...");
            System.exit(0);
        }
    }

//    private static void setupFourierUi(int initialFuncProviderIndex, boolean preload) {
////        if (preload) {
////            stateManager.loadSync(stateManager.getDefaultInitialRotorCount(), null);
////        }

//        Async.postIfNotOnMainThread(() -> {
//            final FourierUi ui = new FourierUi(null, initialFuncProviderIndex);
//        });
//    }


    private void testOverlay() {
        R.init();

        final JFrame frame = new JFrame("Test Frame");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new OverlayLayout(mainPanel));

        final JPanel overlayPanel = new JPanel();
        overlayPanel.setOpaque(false);
        overlayPanel.setLayout(new BorderLayout());

//        final GridBagConstraints gbc_dummy = new GridBagConstraints();
//        gbc_dummy.gridx = gbc_dummy.gridy = 0;
//        gbc_dummy.fill = GridBagConstraints.BOTH;
//        gbc_dummy.weightx = gbc_dummy.weighty = 1;
//        overlayPanel.add(Box.createGlue(), gbc_dummy);

        mainPanel.add(overlayPanel);

        final JPanel fsPanel = new JPanel();
        fsPanel.setBackground(Color.BLACK);
        mainPanel.add(fsPanel);

        final JPanel controlPanel = new JPanel();
        final JButton testButton = new JButton("test Button");
        testButton.addActionListener(e -> controlPanel.setVisible(!controlPanel.isVisible()));
        controlPanel.add(testButton);

        frame.setLayout(new BorderLayout());
        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.add(mainPanel, BorderLayout.CENTER);

        frame.setSize(new Dimension(400, 400));
        frame.setVisible(true);


        // Overlays
        final JPanel bottomOverlay = new JPanel(new BorderLayout());
        overlayPanel.add(bottomOverlay, BorderLayout.PAGE_END);

        JPanel notificationsPanel = new JPanel();
//        final GridBagConstraints gbc = new GridBagConstraints();
//        gbc.gridx = gbc.gridy = 1;
//        gbc.fill = GridBagConstraints.NONE;
//        gbc.weightx = gbc.weighty = 0;
//        gbc.insets = new Insets(10, 10, 10, 10);
//        overlayPanel.add(notificationsPanel, gbc);

        bottomOverlay.add(notificationsPanel, BorderLayout.LINE_END);
        bottomOverlay.setOpaque(false);

        notificationsPanel.setLayout(new BoxLayout(notificationsPanel, BoxLayout.Y_AXIS));
        notificationsPanel.add(new JLabel("Notification 1"));
        notificationsPanel.add(new JLabel("Notification 2"));

        Async.uiPost(() -> {
            Log.d(TAG, "what");
//            notificationsPanel.remove(0);
            notificationsPanel.add(new JLabel("Notification 78823"));
            notificationsPanel.revalidate();
        }, 2000);
    }



    private void ftTest() {
        final XYChart chart = new XYChartBuilder().width(600).height(400).title("Sine Signal").xAxisTitle("Time").yAxisTitle("Intensity").build();
        chart.getStyler().setTheme(new DarkChartTheme());
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setyAxisTickLabelsFormattingFunction(d -> String.format("%.1f", d));

        final SwingWrapper<XYChart> swingWrapper = new SwingWrapper<>(chart);

        final ComplexDomainFunctionI signal = new MergedFunction(
                new SineSignal(5, 1, ComplexUtil.HALF_PI, 1, 1)
//                new SineSignal(3, 1)
        );

        final double[] domain = signal.createSamplesDomain(1000);

        final DoubleAnimator anim = new DoubleAnimator(0, 10);
        anim.setDurationMs(10000);
        anim.addAnimationListener(new Animator.AnimationListenerAdapter<>() {
            @Override
            public void onAnimationUpdate(@NotNull Animator<Double> animator) {
                final double windiwngFreq = animator.getCurrentValue();
                ComplexDomainFunctionI ft = ComplexUtil.fourierTransformIntegrand(signal, windiwngFreq);

                final Complex[] range = ft.createSamplesRange(domain);

                final double[] xdata = RealTransform.toReal(range);
                final double[] ydata = RealTransform.toImaginary(range);

                if (chart.getSeriesMap().containsKey("ft")) {
                    chart.updateXYSeries("ft", xdata, ydata, null);
                } else {
                    XYSeries series = chart.addSeries("ft", xdata, ydata);
                    series.setMarker(new None());
                    series.setShowInLegend(false);
                }

                // todo center of mass

                swingWrapper.getXChartPanel().repaint();
            }
        });

//        final double windiwngFreq = 5;
//        ComplexDomainFunctionI ft = ComplexUtil.fourierTransformIntegrand(signal, windiwngFreq);
//
//        final Complex[] range = ft.createSamplesRange(domain);
//
//        final double[] xdata = RealTransform.toReal(range);
//        final double[] ydata = RealTransform.toImaginary(range);
//
//        final XYSeries graphSeries = chart.addSeries("signal", xdata, ydata);
//        graphSeries.setMarker(new None());
////        graphSeries.setLineWidth(0.5f);
//        graphSeries.setShowInLegend(false);

//        final RotorState state = new RotorState(windiwngFreq, signal);
//        final XYSeries com = chart.addSeries("com", List.of(state.getCoefficient().getReal()), List.of(state.getCoefficient().getImaginary()));
//        com.setMarkerColor(Color.RED);
//        com.setMarker(new Circle());
//        com.setShowInLegend(false);
//        chart.getStyler().setPlotGridLinesVisible(false);

        // Display
        swingWrapper.displayChart();

        Ui.createLooper(anim).start();
        anim.start();
    }



    private static void testFunctionGraph() {
        final XYChart chart = new XYChartBuilder().width(600).height(400).title("Sine Signal (ext loaded)").xAxisTitle("Time").yAxisTitle("Intensity").build();

        final DarkChartTheme theme = new DarkChartTheme();
        theme.configureStyler(chart.getStyler());
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setyAxisTickLabelsFormattingFunction(d -> String.format("%.1f", d));
        chart.getStyler().setxAxisTickLabelsFormattingFunction(d -> String.format("%.1f", d));
        chart.getStyler().setAxisTitlesVisible(false);
        chart.getStyler().setChartTitleVisible(true);
        chart.getStyler().setAnnotationTextFontColor(Colors.FG_DARK);

        final SwingWrapper<XYChart> swingWrapper = new SwingWrapper<>(chart);

        final ComplexDomainFunctionI signal = new MergedFunction(
                new SineSignal(5, 5, 0),
                new SineSignal(3, 20, ComplexUtil.HALF_PI, 1, 0.5),
                new SineSignal(4, 20, 0, 0, 0.7)
        );

        final double[] domain = signal.createSamplesDomain(2000);
        final Complex[] range  = signal.createSamplesRange(domain);

        chart.addAnnotation(new AnnotationText("Loading", (float)chart.getWidth() / 2, (float)chart.getHeight() / 2, false));

//        final XYSeries graphSeries = chart.addSeries("Function", domain, RealTransform.toReal(range));
//        graphSeries.setMarker(new None());

//        final ComplexDomainFunctionI function = Providers.FOURIER_PORTRAIT.requireFunction();
////        final double[] domain = function.createSamplesDomain(10000);
//        final Complex[] range  = function.createSamplesRange(10000);        // more than enough

//        final XYSeries graphSeries = chart.addSeries("function", RealTransform.toReal(range), RealTransform.toReal(range, c -> -c.getImaginary()));
//        graphSeries.setMarker(new None());

        swingWrapper.displayChart();
    }

    // TODO: Test launcher
    private static void launchTest(String[] args) throws Providers.NoOpProviderException {
        R.init();

//        final ComplexDomainFunctionI function = new MergedFunction(
//                new SineSignal(5, 10, 0),
//                new SineSignal(3, 10, ComplexUtil.HALF_PI, 1, 0.5),
//                new SineSignal(4, 10, 0, 0, 0.7)
//        );
//
//        final FunctionMeta meta = new FunctionMeta(FunctionType.EXTERNAL_PROGRAM, "Signal");

        final ComplexDomainFunctionI function = Providers.FOURIER_PORTRAIT.requireFunction();
        final FunctionMeta meta = Providers.FOURIER_PORTRAIT.getFunctionMeta();

        final FunctionGraphPanel graph = new FunctionGraphPanel(
                function,
                meta,
                FunctionGraphMode.OUTPUT_SPACE
        );

        final JFrame frame = new JFrame("test");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(800, 800));
        frame.add(graph);
        frame.setVisible(true);

        Async.uiPost(graph::drawChart);
    }




    // TODO: Main production launcher
    private static void launchMain(String[] args) {
        R.init();
        Async.postIfNotOnMainThread(() -> {
            final FourierUi ui = new FourierUi(null, -1);
            ui.setFunctionProvider(Providers.NoopProvider.getSingleton());          // start with None
        });
    }


    public static void main(String[] args) throws Providers.NoOpProviderException {
//        launchTest(args);
        launchMain(args);
    }

}
