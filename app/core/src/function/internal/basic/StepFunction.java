package function.internal.basic;

import function.definition.AbstractSignal;
import function.definition.ComplexDomainFunctionI;
import function.definition.SignalFunctionI;
import org.jetbrains.annotations.Nullable;
import rotor.frequency.RotorFrequencyProviderI;

public class StepFunction extends AbstractSignal {

    private final double duration;
    private final double resultAddant;
    private final double resultMultiplier;

    public StepFunction(double duration, double resultAddant, double resultMultiplier) {
        this.duration = duration;
        this.resultAddant = resultAddant;
        this.resultMultiplier = resultMultiplier;
    }

    public StepFunction(double duration) {
        this(duration, 0, 1);
    }


    @Override
    public double getDomainStart() {
        return 0;
    }

    @Override
    public double getDomainEnd() {
        return duration;
    }

//    @Override
//    public @Nullable Color getColor(double input) {
//        return Color.getHSBColor((float) (input % 1), 1, 1);
//    }

    @Override
    public double getSignalIntensity(double input) {
        if (input > 1) {
            input %= 1;
        }

        return ((input > 0.5? 1: -1) + resultAddant) * resultMultiplier;
    }

    @Override
    @Nullable
    public RotorFrequencyProviderI getFunctionDefaultFrequencyProvider() {
        return ComplexDomainFunctionI.getDefaultFrequencyProvider(getDomainRange());
    }

    @Override
    public long getDomainAnimationDurationMsDefault() {
        return (long) (2000 * duration);
    }

    @Override
    public long getDomainAnimationDurationMsMin() {
        return (long) (500 * duration);
    }

    @Override
    public long getDomainAnimationDurationMsMax() {
        return (long) (10000 * duration);
    }

}
