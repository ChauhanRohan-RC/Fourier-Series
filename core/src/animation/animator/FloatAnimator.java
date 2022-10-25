package animation.animator;

import org.jetbrains.annotations.NotNull;

public class FloatAnimator extends Animator<Float> {

    public FloatAnimator(float startVal, float endVal) {
        super(startVal, endVal);
    }

    @NotNull
    public FloatAnimator reverse() {
        final FloatAnimator anim = new FloatAnimator(getActualEndValue(), getActualStartValue());
        anim.copyAttributes(this);
        return anim;
    }

    @Override
    protected void doUpdate(float elapsedFraction) {
        updateCurValue(getStartValue() + ((getEndValue() - getStartValue()) * getInterpolator().getInterpolation(elapsedFraction)));
    }
}
