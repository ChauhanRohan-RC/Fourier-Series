package util;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import util.async.Async;
import util.async.Consumer;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Listeners<T> {

    @NonNls
    private final List<T> mListeners = Collections.synchronizedList(new LinkedList<>());

    protected void onActive() {
    }

    protected void onInactive() {
    }

    protected boolean shouldAddListener(@NotNull T listener) {
        return true;
    }

    protected void onListenerRemoved(@NotNull T listener) {
    }



    public final int listenersCount() {
        return mListeners.size();
    }

    public final boolean addListener(@NotNull T listener) {
        if (!shouldAddListener(listener))
            return false;

        mListeners.add(listener);
        if (mListeners.size() == 1) {
            onActive();
        }

        return true;
    }

    public final boolean removeListener(@NotNull T listener) {
        final boolean removed = mListeners.remove(listener);

        if (removed) {
            onListenerRemoved(listener);
            if (mListeners.isEmpty()) {
                onInactive();
            }
        }

        return removed;
    }


    public final boolean containsListener(@NotNull T listener) {
        return mListeners.contains(listener);
    }

    public final boolean ensureListener(@NotNull T listener) {
        return containsListener(listener) || addListener(listener);
    }

    @NotNull
    public final Collection<T> iterationCopy() {
        final List<T> ls = mListeners;
        if (CollectionUtil.isEmpty(ls))
            return Collections.emptyList();

        return CollectionUtil.linkedListCopy(ls);
    }

    public final void forEachListener(@NotNull Consumer<T> action) {
        for (T l: iterationCopy()) {          // safe-iteration (prevents concurrent modification)
            action.consume(l);
        }
    }

    public void dispatchOnMainThread(@NotNull Consumer<T> action) {
        Async.postIfNotOnMainThread(() -> forEachListener(action));
    }
}
