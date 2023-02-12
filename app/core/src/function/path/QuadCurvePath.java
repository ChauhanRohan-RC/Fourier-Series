package function.path;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import util.main.PathUtil;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;

public class QuadCurvePath extends PathFunction {

    @NotNull
    private final Point2D p0;
    @NotNull
    private final Point2D p1;
    @NotNull
    private final Point2D p2;

    public QuadCurvePath(@NotNull Point2D p0, @NotNull Point2D p1, @NotNull Point2D p2) {
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
    }

    @Override
    public @NotNull Point2D startPoint() {
        return p0;
    }

    @Override
    public @NotNull Point2D endPoint() {
        return p2;
    }


    @Override
    public int getControlPointsCount() {
        return 1;
    }

    @Override
    public @NotNull Point2D getControlPointAt(int index) throws IndexOutOfBoundsException {
        if (index == 0)
            return p1;
        throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for control point count 1 (Quad Path)");
    }

    @Override
    public @NotNull @Unmodifiable List<Point2D> getControlPointsImCopy(boolean withStartEnd) {
        return withStartEnd? List.of(p0, p1, p2): List.of(p1);
    }


    @Override
    public @NotNull Point2D interpolate(float i) {
        return PathUtil.interpolateQuad(p0, p1, p2, i);
    }
}


