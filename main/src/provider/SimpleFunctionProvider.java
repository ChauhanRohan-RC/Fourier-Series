package provider;

import function.definition.ComplexDomainFunctionI;
import org.apache.batik.parser.ParseException;
import org.jetbrains.annotations.NotNull;

public class SimpleFunctionProvider extends AbstractFunctionProvider {

    @NotNull
    private final ComplexDomainFunctionI function;

    public SimpleFunctionProvider(@NotNull FunctionMeta meta, @NotNull ComplexDomainFunctionI function) {
        super(meta);
        this.function = function;
    }

    @Override
    @NotNull
    protected ComplexDomainFunctionI loadFunction() throws ParseException {
        return function;
    }

    @Override
    public @NotNull ComplexDomainFunctionI getFunction() {
        return function;
    }
}
