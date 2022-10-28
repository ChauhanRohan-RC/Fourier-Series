package test.pkg;

import function.definition.ComplexDomainFunctionI;
import function.definition.DiscreteSignal;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import rotor.frequency.RotorFrequencyProviderI;
import util.main.ComplexUtil;

public class ExtTest extends DiscreteSignal {

    private int multiplier = 12;

    private static double @NotNull[] createSamples(int length) {
        final double[] samples = new double[length];
        for (int i=0; i<samples.length; i++) {
            samples[i] = Math.sin(i * ComplexUtil.TWo_PI * 0.01);
        }

        return samples;
    }

    public ExtTest() {
        super(0, 9999, createSamples(10000));
    }

    @Override
    public @Nullable RotorFrequencyProviderI getFunctionDefaultFrequencyProvider() {
        return ComplexDomainFunctionI.getDefaultFrequencyProvider(getDomainRange());
    }

    @Override
    public long getDomainAnimationDurationMsMin() {
        return 10000;
    }

    @Override
    public long getDomainAnimationDurationMsDefault() {
        return 50000;
    }

    @Override
    public long getDomainAnimationDurationMsMax() {
        return 100000;
    }
}
