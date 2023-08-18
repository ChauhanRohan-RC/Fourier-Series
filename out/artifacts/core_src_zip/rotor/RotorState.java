package rotor;

import function.definition.ComplexDomainFunctionI;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.main.ComplexUtil;

import java.util.Comparator;

public class RotorState implements Comparable<RotorState> {

    public static final RotorState ZERO = new RotorState(0, Complex.ZERO);

    public static final Comparator<RotorState> COMPARATOR_FREQ_ASC = Comparator.comparingDouble(RotorState::getFrequency);
    public static final Comparator<RotorState> COMPARATOR_FREQ_DESC = COMPARATOR_FREQ_ASC.reversed();

    public static final Comparator<RotorState> COMPARATOR_MAG_ASC = Comparator.comparingDouble(RotorState::getMagnitudeScale);
    public static final Comparator<RotorState> COMPARATOR_MAG_DESC = COMPARATOR_MAG_ASC.reversed();

    public static final Comparator<RotorState> COMPARATOR_NATURAL = COMPARATOR_FREQ_ASC;

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
    private final double mTipCoefficient;

    @Nullable
    private Double mCoefficientArg;

    public RotorState(double frequency, @NotNull Complex coefficient, int direction) {
        mFrequency = frequency;
        mCoefficient = coefficient;
        mCoefficientAbs = coefficient.abs();

        mTipCoefficient = ComplexUtil.getFourierExpTermPowerCoefficient(direction, frequency);
    }

    public RotorState(double frequency, @NotNull Complex coefficient) {
        this(frequency, coefficient, ComplexUtil.DIRECTION_FOURIER_SERIES);
    }

    public RotorState(double frequency, @NotNull ComplexDomainFunctionI function) {
        this(frequency, ComplexUtil.fourierSeriesCoefficient(function, frequency));
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
        if (mCoefficientAbs == 0)
            return Complex.ZERO;

        return ComplexUtil.complexExpFast(mTipCoefficient * input).mult(mCoefficient).toComplex();
    }

    @NotNull
    public final Complex getFourierTransformOutput(double domainRange) {
        return ComplexUtil.fsCoefficientToFt(mCoefficient, domainRange);
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

        if (o instanceof final RotorState state) {
            return mFrequency == state.mFrequency && mCoefficient.equals(state.mCoefficient);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (31 * Double.hashCode(mFrequency)) + mCoefficient.hashCode();
    }


    @Override
    public int compareTo(@NotNull RotorState o) {
        return COMPARATOR_NATURAL.compare(this, o);
    }
}
