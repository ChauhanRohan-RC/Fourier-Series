package function.internal.basic;

import function.definition.AbstractSignal;
import misc.MathUtil;
import org.jetbrains.annotations.Nullable;
import rotor.frequency.BoundedFrequencyProvider;
import rotor.frequency.FixedStartFrequencyProvider;
import rotor.frequency.RotorFrequencyProviderI;

public class SineSignal extends AbstractSignal {

    public static final boolean DEFAULT_EXACT = false;

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
     *     {@link MathUtil#HALF_PI half pi} -> cos wave
     *     {@link MathUtil#PI pi} -> inverted sin wave
     * </pre>
     * */
    private final double phaseRad;

    /* Transformations */
    private final double resultAddant;
    private final double resultMultiplier;

    private volatile boolean exact = DEFAULT_EXACT;

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

    public boolean isExact() {
        return exact;
    }

    public SineSignal setExact(boolean exact) {
        this.exact = exact;
        return this;
    }

    protected double transformOutput(double time, double output) {
        return (output + resultAddant) * resultMultiplier;
    }

    @Override
    public final double getSignalIntensity(double time) {
        final double rad = (MathUtil.TWO_PI * frequency * time) + phaseRad;
        final double val = exact? MathUtil.sinexact(rad): MathUtil.sinfast(rad);

        return transformOutput(time, val);
    }

    @Override
    public @Nullable RotorFrequencyProviderI getFunctionDefaultFrequencyProvider() {
        return new BoundedFrequencyProvider(frequency - 2, frequency + 2);
    }


    @Override
    public String toString() {
        String core = "SineSignal(" +
                "frequency=" + frequency + "Hz" +
                ", duration=" + duration + "s" +
                ", phase=" + phaseRad + "rad";

        if (resultAddant != 0) {
            core += ", addant=" + resultAddant;
        }

        if (resultMultiplier != 1) {
            core += ", multiplier=" + resultMultiplier;
        }

        return core + ")";
    }
}
