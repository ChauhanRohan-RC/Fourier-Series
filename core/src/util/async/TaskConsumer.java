package util.async;

import org.jetbrains.annotations.Nullable;

public interface TaskConsumer<T> extends Consumer<T> {

    void onFailed(@Nullable Throwable t);

}
