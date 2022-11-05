package models.graph;

import org.jetbrains.annotations.NotNull;

public record GraphData(@NotNull FunctionGraphMode graphMode, @NotNull GraphSeries[] graphSeries) {
}
