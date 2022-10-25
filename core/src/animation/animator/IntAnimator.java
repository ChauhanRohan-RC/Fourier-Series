package animation.animator;

import org.jetbrains.annotations.NotNull;

public class IntAnimator extends Animator<Integer> {

    public IntAnimator(int startVal, int endValue) {
        super(startVal, endValue);
    }

    @NotNull
    public IntAnimator reversed() {
        final IntAnimator anim = new IntAnimator(getActualEndValue(), getActualEndValue());
        anim.copyAttributes(this);
        return anim;
    }

    @Override
    protected void doUpdate(float elapsedFraction) {
        final float delta = ((getEndValue() - getStartValue()) * getInterpolator().getInterpolation(elapsedFraction));
        final int newValue = getStartValue() + ((int) delta);
        if (getCurrentValue() != newValue) {
            updateCurValue(newValue);
        }
    }
}
