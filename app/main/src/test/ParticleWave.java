package test;

import function.definition.AbstractSignal;
import misc.MathUtil;

public class ParticleWave extends AbstractSignal {

    private static final boolean DEFAULT_CENTER = false;
    private static final double DEFAULT_DAMPING_FACTOR = 0;

    private final double spaceRange;
    private final double frequency;
    private double dampingFactor;
    private boolean centering = DEFAULT_CENTER;
    private double multiplier = 1;

    public ParticleWave(double spaceRange, double frequency, double dampingFactor) {
        this.spaceRange = spaceRange;
        this.frequency = frequency;
        this.dampingFactor = dampingFactor;
    }

    public ParticleWave(double spaceRange, double frequency) {
        this(spaceRange, frequency, DEFAULT_DAMPING_FACTOR);
    }

    public boolean isCentering() {
        return centering;
    }

    public ParticleWave setCentering(boolean centering) {
        this.centering = centering;
        return this;
    }

    public double getDampingFactor() {
        return dampingFactor;
    }

    public ParticleWave setDampingFactor(double dampingFactor) {
        this.dampingFactor = dampingFactor;
        return this;
    }

    public ParticleWave noDamping() {
        return setDampingFactor(0);
    }

    public ParticleWave setMultiplier(double multiplier) {
        this.multiplier = multiplier;
        return this;
    }

    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public double getDomainStart() {
        return 0;
    }

    @Override
    public double getDomainEnd() {
        return spaceRange;
    }


    @Override
    public double getSignalIntensity(double input) {
        if (Math.abs(input) > spaceRange) {
            input %= spaceRange;
        }

        final double dampFactor = Math.exp(-dampingFactor *  (centering? Math.abs((spaceRange / 2) - input): input));
        return Math.sin(MathUtil.TWO_PI * frequency * input) * dampFactor * multiplier;

//        return Math.sin(MathUtil.TWO_PI * frequency * input) * Math.exp((centering && input > spaceRange / 2? spaceRange - input: input) * dampingFactor);
    }
}
