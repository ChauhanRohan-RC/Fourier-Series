package function.definition;

import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;


/**
 * An implementation of {@link DiscreteFunctionI DiscreteFunction}
 *
 * {@inheritDoc}
 * @see DiscreteSignal DiscreteSignal
 * */
public class DiscreteFunction implements DiscreteFunctionI {

    /**
     * Creates a {@link DiscreteFunction Discrete Function} from a {@link ComplexDomainFunctionI Complex Domain Function}<br>
     * It effectively samples the given function over its domain
     * <br>
     * @param function the function to sample
     * @param sampleCount how many data points should be sampled
     *
     * @return the discrete function backed by samples from the given function
     *
     * @see DomainProviderI#getSampleDomainStep(int) Sample Domain Step
     * @see ComplexDomainFunctionI#createSamplesRange(int)  Create Samples Range
     * */
    @NotNull
    public static DiscreteFunction from(@NotNull ComplexDomainFunctionI function, int sampleCount) {
        return new DiscreteFunction(
                function.getDomainStart(),
                function.getSampleDomainStep(sampleCount),
                function.createSamplesRange(sampleCount)
        );
    }

    private final double domainStart;
    private final double domainStep;
    @NotNull
    private final Complex @NotNull[] samples;

    @NotNull
    private StickMode stickMode = DEFAULT_STICK_MODE;

    public DiscreteFunction(double samplesDomainStart, double samplesDomainStep, @NotNull Complex @NotNull[] samples) {
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
    public @NotNull Complex getSampleAt(int index) {
        return samples[index];
    }

    @Override
    public @NotNull StickMode getStickMode() {
        return stickMode;
    }

    @NotNull
    public DiscreteFunction setStickMode(@NotNull StickMode stickMode) {
        this.stickMode = stickMode;
        return this;
    }
}
