package function.internal.basic;

import function.definition.ComplexDomainFunctionI;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;

public class CircleFunction implements ComplexDomainFunctionI {

    @Override
    public double getDomainStart() {
        return 0;
    }

    @Override
    public double getDomainEnd() {
        return Math.PI * 2;
    }

    @Override
    public @NotNull Complex compute(double input) {
        return new Complex(Math.cos(input), Math.sin(input));
    }
}
