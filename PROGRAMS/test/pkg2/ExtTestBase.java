package test.pkg2;

import function.definition.DiscreteSignal;
import function.definition.SignalFunctionI;
import org.jetbrains.annotations.NotNull;
import util.main.ComplexUtil;

public class ExtTestBase extends DiscreteSignal {

    public ExtTestBase(double samplesDomainStart, double samplesDomainStep, double @NotNull [] samples) {
        super(samplesDomainStart, samplesDomainStep, samples);
    }
}
