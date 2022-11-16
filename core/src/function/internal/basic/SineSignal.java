package function.internal.basic;

import function.definition.SignalFunctionI;
import org.apache.commons.math3.util.FastMath;
import org.jetbrains.annotations.Nullable;
import rotor.frequency.FixedStartFrequencyProvider;
import rotor.frequency.RotorFrequencyProviderI;
import util.main.ComplexUtil;

public class SineSignal implements SignalFunctionI {

    /**
     * Temporal Frequency of this Sine Signal
     * */
    private final double frequency;

    /**
     * Temporal duration of this signal<br>
     * Defines how long the signal persists
     * */
    private final double duration;

    /**
     * Phase of this sine wave<br>
     * This value is added to the input angle<br>
     * <pre>
     *     {@link ComplexUtil#HALF_PI half pi} -> cos wave
     *     {@link ComplexUtil#PI pi} -> inverted sin wave
     * </pre>
     * */
    private final double phaseRad;

    /* Transformations */
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

    protected double transformOutput(double time, double output) {
        return (output + resultAddant) * resultMultiplier;
    }

    @Override
    public final double getSignalIntensity(double time) {
        final double val = FastMath.sin((ComplexUtil.TWo_PI * frequency * time) + phaseRad);

        return transformOutput(time, val);
    }

    @Override
    public @Nullable RotorFrequencyProviderI getFunctionDefaultFrequencyProvider() {
        return new FixedStartFrequencyProvider(frequency - 1, 0.05);
    }
}
