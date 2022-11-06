package test;

import app.R;
import function.internal.basic.MergedFunction;
import function.internal.basic.SineSignal;
import function.internal.basic.StepFunction;
import provider.FunctionMeta;
import provider.FunctionType;
import provider.Providers;
import rotor.StandardRotorStateManager;
import rotor.frequency.FixedStartFrequencyProvider;
import ui.FTGraphPanel;
import ui.FTWinderPanel;
import ui.FourierUi;
import ui.FunctionGraphPanel;
import ui.util.Ui;

import javax.swing.*;
import java.awt.*;


public class TestFrame extends JFrame {

    public static final String TAG = "TestFrame";

    public TestFrame() {
        super("test Frame");

        final StandardRotorStateManager manager = new StandardRotorStateManager(
                new MergedFunction(
                        new SineSignal(3, 40, 0, 0, 1),
                        new SineSignal(4, 40, 0, 0, 1),
                        new SineSignal(5, 40, 0, 0, 1)
                ),
                new FunctionMeta(FunctionType.INTERNAL_PROGRAM, "Test Function"),
                600
        );

//        final StandardRotorStateManager manager = new StandardRotorStateManager(
//                new StepFunction(10, 0, 1),
//                new FunctionMeta(FunctionType.INTERNAL_PROGRAM, "Test Function"),
//                600
//        );

//        final StandardRotorStateManager manager = new StandardRotorStateManager(
//                Providers.FOURIER_PORTRAIT.getFunction(),
//                Providers.FOURIER_PORTRAIT.getFunctionMeta(),
//                600
//        );

        manager.setRotorFrequencyProvider(new FixedStartFrequencyProvider(2.5, 0.005));

        final FunctionGraphPanel functionGraphPanel = new FunctionGraphPanel(manager.getFunction(), manager.getFunctionMeta());
        final FTWinderPanel ftWinderPanel = new FTWinderPanel(manager);
        final FTGraphPanel ftGraphPanel = new FTGraphPanel(ftWinderPanel, null);
        final Timer looper = Ui.createLooper(ftWinderPanel);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(Ui.windowBoundsCenterScreen(FourierUi.INITIAL_WIDTH, FourierUi.INITIAL_HEIGHT));
        setMinimumSize(FourierUi.MINIMUM_SIZE);
//        setSize(400, 700);
        setLayout(new GridBagLayout());

        final GridBagConstraints one = new GridBagConstraints();
        one.gridx = 0;
        one.gridy = 0;
        one.gridwidth = 1;
        one.gridheight = 2;
        one.weightx = one.weighty = 1;
        one.fill = GridBagConstraints.BOTH;
        add(ftWinderPanel, one);

        final GridBagConstraints two = new GridBagConstraints();
        two.gridx = 1;
        two.gridy = 0;
        two.gridwidth = 1;
        two.gridheight = 1;
        two.weightx = two.weighty = 1.25;
        two.fill = GridBagConstraints.BOTH;
        add(functionGraphPanel, two);

        final GridBagConstraints three = new GridBagConstraints();
        three.gridx = 1;
        three.gridy = 1;
        three.gridwidth = 1;
        three.gridheight = 1;
        three.weightx = three.weighty = 1.25;
        three.fill = GridBagConstraints.BOTH;
        add(ftGraphPanel, three);

        setVisible(true);

        EventQueue.invokeLater(() -> {
            functionGraphPanel.drawChart();
            looper.start();
        });
    }


    public static void main(String[] args) {
        R.init();
        final TestFrame frame = new TestFrame();
    }

}
