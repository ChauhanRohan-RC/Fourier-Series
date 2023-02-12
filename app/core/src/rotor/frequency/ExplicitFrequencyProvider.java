package rotor.frequency;

import misc.CollectionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

public final class ExplicitFrequencyProvider implements RotorFrequencyProviderI {

    private final double @NotNull[] frequencies;

    public ExplicitFrequencyProvider(boolean sort, double @NotNull... frequencies) {
        if (frequencies == null) {
            frequencies = new double[0];
        }

        if (sort && frequencies.length > 1) {
            Arrays.sort(frequencies);
        }

        this.frequencies = frequencies;
    }

    public ExplicitFrequencyProvider(boolean sort, @NotNull Collection<Double> frequencies) {
        this(sort, CollectionUtil.toDoubleArray(frequencies));
    }

    public int getFrequencyCount() {
        return frequencies.length;
    }

    public double getFrequencyAt(int index) {
        return frequencies[index];
    }


    @Override
    public double getRotorFrequency(int index, int count) {
        if (index >= frequencies.length)
            return frequencies.length > 0? frequencies[frequencies.length - 1]: 0;      // last one

        return frequencies[index];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final ExplicitFrequencyProvider that = (ExplicitFrequencyProvider) o;
        return Arrays.equals(frequencies, that.frequencies);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(frequencies);
    }

    @Override
    public String toString() {
        return "FixedFrequencyProvider{" +
                "frequencies=" + Arrays.toString(frequencies) +
                '}';
    }
}
