package main.models.rotor;

import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import main.util.ComplexUtil;

public class RotorState {

    public static final RotorState ZERO = new RotorState(0, Complex.ZERO);

    /**
     * frequency of rotor in cycles/secs (Hz)
     *
     * +ve of anticlockwise and -ve for clockwise rotation
     * */
    private final double mFrequency;

    /**
     * Coefficient describing initial state (magnitude scale and phase) of this rotor
     * */
    @NotNull
    private final Complex mCoefficient;

    private final double mCoefficientAbs;
    private final double mTipPreMultiplier;

    @Nullable
    private Double mCoefficientArg;

    public RotorState(double frequency, @NotNull Complex coefficient, int direction) {
        mFrequency = frequency;
        mCoefficient = coefficient;
        mCoefficientAbs = coefficient.abs();
        mTipPreMultiplier = direction * 2 * Math.PI * frequency;
    }

    public RotorState(double frequency, @NotNull Complex coefficient) {
        this(frequency, coefficient, ComplexUtil.getFourierSeriesRotorTipDirection());
    }

    public final double getFrequency() {
        return mFrequency;
    }

    @NotNull
    public final Complex getCoefficient() {
        return mCoefficient;
    }

    public final double getMagnitudeScale() {
        return mCoefficientAbs;
    }

    public final double getCoefficientArgument() {
        if (mCoefficientArg == null) {
            mCoefficientArg = mCoefficient.getArgument();
        }

        return mCoefficientArg;
    }

    public final double getMagnitude(double base) {
        return base * mCoefficientAbs;
    }

    public final double getTipSize(double base) {
        return base * mCoefficientAbs;
    }

    @NotNull
    public final Complex getTip(double input) {
        return new Complex(0, mTipPreMultiplier * input).exp().multiply(mCoefficient);
    }

    @Override
    public String toString() {
        return "RotorState{" +
                "frequency=" + mFrequency +
                ", coefficient=" + mCoefficient +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o instanceof RotorState) {
            final RotorState state = (RotorState) o;
            return mFrequency == state.mFrequency && mCoefficient.equals(state.mCoefficient);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (31 * Double.hashCode(mFrequency)) + mCoefficient.hashCode();
    }



}
