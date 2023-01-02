package provider;

import function.definition.ComplexDomainFunctionI;
import function.path.PathFunctionMerger;
import org.apache.batik.parser.ParseException;
import org.jetbrains.annotations.NotNull;
import util.main.PathUtil;

import java.awt.*;

public class PathFunctionProvider extends AbstractFunctionProvider {

    @NotNull
    private final String[] mPaths;

    public PathFunctionProvider(@NotNull FunctionMeta meta, @NotNull String... pathData) {
        super(meta);
        mPaths = pathData;
    }

    @Override
    @NotNull
    protected ComplexDomainFunctionI loadFunction() throws ParseException {
        final Shape shape = PathUtil.parsePathDataStrings(mPaths);
        return PathFunctionMerger.create(shape);
    }
}
