package function.definition;

import models.graph.FunctionGraphMode;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A Real output function. This is useful to define real world functions, like signals.
 * This however limits output to 1-Dimension, which comes out to be a really BORING 2D drawing in complex plane
 *
 * @see ComplexDomainFunctionI
 * */
public interface SignalFunctionI extends ComplexDomainFunctionI {

    double getSignalIntensity(double input);

    @Override
    @NotNull
    default Complex compute(double input) {
        return new Complex(getSignalIntensity(input), 0);
    }

//    @Override
//    default @Nullable RotorFrequencyProviderI getFunctionDefaultFrequencyProvider() {
//        return new FixedStartFrequencyProvider(0, 0.1);
//    }


    @Override
    default @Nullable FunctionGraphMode getDefaultGraphMode() {
        return FunctionGraphMode.INPUT_VS_OUT_REAL;
    }
}
