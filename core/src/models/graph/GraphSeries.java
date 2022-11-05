package models.graph;

import org.jetbrains.annotations.NotNull;

public record GraphSeries(@NotNull String name, double @NotNull [] xData, double @NotNull [] yData) {
}
