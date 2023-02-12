package live;

import org.jetbrains.annotations.NotNull;
import async.Async;
import async.Consumer;

import java.util.Collection;

public interface ListenersI<T> {

    int listenersCount();

    boolean addListener(@NotNull T listener);

    boolean removeListener(@NotNull T listener);

    boolean containsListener(@NotNull T listener);

    default boolean ensureListener(@NotNull T listener) {
        return containsListener(listener) || addListener(listener);
    }

    @NotNull
    Collection<T> iterationCopy();

    default void forEachListener(@NotNull Consumer<T> action) {
        for (T l: iterationCopy()) {          // safe-iteration (prevents concurrent modification)
            action.consume(l);
        }
    }

    default void dispatchOnMainThread(@NotNull Consumer<T> action) {
        Async.postIfNotOnMainThread(() -> forEachListener(action));
    }

}
