package function.internal.chars;

import function.graphic.CharFunction;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;

public class CharR extends CharFunction {

    private final float w1 = 0.8f;
    private final float w2 = 9;
    private final float w = w1 + w2;

    private final float h1 = 0.8f;
    private final float h2 = 6;
    private final float h3 = 7;
    private final float h = h1 + h2 + h3;

    private final float a = 0.6f;

    public CharR(float zoom, boolean center) {
        super(zoom, center);
    }

    public CharR() {
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
        return 4;
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
        if (i > 4) {
            i %= 4;
        }

        final double x, y;

        block: {
            if (i < 1) {
                x = i * w1; y = i * (h - a);
                break block;
            }

            i--;
            if (i < 1) {
                x = (w1 - a) + (w2 * i); y = h - (h1 * i);
                break block;
            }

            i--;
            if (i < 1) {
                x = w - (w2 * i); y = h3 + (h2 * (1 - i));
                break block;
            }

            i--;
            x = w1 + (i * w2); y = h3 * (1 - i);
        }

        return applyTransform(new Complex(x, y));
    }

//    @Override
//    public @Nullable Color getColor(double input) {
//        return Color.getHSBColor((float) ComplexUtil.map(input, 0, getDomainEnd(), 0, 0.5f), 1, 1);
//    }

}
