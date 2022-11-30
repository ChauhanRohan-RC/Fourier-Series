package provider;

import org.jetbrains.annotations.NotNull;

public enum FunctionType {

    NO_OP("No-Op", false),

    INTERNAL_PROGRAM("Internal Program", true),

    INTERNAL_PATH("Internal Path", true),

    EXTERNAL_PROGRAM("External Program", true),

    EXTERNAL_PATH("External Path", true),

    EXTERNAL_ROTOR_STATE("External Rotor State", false);

    @NotNull
    public final String displayName;
    public final boolean serializable;

    FunctionType(@NotNull String displayName, boolean serializable) {
        this.displayName = displayName;
        this.serializable = serializable;
    }
}


