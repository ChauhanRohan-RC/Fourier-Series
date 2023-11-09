package function.definition;

import models.ComplexBuilder;
import models.FunctionGraphMode;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rotor.frequency.RotorFrequencyProviderI;

import java.util.StringJoiner;

/**
 * A function that constructively combines two or more functions/signals.
 * Output of this function at a certain point is the sum of the outputs of the constituent functions.
 * The merging behaviour is controlled by {@link MergeMode MergeMode}
 * */
public class MergedFunction implements ComplexDomainFunctionI {

    public enum MergeMode {
        /**
         * Domain of the final function = Union of all the constituent function domains
         * */
        UNION,

        /**
         * Domain of the final function = Intersection of all the constituent function domains
         * */
        INTERSECTION
    }

    @NotNull
    private final MergeMode mergeMode;
    @NotNull
    private final ComplexDomainFunctionI[] functions;
    private final double domainStart;
    private final double domainEnd;

    private final long domainTravelMsMin;
    private final long domainTravelMsMax;
    private final long domainTravelMsDef;

    public MergedFunction(@NotNull MergeMode mergeMode, ComplexDomainFunctionI @NotNull... functions) {
        if (functions == null || functions.length == 0) {
            throw new IllegalArgumentException("Merged Function requires at least 1 base function!");
        }

        this.mergeMode = mergeMode;
        this.functions = functions;

//        if (functions == null || functions.length == 0) {
//            domainStart = 0;
//            domainEnd = 0;
//            domainTravelMsMin = -1;
//            domainTravelMsMax = -1;
//            domainTravelMsDef = -1;
//        } else {
//
//        }

        double start = 0;
        double end = 0;
        long msDef = 0;
        boolean first = true;

        for (ComplexDomainFunctionI func: functions) {
            msDef = Math.max(msDef, func.getDomainAnimationDurationMsDefault());

            if (first) {
                start = func.getDomainStart();
                end = func.getDomainEnd();
                first = false;
                continue;
            }

            switch (mergeMode) {
                case INTERSECTION -> {
                    start = Math.max(start, func.getDomainStart());
                    end = Math.min(end, func.getDomainEnd());
                } case UNION -> {
                    start = Math.min(start, func.getDomainStart());
                    end = Math.max(end, func.getDomainEnd());
                } default -> throw new AssertionError("Unknown merge mode: " + mergeMode);
            }
        }

        domainStart = start;
        domainEnd = end;
        domainTravelMsDef = msDef;
        domainTravelMsMin = (long) (msDef / 10f);
        domainTravelMsMax = msDef * 20;
    }


    @NotNull
    public MergeMode getMergeMode() {
        return mergeMode;
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
    public @NotNull Complex compute(double input) {
        final ComplexBuilder sum = new ComplexBuilder();

        for (ComplexDomainFunctionI func: functions) {
            if (func.isWithinDomain(input)) {
                sum.add(func.compute(input));
            }
        }

        return sum.toComplex();
    }


    @Override
    public long getDomainAnimationDurationMsMin() {
        if (domainTravelMsMin != -1)
            return domainTravelMsMin;

        return ComplexDomainFunctionI.super.getDomainAnimationDurationMsMin();
    }

    @Override
    public long getDomainAnimationDurationMsMax() {
        if (domainTravelMsMax != -1)
            return domainTravelMsMax;

        return ComplexDomainFunctionI.super.getDomainAnimationDurationMsMax();
    }

    @Override
    public long getDomainAnimationDurationMsDefault() {
        if (domainTravelMsDef != -1)
            return domainTravelMsDef;

        return ComplexDomainFunctionI.super.getDomainAnimationDurationMsDefault();
    }

    @Override
    public boolean isFrequencySupported(double frequency) {
        for (ComplexDomainFunctionI func: functions) {
            if (func.isFrequencySupported(frequency))
                return true;
        }

        return false;
    }

    @Override
    public @Nullable RotorFrequencyProviderI getFunctionDefaultFrequencyProvider() {
        if (functions.length == 0) {
            return ComplexDomainFunctionI.super.getFunctionDefaultFrequencyProvider();
        }

        return functions[0].getFunctionDefaultFrequencyProvider();           // first frequency provider
    }

    @Override
    public @Nullable FunctionGraphMode getDefaultGraphMode() {
        if (functions.length == 0) {
            return ComplexDomainFunctionI.super.getDefaultGraphMode();
        }

        return functions[0].getDefaultGraphMode();           // first graph mode
    }

    @Override
    public String toString() {
        final StringJoiner sj = new StringJoiner("\n\t", "", "\n]");
        sj.add("MergedFUnction[ mergeMode=" + mergeMode);

        for (ComplexDomainFunctionI f: functions) {
            sj.add(f.toString());
        }

        return sj.toString();
    }
}
