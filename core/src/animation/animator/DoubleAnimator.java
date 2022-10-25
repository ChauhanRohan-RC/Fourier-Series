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
    protected void doUpdate(float elapsedFraction) {
        updateCurValue(getStartValue() + ((getEndValue() - getStartValue()) * getInterpolator().getInterpolation(elapsedFraction)));
    }
}