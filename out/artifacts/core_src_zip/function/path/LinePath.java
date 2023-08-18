package function.path;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import util.main.PathUtil;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LinePath extends PathFunction {

    @NotNull
    private final Point2D p0;
    @NotNull
    private final Point2D p1;

    public LinePath(@NotNull Point2D p0, @NotNull Point2D p1) {
        this.p0 = p0;
        this.p1 = p1;
    }

    @Override
    public @NotNull Point2D startPoint() {
        return p0;
    }

    @Override
    public @NotNull Point2D endPoint() {
        return p1;
    }

    @Override
    public int getControlPointsCount() {
        return 0;
    }

    @Override
    public @NotNull Point2D getControlPointAt(int index) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for control point count 0 (Line Path)");
    }

    @Override
    public @NotNull @Unmodifiable List<Point2D> getControlPointsImCopy(boolean withStartEnd) {
        return withStartEnd? List.of(p0, p1): Collections.emptyList();
    }

    @Override
    @NotNull
    public Point2D interpolate(float i) {
        return PathUtil.interpolateLinear(p0, p1, i);
    }
}
