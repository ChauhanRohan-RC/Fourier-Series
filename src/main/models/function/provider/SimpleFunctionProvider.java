package main.models.function.provider;

import main.models.function.ComplexDomainFunctionI;
import org.apache.batik.parser.ParseException;
import org.jetbrains.annotations.NotNull;

public class SimpleFunctionProvider extends AbstractFunctionProvider {

    @NotNull
    private final ComplexDomainFunctionI mFunction;

    public SimpleFunctionProvider(@NotNull String diplayTitle, @NotNull ComplexDomainFunctionI function) {
        super(diplayTitle);
        mFunction = function;
    }

    @Override
    @NotNull
    protected ComplexDomainFunctionI loadFunction() throws ParseException {
        return mFunction;
    }

    @Override
    public @NotNull ComplexDomainFunctionI requireFunction() throws ParseException {
        return mFunction;
    }

    @Override
    public @NotNull ComplexDomainFunctionI getFunction() {
        return mFunction;
    }
}
