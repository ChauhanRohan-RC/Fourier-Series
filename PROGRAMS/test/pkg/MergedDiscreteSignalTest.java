package test.pkg;

import function.ComplexDomainFunctionWrapper;
import function.definition.ComplexDomainFunctionI;
import function.definition.DiscreteFunction;
import function.internal.basic.MergedFunction;
import function.internal.basic.SineSignal;
import org.jetbrains.annotations.NotNull;
import util.main.ComplexUtil;

public class MergedDiscreteSignalTest extends ComplexDomainFunctionWrapper {

    @NotNull
    private static ComplexDomainFunctionI createFunction() {
        final int sampleCount = 500;

        final ComplexDomainFunctionI[] functions = {
                new SineSignal(1, 10, 0),
                new SineSignal(4, 12, ComplexUtil.HALF_PI)
        };

        final ComplexDomainFunctionI[] discreteFunctions = new ComplexDomainFunctionI[functions.length];

        for (int i=0; i < functions.length; i++) {
            discreteFunctions[i] = DiscreteFunction.from(functions[i], sampleCount);
        }

        return new MergedFunction(discreteFunctions);
    }


    public MergedDiscreteSignalTest() {
        super(createFunction());
    }
}
