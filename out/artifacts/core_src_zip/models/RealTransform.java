package models;

import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface RealTransform {

    RealTransform REAL = Complex::getReal;
    RealTransform IMAGINARY = Complex::getImaginary;
    RealTransform MAGNITUDE = Complex::abs;
    RealTransform ARGUMENT = Complex::getArgument;

    double toReal(@NotNull Complex complex);

    @NotNull
    default RealTransform scale(double scale) {
        return c -> scale * toReal(c);
    }

    @NotNull
    default RealTransform negate() {
        return c -> -toReal(c);
    }


    static double @NotNull[] apply(@NotNull Complex @NotNull[] complexes, @NotNull RealTransform realTransform, boolean negate) {
        if (negate) {
            realTransform = realTransform.negate();
        }

        final double[] realValues = new double[complexes.length];

        for (int i=0; i < complexes.length; i++) {
            realValues[i] = realTransform.toReal(complexes[i]);
        }

        return realValues;
    }

    static double @NotNull[] apply(@NotNull Complex @NotNull[] complexes, @NotNull RealTransform realTransform) {
        return apply(complexes, realTransform, false);
    }


    static double @NotNull[] toReal(@NotNull Complex @NotNull[] complexes) {
        return apply(complexes, REAL);
    }

    static double @NotNull[] toImaginary(@NotNull Complex @NotNull[] complexes) {
        return apply(complexes, IMAGINARY);
    }

    static double @NotNull[] toMagnitude(@NotNull Complex @NotNull[] complexes) {
        return apply(complexes, MAGNITUDE);
    }

    static double @NotNull[] toArgument(@NotNull Complex @NotNull[] complexes) {
        return apply(complexes, ARGUMENT);
    }
}
