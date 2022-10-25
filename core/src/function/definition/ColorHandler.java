package function.definition;

import org.jetbrains.annotations.Nullable;

public interface ColorHandler {

    @Nullable
    ColorProviderI getColorProvider();

    ColorHandler setColorProvider(@Nullable ColorProviderI colorProvider);

    default ColorHandler transparent() {
        return setColorProvider(ColorProviderI.TRANSPARENT);
    }

    ColorHandler hueCycle(float hueStart, float hueEnd);

    default ColorHandler hueCycle() {
        return hueCycle(0, 1);
    }

}
