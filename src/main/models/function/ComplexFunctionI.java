package main.models.function;

import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;

public interface ComplexFunctionI {

    @NotNull
    Complex compute(double input);

}
