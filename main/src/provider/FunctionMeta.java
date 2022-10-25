package provider;

import app.R;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rotor.RotorFrequencyProviderE;
import rotor.RotorState;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public record FunctionMeta(@NotNull FunctionType functionType,
                           @NotNull String displayName,
                           int initialRotorCount,
                           @Nullable RotorFrequencyProviderE frequencyProvider,
                           @Nullable Collection<RotorState> preloadedRotorStates) {

    public static final FunctionMeta NOOP = new FunctionMeta(FunctionType.NO_OP, R.DISPLAY_NAME_FUNCTION_NOOP);

    public FunctionMeta(@NotNull FunctionType functionType, @NotNull String displayName) {
        this(functionType, displayName, -1, null, null);
    }

}
