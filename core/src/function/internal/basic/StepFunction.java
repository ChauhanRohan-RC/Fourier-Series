package function.internal.basic;

import function.definition.SignalFunctionI;

public class StepFunction implements SignalFunctionI {

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
    public double getSignalIntensity(double input) {
        if (input > 1) {
            input %= 1;
        }

        return input > 0.5? 1: -1;
    }

    @Override
    public long getDomainAnimationDurationMsDefault() {
        return 2000;
    }

    @Override
    public long getDomainAnimationDurationMsMin() {
        return 500;
    }

    @Override
    public long getDomainAnimationDurationMsMax() {
        return 10000;
    }

}
