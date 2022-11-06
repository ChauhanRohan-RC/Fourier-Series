package function.internal.basic;

import function.definition.ComplexDomainFunctionI;
import models.ComplexSum;
import models.graph.FunctionGraphMode;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rotor.frequency.RotorFrequencyProviderI;

import java.util.Arrays;
import java.util.Collection;


public class MergedFunction implements ComplexDomainFunctionI {

    @NotNull
    private final Collection<ComplexDomainFunctionI> functions;
    private final double domainStart;
    private final double domainEnd;

    public MergedFunction(@NotNull Collection<ComplexDomainFunctionI> functions) {
        this.functions = functions;

        double start = Double.MIN_VALUE;
        double end = Double.MAX_VALUE;
        for (ComplexDomainFunctionI func: functions) {
            start = Math.max(start, func.getDomainStart());
            end = Math.min(end, func.getDomainEnd());
        }

        domainStart = start;
        domainEnd = end;
    }

    public MergedFunction(@NotNull ComplexDomainFunctionI... functions) {
        this(Arrays.asList(functions));
    }

    @Override
    public @NotNull Complex compute(double input) {
        final ComplexSum sum = new ComplexSum();

        for (ComplexDomainFunctionI func: functions) {
            sum.add(func.compute(input));
        }

        return sum.toComplex();
    }

    @Override
    public double getDomainStart() {
        return domainStart;
    }

    @Override
    public double getDomainEnd() {
        return domainEnd;
    }

    @Override
    public @Nullable FunctionGraphMode getDefaultGraphMode() {
        if (functions.isEmpty()) {
            return ComplexDomainFunctionI.super.getDefaultGraphMode();
        }

        return functions.iterator().next().getDefaultGraphMode();
    }

    @Override
    public @Nullable RotorFrequencyProviderI getFunctionDefaultFrequencyProvider() {
        if (functions.isEmpty()) {
            return ComplexDomainFunctionI.super.getFunctionDefaultFrequencyProvider();
        }

        return functions.iterator().next().getFunctionDefaultFrequencyProvider();
    }
}
