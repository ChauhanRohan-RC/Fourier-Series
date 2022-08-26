//package main.models.function;
//
//import org.apache.commons.math3.complex.Complex;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//import main.util.ComplexUtil;
//
//import java.awt.*;
//
///**
// * Wraps a non periodic function, and makes it periodic by linking
// * end to start
// * */
//public class PeriodicFunctionWrapper extends ComplexDomainFunctionWrapper {
//
//    public static final Color COLOR_LINK = new Color(255, 54, 54, 255);
//
////    private final double baseDStart, baseDEnd, dEnd;
////    private final boolean rangePositive;
//
////    @Nullable
////    private Complex startValue, endValue;
//
//    public PeriodicFunctionWrapper(@NotNull ComplexDomainFunctionI base) {
//        super(base);
////        baseDStart = base.getDomainStart();
////        baseDEnd = base.getDomainEnd();
////
////        final double range = baseDEnd - baseDStart;
////        rangePositive = range >= 0;
////        dEnd = baseDEnd + (rangePositive? 1: -1);
//    }
//
//    @Override
//    public double getDomainEnd() {
//        return base.getDomainEnd() + 1;
//    }
//
//    private boolean isInBaseRange(double input) {
//        return input <= base.getDomainEnd();
//
////        return rangePositive? input >= baseDStart && input <= baseDEnd: input <= baseDStart && input >= baseDEnd;
//    }
//
//    @Override
//    @NotNull
//    public  Complex compute(double input) {
//        if (isInBaseRange(input)) {
//            return base.compute(input);
//        }
//
//        input -= base.getDomainEnd();                // must be in range [0, 1]
////        Log.d("Input: af " + (float) input);
////        if (startValue == null) {
////            startValue = base.compute(baseDStart);
////        }
////
////        if (endValue == null) {
////            endValue = base.compute(baseDEnd);
////        }
//
//
//        return ComplexUtil.interpolateLinear(base.compute(base.getDomainEnd()), base.compute(base.getDomainStart()), (float) input);
//    }
//
//    @Override
//    public @Nullable Color getColor(double input) {
//        return isInBaseRange(input)? base.getColor(input): COLOR_LINK;
//    }
//}
