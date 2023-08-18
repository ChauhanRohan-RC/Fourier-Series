package function.definition;

import misc.MathUtil;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import util.main.ComplexUtil;

/**
 * A discrete function is non-continuous function, that can only map predefined set of domain points to sample values
 * <br>
 * Think of a discrete function as a rigid mapping, that is backed by a set of samples.
 * Samples are collected at fixed domain intervals (sample rate), which controls how narrow or wide a discrete function is defined
 * <br>
 * This implementation can handle domain points outside defined samples, using {@link StickMode}
 *
 * @see ComplexDomainFunctionI
 * @see DiscreteSignalI
 * */
public interface DiscreteFunctionI extends ComplexDomainFunctionI {

    /**
     * Defines how a discrete function should handle a input
     * */
    enum StickMode {
        /**
         * Return a sample which is closest to the given input
         * */
        STICK_NEAREST,

        /**
         * interpolates the given input between the nearest samples
         * */
        INTERPOLATE
    }

    @NotNull
    StickMode DEFAULT_STICK_MODE = StickMode.STICK_NEAREST;



    /**
     * @return domain point of first sample
     * */
    @Override
    double getDomainStart();

    /**
     * @return domain gap between samples
     * */
    double getDomainStep();

    /**
     * @return total sample count
     * */
    int getSampleCount();

    /**
     * @param index sample index
     * @return domain point for a particular sample
     * */
    default double getDomainAt(int index) {
        return getDomainStart() + (index * getDomainStep());
    }

    /**
     * @return domain point for the last sample
     * */
    @Override
    default double getDomainEnd() {
        return getDomainAt(getSampleCount() - 1);
    }

    @Override
    default int getNumericalIntegrationIntervalCount() {
        return getSampleCount() - 1;
    }

    /**
     * @param index sample index
     * @return value of the sample at given index
     * */
    @NotNull
    Complex getSampleAt(int index);

    /**
     * @return the stick mode
     *
     * @see StickMode
     * */
    @NotNull
    StickMode getStickMode();


    @Override
    @NotNull
    default Complex compute(double input) {
        final int i = (int) ((input - getDomainStart()) / getDomainStep());
        final int count = getSampleCount();
        if (i + 1 >= count) {
            return getSampleAt(count - 1);      // last sample
        }

        final float norm = MathUtil.normF(getDomainAt(i), getDomainAt(i + 1), input);

        switch (getStickMode()) {
            case INTERPOLATE: {
                return ComplexUtil.lerp(getSampleAt(i), getSampleAt(i + 1), norm);
            }
            case STICK_NEAREST:
            default: {
                return getSampleAt(norm < 0.5f? i: i + 1);
            }
        }
    }
}
