package main.models.function;

import main.models.ComplexSum;
import main.models.rotor.RotorState;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A class that synthesize a function from Rotor States
 * */
public class RotorStatesFunction implements ComplexDomainFunctionI {

    @NotNull
    private final List<RotorState> states;
    private final double dStart, dEnd;

    public RotorStatesFunction(@NotNull List<RotorState> states, double domainStart, double domainEnd) {
        this.states = states;
        dStart = domainStart;
        dEnd = domainEnd;
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
