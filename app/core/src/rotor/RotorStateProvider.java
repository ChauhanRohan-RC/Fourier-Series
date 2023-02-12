package rotor;

import org.jetbrains.annotations.NotNull;


public interface RotorStateProvider {

    @NotNull
    RotorState getRotorState(int index);
}
