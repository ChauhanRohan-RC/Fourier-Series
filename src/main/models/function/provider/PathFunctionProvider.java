package main.models.function.provider;

import main.models.function.ComplexDomainFunctionI;
import main.models.function.path.PathFunctionMerger;
import main.util.PathUtil;
import org.apache.batik.parser.ParseException;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class PathFunctionProvider extends AbstractFunctionProvider {

    @NotNull
    private final String[] mPaths;

    public PathFunctionProvider(@NotNull String diplayTitle, @NotNull String... pathData) {
        super(diplayTitle);
        mPaths = pathData;
    }

    @Override
    @NotNull
    protected ComplexDomainFunctionI loadFunction() throws ParseException {
        final Shape shape = PathUtil.parsePathDataStrings(mPaths);
        return PathFunctionMerger.create(shape, 1, true);
    }
}
