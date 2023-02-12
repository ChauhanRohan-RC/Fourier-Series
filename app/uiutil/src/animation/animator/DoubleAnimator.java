package animation.animator;

import org.jetbrains.annotations.NotNull;

public class DoubleAnimator extends Animator<Double> {

    public DoubleAnimator(double startVal, double endVal) {
        super(startVal, endVal);
    }

    @NotNull
    public DoubleAnimator reverse() {
        final DoubleAnimator anim = new DoubleAnimator(getActualEndValue(), getActualStartValue());
        anim.copyAttributes(this);
        return anim;
    }

    @Override
    public Double interpolateValue(float elapsedFraction) {
        return getStartValue() + ((getEndValue() - getStartValue()) * getInterpolator().getInterpolation(elapsedFraction));
    }

//    @Override
//    protected void doUpdate(float elapsedFraction) {
//        final double newValue = interpolateValue(elapsedFraction);
//        if (getCurrentValue() != newValue) {
//            updateCurrentValue(newValue);
//        }
//    }
}