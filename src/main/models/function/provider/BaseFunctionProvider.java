package main.models.function.provider;

import main.models.function.ComplexDomainFunctionI;
import main.util.async.Task;
import org.apache.batik.parser.ParseException;
import org.jetbrains.annotations.NotNull;

public class BaseFunctionProvider extends AbstractFunctionProvider {

    @NotNull
    private final Task<ComplexDomainFunctionI> mFunctionTask;

    public BaseFunctionProvider(@NotNull String diplayTitle, @NotNull Task<ComplexDomainFunctionI> functionTask) {
        super(diplayTitle);
        mFunctionTask = functionTask;
    }

    @Override
    @NotNull
    protected ComplexDomainFunctionI loadFunction() throws ParseException {
        return mFunctionTask.begin();
    }
}
