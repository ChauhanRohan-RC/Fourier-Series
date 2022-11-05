package test;

import function.internal.basic.MergedFunction;
import function.internal.basic.SineSignal;
import provider.FunctionMeta;
import provider.FunctionType;
import rotor.StandardRotorStateManager;
import rotor.frequency.FixedStartFrequencyProvider;
import ui.util.Ui;

import javax.swing.*;
import java.awt.*;


public class TestFrame extends JFrame {

    public TestFrame() {
        super("test Frame");

        final StandardRotorStateManager manager = new StandardRotorStateManager(
                new MergedFunction(
                        new SineSignal(3, 10, 0, 1, 0.5)
//                        new SineSignal(4, 2, 90, 0, 2),
//                        new SineSignal(3.5, 2, 45, 1.5, 0.1)
                ),
                new FunctionMeta(FunctionType.INTERNAL_PROGRAM, "Test Function"),
                600
        );

        manager.setRotorFrequencyProvider(new FixedStartFrequencyProvider(2, 0.005));

        final FourierTransformWinderPanel panel = new FourierTransformWinderPanel(manager);
        final Timer looper = Ui.createLooper(panel);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
//        setBounds(Ui.windowBoundsCenterScreen(FourierUi.INITIAL_WIDTH, FourierUi.INITIAL_HEIGHT));
        setSize(400, 700);
//        setMinimumSize(FourierUi.MINIMUM_SIZE);
        setLayout(new BorderLayout(0, 0));
        add(panel, BorderLayout.CENTER);
        setVisible(true);

        EventQueue.invokeLater(() -> {
            looper.start();
        });
    }


    public static void main(String[] args) {
        final TestFrame frame = new TestFrame();
    }

}
