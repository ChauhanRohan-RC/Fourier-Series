package function.path;

import function.definition.ColorHandler;
import function.definition.ColorProviderI;
import function.definition.ComplexDomainFunctionI;
import models.FunctionGraphMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public interface PathFunctionI extends ComplexDomainFunctionI, ColorHandler {

    boolean DEFAULT_INTERPOLATE_LINE = false;       // increasing points decreases accuracy
    int DEFAULT_INTERPOLATE_POINT_COUNT = 10;


    @Override
    default long getDomainAnimationDurationMsDefault() {
        return 2000;
    }

    @Override
    default long getDomainAnimationDurationMsMin() {
        return 200;
    }

    @Override
    default long getDomainAnimationDurationMsMax() {
        return 6000;
    }

    @Override
    default @Nullable FunctionGraphMode getDefaultGraphMode() {
        return FunctionGraphMode.OUTPUT_SPACE;
    }


    @NotNull
    Point2D startPoint();

    @NotNull
    Point2D endPoint();

    /**
     * @return number of control points in this PathFunction, minimum 0 for line path
     * */
    int getControlPointsCount();

    @NotNull
    Point2D getControlPointAt(int index) throws IndexOutOfBoundsException;

    @NotNull
    @Unmodifiable
    default List<Point2D> getControlPointsImCopy(boolean withStartEnd) {
        final List<Point2D> pts = new ArrayList<>();
        if (withStartEnd) {
            pts.add(startPoint());
        }

        for (int i = 0; i < getControlPointsCount(); i++) {
            pts.add(getControlPointAt(i));
        }

        if (withStartEnd) {
            pts.add(endPoint());
        }

        return pts;
    }

    @NotNull
    Point2D interpolate(float i);


    /**
     * samples a path segment by interpolation points along the curve
     *
     * @param pointCount no of points to sample from the path segment
     * @param interpolateLine whether to sample points from straight lines
     * @return list of sampled points, which may have different number of sample points based on interpolateLine flag
     * */
    @NotNull
    default List<Point2D> interpolatePoints(int pointCount, boolean interpolateLine) {
        final List<Point2D> r = new ArrayList<>(pointCount + 2);
        r.add(startPoint());
        r.add(endPoint());

        if (pointCount > 2 && (interpolateLine || getControlPointsCount() > 0)) {
            final float step = 1f / (pointCount - 1);

            for (int i=1; i < pointCount - 1; i++) {
                r.add(interpolate(i * step));
            }
        }

        return r;
    }

    @NotNull
    default List<Point2D> interpolatePoints() {
        return interpolatePoints(DEFAULT_INTERPOLATE_POINT_COUNT, DEFAULT_INTERPOLATE_LINE);
    }


    boolean isContinuityLink();

    @NotNull
    PathFunctionI setIsContinuityLink(boolean isContinuityLink);

    @Override
    @Nullable
    default Color getColor(double input) {
        final ColorProviderI cp = getColorProvider();
        return cp != null? cp.getColor(input): null;
    }
}
