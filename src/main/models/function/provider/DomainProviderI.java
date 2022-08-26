package main.models.function.provider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import main.util.ComplexUtil;

import java.awt.*;

public interface DomainProviderI extends ColorProviderI {

    double getDomainStart();

    double getDomainEnd();

    default double getDomainRange() {
        return getDomainEnd() - getDomainStart();
    }

    default double getDomainRangeTravelMsDefault() {
        return 2000;
    }

    default double getDomainRangeTravelMsMin() {             // Fastest speed
        return 200;
    }

    default double getDomainRangeTravelMsMax() {
        return 5000;                                           // slowest speed
    }

    @Override
    default @Nullable Color getColor(double input) {
        return null;
    }

    default double getDomainStepPerMsMin() {
        return getDomainRange() / getDomainRangeTravelMsMax();
    }

    default double getDomainStepPerMsMax() {
        return getDomainRange() / getDomainRangeTravelMsMin();
    }

    default double getDomainStepPerMsDefault() {
        return getDomainRange() / getDomainRangeTravelMsDefault();
    }

    default double getDomainStepPerMsRange() {
        return getDomainStepPerMsMax() - getDomainStepPerMsMin();
    }

    default double factorToDomainStepPerMs(float factor /* [0, 1] */) {
        final double step = ComplexUtil.map(ComplexUtil.constraint(0, 1, Math.abs(factor)), 0, 1, getDomainStepPerMsMin(), getDomainStepPerMsMax());
        return factor < 0? -step: step;
    }

    default double percentToDomainStepPerMs(int percent /* [0, 100] */) {
        return factorToDomainStepPerMs(percent / 100f);
    }

    /* [0, 1] */
    default float domainStepPerMsToFactor(double domainStepPerMs) {
        final double min = getDomainStepPerMsMin();
        final double max = getDomainStepPerMsMax();

        return (float) ComplexUtil.map(ComplexUtil.constraint(min, max, Math.abs(domainStepPerMs)), min, max, 0, 1);
    }

    /* [0, 100] */
    default int domainStepPerMsToPercent(double domainStepPerMs) {
        return (int) (domainStepPerMsToFactor(domainStepPerMs) * 100);
    }



    @NotNull
    default EndInfo isEnded(boolean inputStepPositive, double input) {
        return isEnded(this, inputStepPositive, input);
    }


    class EndInfo {
        public boolean ended;
        public boolean isBoundDomainEnd;
        public double endBound;
    }

    @NotNull
    static EndInfo isEnded(@NotNull DomainProviderI d, boolean inputStepPositive, double input) {
        final double dstart = d.getDomainStart();
        final double dend = d.getDomainEnd();
        final boolean rangePos = dend >= dstart;

        final EndInfo info = new EndInfo();
        if (rangePos) {
            if (inputStepPositive) {
                info.ended = input > dend;
                info.isBoundDomainEnd = true;
                info.endBound = dend;
            } else {
                info.ended = input < dstart;
                info.isBoundDomainEnd = false;
                info.endBound = dstart;
            }
        } else {
            if (inputStepPositive) {
                info.ended = input > dstart;
                info.isBoundDomainEnd = false;
                info.endBound = dstart;
            } else {
                info.ended = input < dend;
                info.isBoundDomainEnd = true;
                info.endBound = dend;
            }
        }

        return info;
    }

}
