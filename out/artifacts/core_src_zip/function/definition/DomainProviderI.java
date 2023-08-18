package function.definition;

import misc.MathUtil;
import org.jetbrains.annotations.Nullable;

import util.main.ComplexUtil;

import java.awt.*;

public interface DomainProviderI extends ColorProviderI {

    double getDomainStart();

    double getDomainEnd();

    default double getDomainRange() {
        return getDomainEnd() - getDomainStart();
    }

    default boolean isWithinDomain(double input) {
        final double start = getDomainStart();
        final double end = getDomainEnd();

        if (start <= end) {
            return input >= start && input <= end;
        }

        return input >= end && input <= start;
    }

    /**
     * @return fundamental frequency as defined in the Discrete Fourier Transform
     * */
    static double getFundamentalFrequency(double domainRange) {
        return domainRange != 0? 1 / domainRange: 0;
    }

    /**
     * @see #getFundamentalFrequency(double)
     * */
    default double getFundamentalFrequency() {
        return getFundamentalFrequency(getDomainRange());
    }


    /**
     * @param domainRange the domain range (end  - start)
     * @param sampleCount how many values to sample
     * @return sampling domain step
     * */
    static double getSampleDomainStep(double domainRange, int sampleCount) {
        return sampleCount < 2? 0: domainRange / (sampleCount - 1);
    }

    /**
     * @see #getSampleDomainStep(double, int)
     * */
    default double getSampleDomainStep(int sampleCount) {
        return getSampleDomainStep(getDomainRange(), sampleCount);
    }

    /**
     * number of intervals domain range must be divided for numerical integration.<br>
     * This can be used to simulate <b>Discrete Fourier Transform</b><br>
     * Return {@code <= 0} to use default interval count
     *
     * @return number of intervals, or {@code <= 0} to use default interval count
     *
     * @see ComplexUtil#simpson38(ComplexFunctionI, double, double, int)
     * */
    default int getNumericalIntegrationIntervalCount() {
        return -1;
    }

    /**
     * @param input input
     * @return color of the output space point mapped by given input
     * */
    @Override
    @Nullable
    default Color getColor(double input) {
        return null;
    }



    /**
     * Determines how fast the domain range should be transversed
     * <b>by default</b> during fourier graphSeries rotors animation
     *
     * @return default domain range travel duration (milliseconds)
     * */
    default long getDomainAnimationDurationMsDefault() {
        return 10000;                                            // Default Speed
    }

    /**
     * Determines <b>max speed</b> with which the domain range should be transversed
     * during fourier graphSeries rotors animation
     *
     * @return minimum domain range travel duration (milliseconds)
     * */
    default long getDomainAnimationDurationMsMin() {             // Fastest speed
        return 100;
    }

    /**
     * Determines <b>min speed</b> with which the domain range should be transversed
     * during fourier graphSeries rotors animation
     *
     * @return maximum domain range travel duration (milliseconds)
     * */
    default long getDomainAnimationDurationMsMax() {
        return 50000;                                           // slowest speed
    }


    static float durationToSpeedFraction(float fraction) {
        return 1 - fraction;
    }

    default float durationMsToDomainAnimationSpeedFraction(long durationMs) {
        final long durMin = getDomainAnimationDurationMsMin();
        final long durMax = getDomainAnimationDurationMsMax();
        if (durMin == durMax)
            return 1;

        durationMs = MathUtil.constraint(durMin, durMax, Math.abs(durationMs));

        final float frac =  ((float) (durationMs - durMin)) / (durMax - durMin);
        return durationToSpeedFraction(frac);
//        return ComplexUtil.normF(durMin, durMax, durationMs);
    }

    default long domainAnimationSpeedFractionToDurationMs(float fraction) {
        final long durMin = getDomainAnimationDurationMsMin();
        final long durMax = getDomainAnimationDurationMsMax();

        return (long) MathUtil.lerp(durMin, durMax, MathUtil.constraint(0, 1, durationToSpeedFraction(fraction)));
    }


//    default double getRotorDomainStepPerMsMin() {
//        return getDomainRange() / getRotorDomainRangeTravelMsMax();
//    }
//
//    default double getRotorDomainStepPerMsMax() {
//        return getDomainRange() / getRotorDomainRangeTravelMsMin();
//    }
//
//    default double getRotorDomainStepPerMsDefault() {
//        return getDomainRange() / getRotorDomainRangeTravelMsDefault();
//    }
//
//    default double getRotorDomainStepPerMsRange() {
//        return getRotorDomainStepPerMsMax() - getRotorDomainStepPerMsMin();
//    }
//
//    default double factorToDomainStepPerMs(float factor /* [0, 1] */) {
//        final double step = ComplexUtil.map(ComplexUtil.constraint(0, 1, Math.abs(factor)), 0, 1, getRotorDomainStepPerMsMin(), getRotorDomainStepPerMsMax());
//        return factor < 0? -step: step;
//    }
//
//    default double percentToDomainStepPerMs(int percent /* [0, 100] */) {
//        return factorToDomainStepPerMs(percent / 100f);
//    }
//
//    /* [0, 1] */
//    default float domainStepPerMsToFactor(double domainStepPerMs) {
//        final double min = getRotorDomainStepPerMsMin();
//        final double max = getRotorDomainStepPerMsMax();
//
//        return (float) ComplexUtil.map(ComplexUtil.constraint(min, max, Math.abs(domainStepPerMs)), min, max, 0, 1);
//    }
//
//    /* [0, 100] */
//    default int domainStepPerMsToPercent(double domainStepPerMs) {
//        return (int) (domainStepPerMsToFactor(domainStepPerMs) * 100);
//    }



//    @NotNull
//    default EndInfo isEnded(boolean inputStepPositive, double input) {
//        return isEnded(this, inputStepPositive, input);
//    }
//
//
//    class EndInfo {
//        public boolean ended;
//        public boolean isBoundDomainEnd;
//        public double endBound;
//    }
//
//    @NotNull
//    static EndInfo isEnded(@NotNull DomainProviderI d, boolean inputStepPositive, double input) {
//        final double dstart = d.getDomainStart();
//        final double dend = d.getDomainEnd();
//        final boolean rangePos = dend >= dstart;
//
//        final EndInfo info = new EndInfo();
//        if (rangePos) {
//            if (inputStepPositive) {
//                info.ended = input > dend;
//                info.isBoundDomainEnd = true;
//                info.endBound = dend;
//            } else {
//                info.ended = input < dstart;
//                info.isBoundDomainEnd = false;
//                info.endBound = dstart;
//            }
//        } else {
//            if (inputStepPositive) {
//                info.ended = input > dstart;
//                info.isBoundDomainEnd = false;
//                info.endBound = dstart;
//            } else {
//                info.ended = input < dend;
//                info.isBoundDomainEnd = true;
//                info.endBound = dend;
//            }
//        }
//
//        return info;
//    }

}
