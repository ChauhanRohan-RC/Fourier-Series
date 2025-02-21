package demo;

import function.definition.ComplexDomainFunctionI;
import function.definition.MergedFunction;
import function.internal.basic.SineSignal;
import misc.MathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rotor.frequency.BoundedFrequencyProvider;
import rotor.frequency.RotorFrequencyProviderI;

public class Sine extends MergedFunction {

    private static ComplexDomainFunctionI @NotNull [] createFunctions() {

        return new ComplexDomainFunctionI[]{
                new SineSignal(2, 15, 0, 1, 1)
                //,
                // new SineSignal(4, 10, MathUtil.HALF_PI),
                // new SineSignal(7, 20, MathUtil.PI),
        };
    }

    public Sine() {
        super(MergeMode.UNION /* Merge Mode */,
                createFunctions()  /* Functions */
        );
    }

    public @Nullable RotorFrequencyProviderI getFunctionDefaultFrequencyProvider() {
        return new BoundedFrequencyProvider(0d, 10d);
    }

    public long getDomainAnimationDurationMsMin() {
        return 10000L;
    }

    public long getDomainAnimationDurationMsDefault() {
        return 50000L;
    }

    public long getDomainAnimationDurationMsMax() {
        return 100000L;
    }
}
