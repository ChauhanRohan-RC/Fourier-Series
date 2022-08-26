package main.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.function.Consumer;

public class Listeners<T> {

    @Nullable
    private LinkedList<T> mListeners;

    public final int listenersCount() {
        return CollectionUtil.size(mListeners);
    }

    public final void addListener(@NotNull T listener) {
        if (mListeners == null) {
            mListeners = new LinkedList<>();
        }

        mListeners.add(listener);
    }

    public final boolean removeListener(@NotNull T listener) {
        return mListeners != null && mListeners.remove(listener);
    }

    public final boolean containsListener(@NotNull T listener) {
        return mListeners != null && mListeners.contains(listener);
    }

    public final void forEachListener(@NotNull Consumer<T> action) {
        final LinkedList<T> ls = mListeners;
        if (CollectionUtil.isEmpty(ls))
            return;

        for (T l: CollectionUtil.linkedListCopy(ls)) {          // safe-iteration (prevents concurrent modification)
            action.accept(l);
        }
    }
}
