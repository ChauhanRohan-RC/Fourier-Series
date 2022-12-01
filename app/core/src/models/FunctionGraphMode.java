package models;

import function.definition.ComplexDomainFunctionI;
import org.jetbrains.annotations.NotNull;

/**
 * Defines how {@link ComplexDomainFunctionI ComplexFunction} graph should be plotted<br>
 * A complex Function has 3 dimensions
 * <p>
 *     1. Input dimension (real, 1D)<br>
 *     2. Output dimension (complex, 2D)
 * </p>
 * <br>
 * This class defines how a 3D complex function should be sampled and plotted over 2D space
 *
 * @see ComplexDomainFunctionI#getDefaultGraphMode() DefaultGraphMode
 * */
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
