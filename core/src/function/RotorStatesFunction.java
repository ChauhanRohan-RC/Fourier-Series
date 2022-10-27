package function;

import function.definition.ComplexDomainFunctionI;
import models.ComplexSum;
import org.jetbrains.annotations.Nullable;
import rotor.RotorState;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import rotor.frequency.RotorFrequencyProviderI;

import java.util.Collection;

/**
 * A class that synthesize a function from Rotor States of a different function
 *
 * If provided, delegates to base function for every call,
 * otherwise it uses given Rotor States to synthesize a function (Inverse Fourier Series)
 * */
public class RotorStatesFunction implements ComplexDomainFunctionI {

    @Nullable
    private final ComplexDomainFunctionI function;
    @NotNull
    private final Collection<RotorState> states;

    /* ....................  Defaults  ................... */
    private final double defaultDomainStart;
    private final double defaultDomainEnd;
    private final int defaultNumericalIntegrationIntervalCount;

    private final long defaultDomainAnimMsDefault;
    private final long defaultDomainAnimMsMin;
    private final long defaultDomainAnimMsMax;

    @Nullable
    private final RotorFrequencyProviderI defaultFrequencyProvider;

    public RotorStatesFunction(@Nullable ComplexDomainFunctionI function,
                               @NotNull Collection<RotorState> states,
                               double defaultDomainStart,
                               double defaultDomainEnd,
                               int defaultNumericalIntegrationIntervalCount,
                               long defaultDomainAnimMsDefault,
                               long defaultDomainAnimMsMin,
                               long defaultDomainAnimMsMax,
                               @Nullable RotorFrequencyProviderI defaultFrequencyProvider) {

        this.function = function;
        this.states = states;
        this.defaultDomainStart = defaultDomainStart;
        this.defaultDomainEnd = defaultDomainEnd;
        this.defaultNumericalIntegrationIntervalCount = defaultNumericalIntegrationIntervalCount;
        this.defaultDomainAnimMsDefault = defaultDomainAnimMsDefault;
        this.defaultDomainAnimMsMin = defaultDomainAnimMsMin;
        this.defaultDomainAnimMsMax = defaultDomainAnimMsMax;
        this.defaultFrequencyProvider = defaultFrequencyProvider;
    }

    @Nullable
    public ComplexDomainFunctionI getBaseFunction() {
        return function;
    }

    public boolean hasBaseFunction() {
        return function != null;
    }

    @Override
    public double getDomainStart() {
        return function != null? function.getDomainStart(): defaultDomainStart;
    }

    @Override
    public double getDomainEnd() {
        return function != null? function.getDomainEnd(): defaultDomainEnd;
    }

    @Override
    public int getNumericalIntegrationIntervalCount() {
        return function != null? function.getNumericalIntegrationIntervalCount(): defaultNumericalIntegrationIntervalCount;
    }

    @Override
    public @NotNull Complex compute(double input) {
        if (function != null) {
            return function.compute(input);
        }

        final ComplexSum r = new ComplexSum(0, 0);

        for (RotorState s: states) {
            r.add(s.getTip(input));
        }

        return r.toComplex();
    }

    @Override
    @Nullable
    public RotorFrequencyProviderI getFunctionDefaultFrequencyProvider() {
        return function != null? function.getFunctionDefaultFrequencyProvider(): defaultFrequencyProvider;
    }

    @Override
    public long getDomainAnimationDurationMsDefault() {
        if (function != null) {
            return function.getDomainAnimationDurationMsDefault();
        }

        return defaultDomainAnimMsDefault > 0? defaultDomainAnimMsDefault : ComplexDomainFunctionI.super.getDomainAnimationDurationMsDefault();
    }

    @Override
    public long getDomainAnimationDurationMsMin() {
        if (function != null) {
            return function.getDomainAnimationDurationMsMin();
        }

        return defaultDomainAnimMsMin > 0? defaultDomainAnimMsMin : ComplexDomainFunctionI.super.getDomainAnimationDurationMsMin();
    }

    @Override
    public long getDomainAnimationDurationMsMax() {
        if (function != null) {
            return function.getDomainAnimationDurationMsMax();
        }

        return defaultDomainAnimMsMax > 0? defaultDomainAnimMsMax : ComplexDomainFunctionI.super.getDomainAnimationDurationMsMax();
    }
}
