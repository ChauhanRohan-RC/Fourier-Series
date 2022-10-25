package function.definition;

import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;


public class DiscreteFunction implements DiscreteFunctionI {

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
