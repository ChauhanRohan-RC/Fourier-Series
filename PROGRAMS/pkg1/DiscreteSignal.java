package pkg1;

import org.jetbrains.annotations.NotNull;

public class DiscreteSignal extends function.definition.DiscreteSignal {

    public DiscreteSignal(double samplesDomainStart, double samplesDomainStep, double @NotNull [] samples) {
        super(samplesDomainStart, samplesDomainStep, samples);
    }
}

