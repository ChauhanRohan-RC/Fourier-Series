package function.definition;

import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;

public interface ComplexFunctionI {

    ComplexFunctionI ZERO = in -> Complex.ZERO;



    @NotNull
    Complex compute(double input);

}
