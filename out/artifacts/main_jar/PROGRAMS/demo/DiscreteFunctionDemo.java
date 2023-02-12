package demo;

import function.definition.DiscreteFunction;
import function.definition.DomainProviderI;
import misc.MathUtil;
import models.FunctionGraphMode;
import models.RealTransform;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rotor.RotorState;
import rotor.frequency.BoundedFrequencyProvider;
import rotor.frequency.ExplicitFrequencyProvider;
import rotor.frequency.RotorFrequencyProviderI;

import java.awt.*;

public class DiscreteFunctionDemo extends DiscreteFunction {

    private static final double DOMAIN_START = 0;
    private static final double DOMAIN_END = 10;

    private static final int SAMPLE_COUNT = 500;
    private static final double SAMPLE_DOMAIN_STEP = DomainProviderI.getSampleDomainStep(DOMAIN_END - DOMAIN_START, SAMPLE_COUNT);

    private static Complex @NotNull[] createSamples() {
        final double fq1 = 3.0d;
        final double phase1 = 0;

        final double fq2 = 7.0d;
        final double phase2 = MathUtil.HALF_PI;

        final Complex[] samples = new Complex[SAMPLE_COUNT];

        for (int i=0; i < SAMPLE_COUNT; i++) {
            final double domain = DOMAIN_START + (i * SAMPLE_DOMAIN_STEP);

            final double value1 = MathUtil.sinexact((MathUtil.TWO_PI * fq1 * domain) + phase1);
            final double value2 = MathUtil.sinexact((MathUtil.TWO_PI * fq2 * domain) + phase2);
            samples[i] = new Complex(value1 + value2, i % 4);
        }

        return samples;
    }

    public DiscreteFunctionDemo() {
        super(DOMAIN_START, SAMPLE_DOMAIN_STEP, createSamples());

        setStickMode(StickMode.STICK_NEAREST);
//        setStickMode(StickMode.INTERPOLATE);
    }


    @Override
    public @Nullable RotorFrequencyProviderI getFunctionDefaultFrequencyProvider() {
        return new BoundedFrequencyProvider(0d, 10d);
    }


    @Override
    public double getDomainAt(int index) {
        return super.getDomainAt(index);
    }

    @Override
    public double getDomainEnd() {
        return super.getDomainEnd();
    }

    @Override
    public int getNumericalIntegrationIntervalCount() {
        return super.getNumericalIntegrationIntervalCount();
    }

    @Override
    public @NotNull Complex compute(double input) {
        return super.compute(input);
    }

    @Override
    public boolean isNoop() {
        return super.isNoop();
    }

    @Override
    public double @NotNull [] createSamplesDomain(int sampleCount) {
        return super.createSamplesDomain(sampleCount);
    }

    @Override
    public @NotNull Complex @NotNull [] createSamplesRange(int sampleCount) {
        return super.createSamplesRange(sampleCount);
    }

    @Override
    public @NotNull Complex @NotNull [] createSamplesRange(double @NotNull [] samplesDomain) {
        return super.createSamplesRange(samplesDomain);
    }

    @Override
    public double @NotNull [] createSamplesRealRange(int sampleCount, @NotNull RealTransform realTransform) {
        return super.createSamplesRealRange(sampleCount, realTransform);
    }

    @Override
    public double @NotNull [] createSamplesRealRange(double @NotNull [] samplesDomain, @NotNull RealTransform realTransform) {
        return super.createSamplesRealRange(samplesDomain, realTransform);
    }

    @Override
    public boolean containsCachedRotorState(double frequency) {
        return super.containsCachedRotorState(frequency);
    }

    @Override
    public @Nullable RotorState getCachedRotorState(double frequency) {
        return super.getCachedRotorState(frequency);
    }

    @Override
    public boolean isFrequencySupported(double frequency) {
        return super.isFrequencySupported(frequency);
    }



    @Override
    public @Nullable ExplicitFrequencyProvider getExplicitFrequencyProvider() {
        return super.getExplicitFrequencyProvider();
    }

    @Override
    public boolean frequenciesExceptExplicitSupported() {
        return super.frequenciesExceptExplicitSupported();
    }

    @Override
    public @Nullable FunctionGraphMode getDefaultGraphMode() {
        return super.getDefaultGraphMode();
    }

    @Override
    public double getDomainRange() {
        return super.getDomainRange();
    }

    @Override
    public boolean isWithinDomain(double input) {
        return super.isWithinDomain(input);
    }

    @Override
    public double getFundamentalFrequency() {
        return super.getFundamentalFrequency();
    }

    @Override
    public double getSampleDomainStep(int sampleCount) {
        return super.getSampleDomainStep(sampleCount);
    }

    @Override
    public @Nullable Color getColor(double input) {
        return super.getColor(input);
    }

    @Override
    public long getDomainAnimationDurationMsDefault() {
        return super.getDomainAnimationDurationMsDefault();
    }

    @Override
    public long getDomainAnimationDurationMsMin() {
        return super.getDomainAnimationDurationMsMin();
    }

    @Override
    public long getDomainAnimationDurationMsMax() {
        return super.getDomainAnimationDurationMsMax();
    }

    @Override
    public float durationMsToDomainAnimationSpeedFraction(long durationMs) {
        return super.durationMsToDomainAnimationSpeedFraction(durationMs);
    }

    @Override
    public long domainAnimationSpeedFractionToDurationMs(float fraction) {
        return super.domainAnimationSpeedFractionToDurationMs(fraction);
    }
}
