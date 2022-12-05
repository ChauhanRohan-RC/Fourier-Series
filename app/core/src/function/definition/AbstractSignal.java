package function.definition;

/**
 * {@inheritDoc}
 * */
public abstract class AbstractSignal implements SignalFunctionI {

    private volatile boolean mReal = DEFAULT_REAL;

    public AbstractSignal setReal(boolean real) {
        mReal = real;
        return this;
    }

    @Override
    public boolean isReal() {
        return mReal;
    }
}
