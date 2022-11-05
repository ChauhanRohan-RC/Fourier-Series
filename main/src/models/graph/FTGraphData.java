package models.graph;

import org.jetbrains.annotations.NotNull;

public record FTGraphData(@NotNull FTGraphMode graphMode,
                          int rotorCount,
                          int currentRotorIndex,
                          boolean xInverted,
                          boolean yInverted,
                          @NotNull GraphSeries @NotNull[] graphSeries) {

    private static final GraphSeries[] EMPTY_SERIES = new GraphSeries[0];

    @NotNull
    public static FTGraphData empty(@NotNull FTGraphMode graphMode) {
        return new FTGraphData(graphMode, 0, -1, false, false, EMPTY_SERIES);
    }

}
