package main.models.function.x;

import main.models.function.ComplexDomainFunctionI;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class StepFunction implements ComplexDomainFunctionI {

    private final boolean mReal;

    public StepFunction(boolean real) {
        mReal = real;
    }

    public StepFunction() {
        this(true);
    }

    @Override
    public double getDomainStart() {
        return 0;
    }

    @Override
    public double getDomainEnd() {
        return 1;
    }

//    @Override
//    public @Nullable Color getColor(double input) {
//        return Color.getHSBColor((float) (input % 1), 1, 1);
//    }

    @Override
    public @NotNull Complex compute(double input) {
        if (input > 1) {
            input %= 1;
        }

        final double v = input > 0.5? 1: -1;
        return mReal? new Complex(v, 0): new Complex(0, v);
    }

    @Override
    public double getDomainRangeTravelMsDefault() {
        return 2000;
    }

    @Override
    public double getDomainRangeTravelMsMin() {
        return 500;
    }

    @Override
    public double getDomainRangeTravelMsMax() {
        return 10000;
    }

}
