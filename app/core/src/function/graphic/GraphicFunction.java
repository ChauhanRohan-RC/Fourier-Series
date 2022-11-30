package function.graphic;

import function.definition.ComplexDomainFunctionI;
import models.FunctionGraphMode;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.Rectangle2D;


public abstract class GraphicFunction implements ComplexDomainFunctionI {

    protected final float zoom;
    protected final boolean center;

    protected GraphicFunction(float zoom, boolean center) {
        this.zoom = zoom;
        this.center = center;
    }

//    protected GraphicFunction() {
//        this(1, false);
//    }

    @NotNull
    public abstract Rectangle2D getBounds();

    @NotNull
    protected Complex applyTransform(@NotNull Complex o) {
        if (center) {
            final Rectangle2D bounds = getBounds();
            o = o.subtract(new Complex(bounds.getCenterX(), bounds.getCenterY()));
        }

        return o.multiply(zoom);
    }


    @Override
    public @Nullable FunctionGraphMode getDefaultGraphMode() {
        return FunctionGraphMode.OUTPUT_SPACE;
    }

}
