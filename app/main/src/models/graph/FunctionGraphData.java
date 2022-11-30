package models.graph;

import models.FunctionGraphMode;
import org.jetbrains.annotations.NotNull;
import xchart.XYSeriesData;

public record FunctionGraphData(@NotNull FunctionGraphMode graphMode,
                                @NotNull XYSeriesData @NotNull[] graphSeries) {
}
