package test.pkg;

import function.definition.ComplexDomainFunctionI;
import function.definition.DiscreteSignal;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import rotor.frequency.RotorFrequencyProviderI;
import util.main.ComplexUtil;

public class DiscreteSignalTest extends DiscreteSignal {

    private static final double duration = 10;
    private static final int sampleCount = 500;

    private static final double sampleDomainStart = 0;
    private static final double sampleDomainStep = duration / (sampleCount - 1);

    private static final double freq1 = 1;
    private static final double phase1 = 0;

    private static final double freq2 = 3;
    private static final double phase2 = 0;

    private static double @NotNull[] createSamples() {
        final double[] samples = new double[sampleCount];

        for (int i=0; i < samples.length; i++) {
            double time = sampleDomainStart + (i * sampleDomainStep);
            double baseAngle = ComplexUtil.TWo_PI * time;

            double val = Math.sin((baseAngle * freq1) + phase1) + Math.sin((baseAngle * freq2) + phase2);
            samples[i] = val;
        }

        return samples;
    }

    public DiscreteSignalTest() {
        super(sampleDomainStart, sampleDomainStep, createSamples());
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
