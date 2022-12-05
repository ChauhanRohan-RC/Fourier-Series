package function.definition;

import org.jetbrains.annotations.Nullable;
import rotor.RotorState;

public interface CacheRotorStateProvider {

    boolean containsCachedRotorState(double frequency);

    @Nullable
    RotorState getCachedRotorState(double frequency);

}
