package function.internal.basic;

import function.definition.ComplexDomainFunctionI;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;

public class RectFunction implements ComplexDomainFunctionI {

    private final int width;
    private final int height;

    private final int end;

    public RectFunction(int width, int height) {
        this.width = width;
        this.height = height;

        end = (width + height) * 2;
    }

    @Override
    public double getDomainStart() {
        return 0;
    }

    @Override
    public double getDomainEnd() {
        return end;
    }


    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    @Override
    public @NotNull Complex compute(double input) {
        if (input > end) {
            input %= end;
        }

        final double x, y;
        if (input < width) {
            x = input; y = height;
        } else {
            input -= width;
            if (input < height) {
                x = width; y = height - input;
            } else {
                input -= height;
                if (input < width) {
                    x = width - input; y = 0;
                } else {
                    input -= width;
                    x = 0; y = input;
                }
            }
        }

        return new Complex(x, y);
    }

    @Override
    public long getDomainAnimationDurationMsDefault() {
        return 4000;
    }

    @Override
    public long getDomainAnimationDurationMsMin() {
        return 1000;
    }

    @Override
    public long getDomainAnimationDurationMsMax() {
        return 20000;
    }
}
