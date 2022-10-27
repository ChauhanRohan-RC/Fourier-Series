package rotor.frequency;

import org.jetbrains.annotations.Nullable;

public class BoundedFrequencyProvider implements RotorFrequencyProviderI {

    public static final double DEFAULT_START = 0;
    @Nullable
    public static final Double DEFAULT_END = null;

    private double frequencyStart = DEFAULT_START;
    @Nullable
    private Double frequencyEnd = DEFAULT_END;        // defaults to freq_start + rotor count

    public BoundedFrequencyProvider(double frequencyStart, @Nullable Double frequencyEnd) {
        this.frequencyStart = frequencyStart;
        this.frequencyEnd = frequencyEnd;
    }

    public BoundedFrequencyProvider() {
    }

    public double getFrequencyStart() {
        return frequencyStart;
    }

    public BoundedFrequencyProvider setFrequencyStart(double frequencyStart) {
        this.frequencyStart = frequencyStart;
        return this;
    }

    @Nullable
    public Double getFrequencyEnd() {
        return frequencyEnd;
    }

    public double getFrequencyEnd(int count) {
        if (frequencyEnd != null) {
            return frequencyEnd;
        }

        return frequencyStart + count;
    }

    public BoundedFrequencyProvider setFrequencyEnd(@Nullable Double frequencyEnd) {
        this.frequencyEnd = frequencyEnd;
        return this;
    }

    @Override
    public double getRotorFrequency(int index, int count) {
        final double start = getFrequencyStart();
        final double end = getFrequencyEnd(count);
        return start + (index * ((end - start) / count));
    }

    @Override
    public String toString() {
        return "BoundedFrequencyProvider{" +
                "frequencyStart=" + frequencyStart +
                ", frequencyEnd=" + frequencyEnd +
                '}';
    }
}
