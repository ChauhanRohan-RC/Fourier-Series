package models.graph;

import org.jetbrains.annotations.NotNull;
import xchart.XYSeriesData;

public record FTGraphData(@NotNull FTGraphMode graphMode,
                          int rotorCount,
                          int currentRotorIndex,
                          boolean xInverted,
                          boolean yInverted,
                          @NotNull XYSeriesData @NotNull[] graphSeries) {

    private static final XYSeriesData[] EMPTY_SERIES = new XYSeriesData[0];

    @NotNull
    public static FTGraphData empty(@NotNull FTGraphMode graphMode) {
        return new FTGraphData(graphMode, 0, -1, false, false, EMPTY_SERIES);
    }

}
