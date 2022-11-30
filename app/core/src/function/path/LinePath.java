package function.path;

import org.jetbrains.annotations.NotNull;
import util.main.PathUtil;

import java.awt.geom.Point2D;

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
    @NotNull
    protected Point2D interpolate(float i) {
        return PathUtil.interpolateLinear(p0, p1, i);
    }
}
