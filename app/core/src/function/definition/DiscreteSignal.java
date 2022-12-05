package function.definition;

import models.RealTransform;
import org.jetbrains.annotations.NotNull;

/**
 * An implementation of {@link DiscreteSignalI DiscreteSignal}
 *
 * {@inheritDoc}
 *  @see DiscreteFunction DiscreteFunction
 * */
public class DiscreteSignal implements DiscreteSignalI {

    /**
     * Creates a {@link DiscreteSignal Discrete Signal} from a {@link ComplexDomainFunctionI Complex Domain Function}<br>
     * It effectively samples the given function over its domain, mapping {@link org.apache.commons.math3.complex.Complex complex} output to rela values using supplied {@link RealTransform Real Transform}
     * <br>
     * @param function the function to sample
     * @param sampleCount how many data points should be sampled
     * @param realTransform how Complex output of function should be mapped to real values
     * @return the Discrete Signal backed by samples from the given function
     *
     * @see DomainProviderI#getSampleDomainStep(int) Sample Domain Step
     * @see ComplexDomainFunctionI#createSamplesRealRange(int, RealTransform)   Create Samples Real Range
     * */
    @NotNull
    public static DiscreteSignal from(@NotNull ComplexDomainFunctionI function, int sampleCount, @NotNull RealTransform realTransform) {
        return new DiscreteSignal(
                function.getDomainStart(),
                function.getSampleDomainStep(sampleCount),
                function.createSamplesRealRange(sampleCount, realTransform)
        );
    }

    private final double domainStart;
    private final double domainStep;
    private final double @NotNull[] samples;

    private volatile boolean mReal = DEFAULT_REAL;

    @NotNull
    private StickMode stickMode = DEFAULT_STICK_MODE;

    public DiscreteSignal(double samplesDomainStart, double samplesDomainStep, double @NotNull[] samples) {
        if (samples == null || samples.length == 0) {
            throw new IllegalArgumentException("There must be at least 1 sample");
        }

        if (samplesDomainStep < 0) {
            throw new IllegalArgumentException("Samples Domain Step must be >= 0, given: " + samplesDomainStep);
        }

        this.domainStart = samplesDomainStart;
        this.domainStep = samplesDomainStep;
        this.samples = samples;
    }

    @Override
    public double getDomainStart() {
        return domainStart;
    }

    @Override
    public double getDomainStep() {
        return domainStep;
    }

    @Override
    public int getSampleCount() {
        return samples.length;
    }

    @Override
    public double getSignalIntensitySampleAt(int index) {
        return samples[index];
    }

    @Override
    public boolean isReal() {
        return mReal;
    }

    public DiscreteSignal setReal(boolean real) {
        mReal = real;
        return this;
    }

    @Override
    public @NotNull StickMode getStickMode() {
        return stickMode;
    }

    @NotNull
    public DiscreteSignal setStickMode(StickMode stickMode) {
        this.stickMode = stickMode;
        return this;
    }
}
