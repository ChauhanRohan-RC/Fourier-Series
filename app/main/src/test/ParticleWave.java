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
        if (input > spaceRange) {
            input %= spaceRange;
        }

        if (centering && input > spaceRange / 2) {
            input = spaceRange - input;
        }

        return Math.sin(MathUtil.TWO_PI * frequency * input) * Math.exp(input * dampingFactor);
    }
}
