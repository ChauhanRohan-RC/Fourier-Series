package async;

public interface ThrowableTask<Output> {

    Output begin() throws Throwable;
}
