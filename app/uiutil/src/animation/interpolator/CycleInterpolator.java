package animation.interpolator;

import misc.MathUtil;

/**
 * Repeats the animation for a specified number of cycles. The
 * rate of change follows a sinusoidal pattern.
 *
 */
public class CycleInterpolator implements Interpolator {

    private final float mCycles;

    public CycleInterpolator(float cycles) {
        mCycles = cycles;
    }

    public float getInterpolation(float input) {
        return (MathUtil.sinfast(mCycles * MathUtil.TWO_PI * input));
    }
}
