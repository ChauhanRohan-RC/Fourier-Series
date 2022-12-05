package function.definition;

import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;

/**
 * A derived implementation of {@link DiscreteFunctionI} that has a real 1-Dimensional output
 *
 * @see models.RealTransform RealTransform
 * @see DiscreteFunctionI Discrete Function
 * @see SignalFunctionI Signal Function
 * */
public interface DiscreteSignalI extends DiscreteFunctionI, SignalFunctionI {

    /**
     * @param index sample index
     * @return signal intensity for a particular sample
     * */
    double getSignalIntensitySampleAt(int index);

    @Override
    default @NotNull Complex getSampleAt(int index) {
        final double intensity = getSignalIntensitySampleAt(index);

        if (isReal()) {
            return new Complex(intensity, 0);
        }

        return new Complex(0, intensity);
    }

    @Override
    @NotNull
    default Complex compute(double input) {
        return DiscreteFunctionI.super.compute(input);
    }

    @Override
    default double getSignalIntensity(double input) {
        return DiscreteFunctionI.super.compute(input).getReal();
    }

}
