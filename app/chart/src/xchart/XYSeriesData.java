package xchart;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class XYSeriesData implements XYSeriesDataI {

    public static double @NotNull[] negate(double @NotNull[] data) {
        for (int i=0; i < data.length; i++) {
            data[i] = -data[i];
        }

        return data;
    }

    @NotNull
    private final String name;
    private final double @NotNull [] xData;
    private final double @NotNull [] yData;
    private final double @Nullable[] errorBars;
    @Nullable
    private final Object tag;

    public XYSeriesData(@NotNull String name,
                        double @NotNull [] xData,
                        double @NotNull [] yData,
                        double @Nullable[] errorBars,
                        @Nullable Object tag) {
        this.name = name;
        this.xData = xData;
        this.yData = yData;
        this.errorBars = errorBars;
        this.tag = tag;
    }


    public XYSeriesData(@NotNull String name,
                        double @NotNull [] xData,
                        double @NotNull [] yData) {
        this(name, xData, yData, null, null);
    }

    @Override
    public @NotNull String name() {
        return name;
    }

    @Override
    public double @NotNull [] xData() {
        return xData;
    }

    @Override
    public double @NotNull [] yData() {
        return yData;
    }

    @Override
    public double @Nullable [] errorBars() {
        return errorBars;
    }

    @Override
    public @Nullable Object tag() {
        return tag;
    }


    public void negateX() {
        negate(xData);
    }

    public void negateY() {
        negate(yData);
    }

    public void negateErrorBars() {
        if (errorBars != null) {
            negate(errorBars);
        }
    }
}
