package function.path;

import function.definition.ColorHandler;
import function.definition.ColorProviderI;
import function.definition.ComplexDomainFunctionI;
import models.graph.FunctionGraphMode;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.geom.Point2D;


public abstract class PathFunction implements ComplexDomainFunctionI, ColorHandler {

    private boolean isContinuityLink;

    @Nullable
    private ColorProviderI colorProvider;

    @Override
    public final double getDomainStart() {
        return 0;
    }

    @Override
    public final double getDomainEnd() {
        return 1;
    }

    @Override
    public long getDomainAnimationDurationMsDefault() {
        return 2000;
    }

    @Override
    public long getDomainAnimationDurationMsMin() {
        return 200;
    }

    @Override
    public long getDomainAnimationDurationMsMax() {
        return 6000;
    }

    @Override
    public @Nullable FunctionGraphMode getDefaultGraphMode() {
        return FunctionGraphMode.OUTPUT_SPACE;
    }

    @NotNull
    public abstract Point2D startPoint();

    @NotNull
    public abstract Point2D endPoint();

    @NotNull
    protected abstract Point2D interpolate(float i);

    @NotNull
    protected Complex transform(@NotNull Point2D interpolatedValue) {
        return new Complex(interpolatedValue.getX(), interpolatedValue.getY());
    }

    @Override
    @NotNull
    public final Complex compute(double input) {
        if (input > 1) {
            input %= 1;
        }

        return transform(interpolate((float) input));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(start:" + startPoint() + ", end: " + endPoint() + ")";
    }

    @Nullable
    @Override
    public ColorProviderI getColorProvider() {
        return colorProvider;
    }

    @Override
    public PathFunction setColorProvider(@Nullable ColorProviderI colorProvider) {
        this.colorProvider = colorProvider;
        return this;
    }

    @Override
    public ColorHandler hueCycle(float hueStart, float hueEnd) {
        return setColorProvider(new HueCycle(this, hueStart, hueEnd));
    }


    public boolean isContinuityLink() {
        return isContinuityLink;
    }

    public PathFunction setIsContinuityLink(boolean isContinuityLink) {
        this.isContinuityLink = isContinuityLink;
        return this;
    }

    @Override
    @Nullable
    public Color getColor(double input) {
        return colorProvider != null? colorProvider.getColor(input): null;
    }
}
