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
 * A class that synthesize a function from Rotor States
 * */
public class RotorStatesFunction implements ComplexDomainFunctionI {

    @NotNull
    private final Collection<RotorState> states;
    private final double domainStart;
    private final double domainEnd;
    private final int numericalIntegrationIntervalCount;

    private final long domainAnimMsDefault,
            domainAnimMsMin,
            domainAnimMsMax;

    @Nullable
    private final RotorFrequencyProviderI defaultFrequencyProvider;

    public RotorStatesFunction(double domainStart,
                               double domainEnd,
                               int numericalIntegrationIntervalCount,
                               long domainAnimMsDefault,
                               long domainAnimMsMin,
                               long domainAnimMsMax,
                               @Nullable RotorFrequencyProviderI defaultFrequencyProvider,
                               @NotNull Collection<RotorState> states) {

        this.domainStart = domainStart;
        this.domainEnd = domainEnd;
        this.numericalIntegrationIntervalCount = numericalIntegrationIntervalCount;
        this.domainAnimMsDefault = domainAnimMsDefault;
        this.domainAnimMsMin = domainAnimMsMin;
        this.domainAnimMsMax = domainAnimMsMax;
        this.defaultFrequencyProvider = defaultFrequencyProvider;
        this.states = states;
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
    public int getNumericalIntegrationIntervalCount() {
        return numericalIntegrationIntervalCount;
    }

    @Override
    public @NotNull Complex compute(double input) {
        final ComplexSum r = new ComplexSum(0, 0);

        for (RotorState s: states) {
            r.add(s.getTip(input));
        }

        return r.toComplex();
    }

    @Override
    public @Nullable RotorFrequencyProviderI getFunctionDefaultFrequencyProvider() {
        return defaultFrequencyProvider;
    }

    @Override
    public long getDomainAnimationDurationMsDefault() {
        return domainAnimMsDefault > 0? domainAnimMsDefault : ComplexDomainFunctionI.super.getDomainAnimationDurationMsDefault();
    }

    @Override
    public long getDomainAnimationDurationMsMin() {
        return domainAnimMsMin > 0? domainAnimMsMin : ComplexDomainFunctionI.super.getDomainAnimationDurationMsMin();
    }

    @Override
    public long getDomainAnimationDurationMsMax() {
        return domainAnimMsMax > 0? domainAnimMsMax : ComplexDomainFunctionI.super.getDomainAnimationDurationMsMax();
    }
}
