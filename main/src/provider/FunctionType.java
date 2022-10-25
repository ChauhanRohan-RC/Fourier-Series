package provider;

import org.jetbrains.annotations.NotNull;

public enum FunctionType {

    NO_OP("No-Op"),

    INTERNAL_PROGRAM("Internal Program"),

    INTERNAL_PATH("Internal Path"),

    EXTERNAL_PROGRAM("External Program"),

    EXTERNAL_PATH("External Path"),

    EXTERNAL_ROTOR_STATE("External Rotor State");



    @NotNull
    public final String displayName;

    FunctionType(@NotNull String displayName) {
        this.displayName = displayName;
    }
}


