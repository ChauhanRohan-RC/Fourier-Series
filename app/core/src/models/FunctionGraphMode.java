package models;

import org.jetbrains.annotations.NotNull;

public enum FunctionGraphMode {

    OUTPUT_SPACE("Complex Output Space",
            "Real",
            "Imaginary"
    ),

    INPUT_VS_OUT_REAL("Input vs Output (Real)",
            "Input",
            "Output (Real)"
    ),

    INPUT_VS_OUT_IMAGINARY("Input vs Output (Imaginary)",
            "Input",
            "Output (Imaginary)"
    ),

    INPUT_VS_OUT_REAL_AND_IMG("Input vs Output (Real and Img)",
            "Input",
            "Output"
    ),

    INPUT_VS_OUT_MAGNITUDE("Input vs Output (Magnitude)",
            "Input",
            "Output (Magnitude)"
    ),

    INPUT_VS_OUT_ARGUMENT("Input vs Output (Argument)",
            "Input",
            "Output (Argument)"
    ),

    INPUT_VS_OUT_MAG_AND_ARG("Input vs Output (Mag and Arg)",
            "Input",
            "Output"
    ),

    ;

    @NotNull
    public final String displayName;
    @NotNull
    public final String xAxisTitle;
    @NotNull
    public final String yAxisTitle;

    FunctionGraphMode(@NotNull String displayName, @NotNull String xAxisTitle, @NotNull String yAxisTitle) {
        this.displayName = displayName;
        this.xAxisTitle = xAxisTitle;
        this.yAxisTitle = yAxisTitle;
    }
}
