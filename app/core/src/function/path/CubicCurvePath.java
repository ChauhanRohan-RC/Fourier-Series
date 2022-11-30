package function.path;

import org.jetbrains.annotations.NotNull;
import util.main.PathUtil;

import java.awt.geom.Point2D;

public class CubicCurvePath extends PathFunction {

    @NotNull
    private final Point2D p0;
    @NotNull
    private final Point2D p1;
    @NotNull
    private final Point2D p2;
    @NotNull
    private final Point2D p3;

    public CubicCurvePath(@NotNull Point2D p0, @NotNull Point2D p1, @NotNull Point2D p2, @NotNull Point2D p3) {
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }

    @Override
    public @NotNull Point2D startPoint() {
        return p0;
    }

    @Override
    public @NotNull Point2D endPoint() {
        return p3;
    }


    @Override
    protected @NotNull Point2D interpolate(float i) {
        return PathUtil.interpolateCubic(p0, p1, p2, p3, i);
    }
}