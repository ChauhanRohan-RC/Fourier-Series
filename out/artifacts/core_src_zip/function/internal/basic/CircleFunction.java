package function.internal.basic;

import function.definition.ComplexDomainFunctionI;
import misc.MathUtil;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import util.main.ComplexUtil;

public class CircleFunction implements ComplexDomainFunctionI {

    @Override
    public double getDomainStart() {
        return 0;
    }

    @Override
    public double getDomainEnd() {
        return MathUtil.TWO_PI;
    }

    @Override
    public @NotNull Complex compute(double input) {
        return ComplexUtil.polarUnitFast(input);
    }
}
