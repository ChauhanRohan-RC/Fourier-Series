package provider;

import function.definition.ComplexDomainFunctionI;
import org.apache.batik.parser.ParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractFunctionProvider implements FunctionProviderI {

//    @NotNull
//    public static BaseFunctionProvider ofPaths(@NotNull String title, @NotNull String... paths) {
//        return new BaseFunctionProvider(title, () -> {
//            final Shape shape = PathUtil.parsePathDataStrings(paths);
//            return PathFunctionMerger.create(shape, 1, true);
//        });
//    }

    @NotNull
    private final FunctionMeta meta;
    @Nullable
    private volatile ComplexDomainFunctionI mFunction;


    public AbstractFunctionProvider(@NotNull FunctionMeta meta) {
        this.meta = meta;
    }

    @NotNull
    @Override
    public FunctionMeta getFunctionMeta() {
        return meta;
    }

    @Override
    public final String toString() {
        return getFunctionMeta().displayName();
    }


    @NotNull
    protected abstract ComplexDomainFunctionI loadFunction() throws ParseException;

    @Override
    @NotNull
    public final ComplexDomainFunctionI requireFunction() throws ParseException {
        ComplexDomainFunctionI f = mFunction;
        if (f == null) {
            synchronized (this) {
                f = mFunction;
                if (f == null) {
                    f = loadFunction();
                    mFunction = f;
                }
            }
        }

        return f;
    }
}
