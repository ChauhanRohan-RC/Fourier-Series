package main.models.function.path;

import org.jetbrains.annotations.NotNull;
import main.util.PathUtil;

import java.awt.geom.Point2D;

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
    protected @NotNull Point2D interpolate(float i) {
        return PathUtil.interpolateQuad(p0, p1, p2, i);
    }
}


