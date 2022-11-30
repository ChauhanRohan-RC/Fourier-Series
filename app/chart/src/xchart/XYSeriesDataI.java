package xchart;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface XYSeriesDataI {

    @NotNull
    String name();

    double @NotNull[] xData();

    double @NotNull[] yData();

    double @Nullable[] errorBars();

    @Nullable
    Object tag();
}
