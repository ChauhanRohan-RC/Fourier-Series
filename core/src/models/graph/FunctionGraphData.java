package models.graph;

import org.jetbrains.annotations.NotNull;

public record FunctionGraphData(@NotNull FunctionGraphMode graphMode,
                                @NotNull GraphSeries @NotNull[] graphSeries) {
}
