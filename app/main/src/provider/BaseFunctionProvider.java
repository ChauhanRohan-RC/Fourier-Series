package provider;

import function.definition.ComplexDomainFunctionI;
import org.apache.batik.parser.ParseException;
import org.jetbrains.annotations.NotNull;
import async.Task;

public class BaseFunctionProvider extends AbstractFunctionProvider {

    @NotNull
    private final Task<ComplexDomainFunctionI> mFunctionTask;

    public BaseFunctionProvider(@NotNull FunctionMeta meta, @NotNull Task<ComplexDomainFunctionI> functionTask) {
        super(meta);
        mFunctionTask = functionTask;
    }

    @Override
    @NotNull
    protected ComplexDomainFunctionI loadFunction() throws ParseException {
        return mFunctionTask.begin();
    }
}
