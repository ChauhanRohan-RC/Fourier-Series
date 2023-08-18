package rotor.frequency;

public final class FixedStartFrequencyProvider implements RotorFrequencyProviderI {

    public static final double DEFAULT_FREQUENCY_START = 0;
    public static final double DEFAULT_FREQUENCY_STEP = 1;

    private double startFrequency = DEFAULT_FREQUENCY_START;
    private double frequencyStep = DEFAULT_FREQUENCY_STEP;

    public FixedStartFrequencyProvider(double startFrequency, double frequencyStep) {
        this.startFrequency = startFrequency;
        this.frequencyStep = frequencyStep;
    }

    public FixedStartFrequencyProvider() {
    }

    public double getStartFrequency() {
        return startFrequency;
    }

    public FixedStartFrequencyProvider setStartFrequency(double startFrequency) {
        this.startFrequency = startFrequency;
        return this;
    }

    public double getFrequencyStep() {
        return frequencyStep;
    }

    public FixedStartFrequencyProvider setFrequencyStep(double frequencyStep) {
        this.frequencyStep = frequencyStep;
        return this;
    }

    @Override
    public double getRotorFrequency(int index, int count) {
        return startFrequency + (frequencyStep * index);
    }

    @Override
    public String toString() {
        return "FixedStartFrequencyProvider{" +
                "startFrequency=" + startFrequency +
                ", frequencyStep=" + frequencyStep +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;

        FixedStartFrequencyProvider that = (FixedStartFrequencyProvider) o;
        return that.startFrequency == startFrequency && that.frequencyStep == frequencyStep;
    }

    @Override
    public int hashCode() {
        return 31 * Double.hashCode(startFrequency) + Double.hashCode(frequencyStep);
    }
}
