package models.graph;

import org.jetbrains.annotations.NotNull;

/**
 * Defines how Fourier Transform Graph of a function is drawn
 * */
public enum FTGraphMode {

    REAL("Out (Real)"),

    IMG("Out (Imaginary)"),

    REAL_AND_IMG("Out (Real and Img)", "Strength"),

    MAG("Out (Magnitude)"),

    PHASE("Out (Phase)"),

    MAG_AND_PHASE("Out (Mag and Phase)", "Strength");

    @NotNull
    public final String displayName;
    @NotNull
    public final String yAxisTitle;
    @NotNull
    public final String xAxisTitle;

    FTGraphMode(@NotNull String displayName, @NotNull String yAxisTitle, @NotNull String xAxisTitle) {
        this.displayName = displayName;
        this.yAxisTitle = yAxisTitle;
        this.xAxisTitle = xAxisTitle;
    }

    FTGraphMode(@NotNull String displayName, @NotNull String yAxisTitle) {
        this(displayName, yAxisTitle, "Frequency (Hz)");
    }

    FTGraphMode(@NotNull String displayName) {
        this(displayName, displayName);
    }
}
