package function.definition;

import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;

public interface ComplexFunctionI {

    @NotNull
    Complex compute(double input);

}
