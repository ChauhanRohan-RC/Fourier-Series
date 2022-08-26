package main.models.function.path;

import main.models.ColorHandler;
import main.models.function.provider.ColorProviderI;
import main.models.function.ComplexDomainFunctionI;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.geom.Point2D;


public abstract class PathFunction implements ComplexDomainFunctionI, ColorHandler {

    private boolean mIsContinuityLink;

    @Nullable
    private ColorProviderI mColorProvider;

    @Override
    public final double getDomainStart() {
        return 0;
    }

    @Override
    public final double getDomainEnd() {
        return 1;
    }

    @Override
    public double getDomainRangeTravelMsDefault() {
        return 2000;
    }

    @Override
    public double getDomainRangeTravelMsMin() {
        return 200;
    }

    @Override
    public double getDomainRangeTravelMsMax() {
        return 6000;
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
        return mColorProvider;
    }

    @Override
    public PathFunction setColorProvider(@Nullable ColorProviderI colorProvider) {
        mColorProvider = colorProvider;
        return this;
    }

    @Override
    public ColorHandler hueCycle(float hueStart, float hueEnd) {
        return setColorProvider(new HueCycle(this, hueStart, hueEnd));
    }


    public boolean isContinuityLink() {
        return mIsContinuityLink;
    }

    public PathFunction setIsContinuityLink(boolean isContinuityLink) {
        mIsContinuityLink = isContinuityLink;
        return this;
    }

    @Override
    @Nullable
    public Color getColor(double input) {
        return mColorProvider != null? mColorProvider.getColor(input): null;
    }
}
