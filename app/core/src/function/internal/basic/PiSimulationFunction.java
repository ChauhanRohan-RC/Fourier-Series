package function.internal.basic;

import function.RotorStatesFunction;
import java.util.List;

import misc.MathUtil;
import models.FunctionGraphMode;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.Nullable;
import rotor.RotorState;

/**
 * f(x) = exp(ix) + exp(i.pi.x)
 * <br>
 * A function with only two frequencies (rotors), where the second rotor rotates exactly pi times faster than the first one.<br>
 * The output of this function is a complex number.
 * <strong>Since pi is irrational, the function never touches its starting value again, no matter how long the simulation is run.</strong>
 * */
public class PiSimulationFunction extends RotorStatesFunction {
    private static final int NUM_CYCLES = 200;

    public PiSimulationFunction() {
        super(0.0, (double) (NUM_CYCLES * MathUtil.TWO_PI), List.of(new RotorState(1 / MathUtil.TWO_PI, Complex.ONE), new RotorState(0.5, Complex.ONE)));
        setComputeMode(ComputeMode.COMPLEX);
        setFrequenciesExceptExplicitSupported(false);
    }

    public int getInitialRotorCount() {
        return 2;
    }

    public @Nullable FunctionGraphMode getDefaultGraphMode() {
        return FunctionGraphMode.OUTPUT_SPACE;
    }

    public long getDomainAnimationDurationMsMin() {
        return NUM_CYCLES * 600;
    }

    public long getDomainAnimationDurationMsMax() {
        return NUM_CYCLES * 8000;
    }

    public long getDomainAnimationDurationMsDefault() {
        return NUM_CYCLES * 2000;
    }
}
