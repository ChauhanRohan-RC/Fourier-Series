package provider;

import app.R;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rotor.RotorState;
import rotor.frequency.RotorFrequencyProviderI;

import java.util.Collection;

public record FunctionMeta(@NotNull FunctionType functionType,
                           @NotNull String displayName,
                           @Nullable RotorFrequencyProviderI frequencyProvider,
                           int defaultInitialRotorCount,
                           boolean hasBaseDefinition,
                           @Nullable Collection<RotorState> preloadedRotorStates) {

    public static final FunctionMeta NOOP = new FunctionMeta(FunctionType.NO_OP, R.DISPLAY_NAME_FUNCTION_NOOP);

    public FunctionMeta(@NotNull FunctionType functionType, @NotNull String displayName) {
        this(functionType, displayName, null, -1, true, null);
    }

    @NotNull
    public String getTypedFunctionDisplayName() {
        return displayName + " (" + functionType.displayName + ")";
    }


}
