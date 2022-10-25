package function;

import function.definition.ComplexDomainFunctionI;
import models.ComplexSum;
import rotor.RotorState;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A class that synthesize a function from Rotor States
 * */
public class RotorStatesFunction implements ComplexDomainFunctionI {

    @NotNull
    private final Collection<RotorState> states;
    private final double
            dStart,
            dEnd;

    private final long travleMsDefault,
            travelMsMin,
            travelMsMax;

    public RotorStatesFunction(@NotNull Collection<RotorState> states,
                               double domainStart,
                               double domainEnd,
                               long travleMsDefault,
                               long travelMsMin,
                               long travelMsMax) {

        this.states = states;
        dStart = domainStart;
        dEnd = domainEnd;
        this.travleMsDefault = travleMsDefault;
        this.travelMsMin = travelMsMin;
        this.travelMsMax = travelMsMax;
    }

    @Override
    public long getDomainAnimationDurationMsDefault() {
        return travleMsDefault > 0? travleMsDefault: ComplexDomainFunctionI.super.getDomainAnimationDurationMsDefault();
    }

    @Override
    public long getDomainAnimationDurationMsMin() {
        return travelMsMin > 0? travelMsMin: ComplexDomainFunctionI.super.getDomainAnimationDurationMsMin();
    }

    @Override
    public long getDomainAnimationDurationMsMax() {
        return travelMsMax > 0? travelMsMax: ComplexDomainFunctionI.super.getDomainAnimationDurationMsMax();
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
    public double getDomainStart() {
        return dStart;
    }

    @Override
    public double getDomainEnd() {
        return dEnd;
    }
}
