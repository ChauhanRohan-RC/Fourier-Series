package main.models.function.provider;

import main.models.function.ComplexDomainFunctionI;
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
    public final String displayTitle;
//    @NotNull
//    private final Task<ComplexDomainFunctionI> mFunctionTask;

    @Nullable
    private volatile ComplexDomainFunctionI mFunction;

    public AbstractFunctionProvider(@NotNull String diplayTitle) {
        this.displayTitle = diplayTitle;
//        mFunctionTask = functionTask;
    }

    @NotNull
    public final String getDisplayTitle() {
        return displayTitle;
    }

    @Override
    public final String toString() {
        return displayTitle;
    }


    @NotNull
    protected abstract ComplexDomainFunctionI loadFunction() throws ParseException;

    @Override
    @NotNull
    public ComplexDomainFunctionI requireFunction() throws ParseException {
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
