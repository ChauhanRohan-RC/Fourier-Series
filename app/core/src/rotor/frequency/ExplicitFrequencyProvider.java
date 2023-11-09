package rotor.frequency;

import misc.CollectionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public final class ExplicitFrequencyProvider implements RotorFrequencyProviderI {

    public enum ExtrapolateMode {
        /**
         * Restarts frequencies from the beginning
         * */
        REPEAT,

        /**
         * Creates a infinitely repeating cycle of frequencies
         * */
        CYCLE,

        /**
         * USed when frequencies other than explicit are not supported.
         * This will create a unique frequency token for all other frequencies, to avoid overwriting of explicit frequencies
         * */
        UNIQUE_CONSTANT
    }


    @NotNull
    public static final ExtrapolateMode DEFAULT_EXTRAPOLATE_MODE = ExtrapolateMode.UNIQUE_CONSTANT;

    private final double @NotNull[] frequencies;
    @NotNull
    private ExtrapolateMode mExtrapolateMode = DEFAULT_EXTRAPOLATE_MODE;

    @Nullable
    private Double mUniqueFreq;

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
        if (index >= frequencies.length) {
            switch (mExtrapolateMode) {
                case REPEAT -> index %= frequencies.length;

                case CYCLE -> {
                    final int d = index / (frequencies.length - 1);
                    final int r = index % (frequencies.length - 1);

                    if (d % 2 == 0) {
                        index = r;
                    } else {
                        index = frequencies.length - r - 1;
                    }
                }

                case UNIQUE_CONSTANT -> {
                    return getUniqueFrequencyTag();
                }

                default -> throw new IllegalStateException("Unknown ExtrapolateMode in ExplicitFrequencyProvider: " + mExtrapolateMode);
            }
        }

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

    @NotNull
    public ExtrapolateMode getExtrapolateMode() {
        return mExtrapolateMode;
    }

    @NotNull
    public ExplicitFrequencyProvider setExtrapolateMode(@NotNull ExtrapolateMode extrapolateMode) {
        mExtrapolateMode = extrapolateMode;
        return this;
    }

    public double getUniqueFrequencyTag() {
        if (mUniqueFreq == null) {
            mUniqueFreq = CollectionUtil.generateUniqueDouble(DoubleStream.of(frequencies).boxed().collect(Collectors.toSet()));
        }

        return mUniqueFreq;
    }
}
