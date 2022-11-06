package function.internal.basic;

import function.definition.SignalFunctionI;
import org.apache.commons.math3.util.FastMath;
import org.jetbrains.annotations.Nullable;
import rotor.frequency.FixedStartFrequencyProvider;
import rotor.frequency.RotorFrequencyProviderI;
import util.main.ComplexUtil;

public class SineSignal implements SignalFunctionI {

    private final double frequency;     // temporal (not angular) frequency
    private final double duration;

    private final double phaseRad;
    private final double resultAddant;
    private final double resultMultiplier;

    public SineSignal(double frequency,
                      double duration,
                      double phaseRad,
                      double resultAddant,
                      double resultMultiplier) {
        this.frequency = frequency;
        this.duration = duration;
        this.phaseRad = phaseRad;
        this.resultAddant = resultAddant;
        this.resultMultiplier = resultMultiplier;
    }

    public SineSignal(double frequency, double duration, double phaseRad) {
        this(frequency, duration, phaseRad, 0, 1);
    }

    public SineSignal(double frequency, double duration) {
        this(frequency, duration, 0);
    }

    @Override
    public double getDomainStart() {
        return 0;
    }

    @Override
    public double getDomainEnd() {
        return duration;
    }

    @Override
    public double getSignalIntensity(double input) {
        return (FastMath.sin((ComplexUtil.TWo_PI * frequency * input) + phaseRad) + resultAddant) * resultMultiplier;
    }

    @Override
    public @Nullable RotorFrequencyProviderI getFunctionDefaultFrequencyProvider() {
        return new FixedStartFrequencyProvider(frequency - 2, 0.05);
    }
}
