package models.graph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.main.ComplexUtil;

public record GraphSeries(@NotNull String name,
                          double @NotNull [] xData,
                          double @NotNull [] yData,
                          double @Nullable[] errorBars,
                          Object tag) {

    public GraphSeries(@NotNull String name,
                       double @NotNull [] xData,
                       double @NotNull [] yData) {
        this(name, xData, yData, null, null);
    }

    public void negateX() {
        ComplexUtil.negate(xData);
    }

    public void negateY() {
        ComplexUtil.negate(yData);
    }

    public void negateErrorBars() {
        if (errorBars != null) {
            ComplexUtil.negate(errorBars);
        }
    }
}
