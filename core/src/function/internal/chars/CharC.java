package function.internal.chars;

import function.graphic.CharFunction;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;

public class CharC extends CharFunction {

    private final float w1 = 1.1f;
    private final float w2 = 8.6f;
    private final float w = w1 + w2;

    private final float h1 = 3.1f;
    private final float h2 = 7.8f;
    private final float h3 = 2.4f;
    private final float h = h1 + h2 + h3;

    public CharC(float zoom, boolean center) {
        super(zoom, center);
    }

    public CharC() {
        super();
    }

    @Override
    public double getWidth() {
        return w;
    }

    @Override
    public double getHeight() {
        return h;
    }

    @Override
    public double getDomainEnd() {
        return 3;
    }

    @Override
    public long getDomainAnimationDurationMsDefault() {
        return 5000;
    }

    @Override
    public long getDomainAnimationDurationMsMin() {
        return 1000;
    }

    @Override
    public long getDomainAnimationDurationMsMax() {
        return 20000;
    }

    @Override
    public @NotNull Complex compute(double i) {
        if (i > 3) {
            i %= 3;
        }

        final double x, y;

        block: {
            if (i < 1) {
                x = (1 - i) * w; y = i * h1;
                break block;
            }

            i--;
            if (i < 1) {
                x = w1 * i; y = h1 + (h2 * i);
                break block;
            }

            i--;
            x = w1 + (i * w2); y = h - (h3 * (1 - i));
        }

        return applyTransform(new Complex(x, y));
    }


//    @Override
//    public @Nullable Color getColor(double input) {
//        return Color.getHSBColor((float) ComplexUtil.map(input, 0, getDomainEnd(), 0.5f, 1f), 1, 1);
//    }
}
