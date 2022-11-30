package provider;

import function.definition.ComplexDomainFunctionI;
import misc.Log;
import org.apache.batik.parser.ParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public interface FunctionProviderI {

    String TAG = "FunctionProviderI";

    @NotNull
    FunctionMeta getFunctionMeta();

    @NotNull
    ComplexDomainFunctionI requireFunction() throws ParseException, Providers.NoOpProviderException;

    @Nullable
    default ComplexDomainFunctionI getFunction() {
        try {
            return requireFunction();
        } catch (Throwable t) {
            Log.e(TAG, "Failed to create function: " + getFunctionMeta().displayName(), t);
        }

        return null;
    }


    /**
     * Should return display title
     *  */
    String toString();


    @NotNull
    static Predicate<FunctionProviderI> forType(@NotNull FunctionType type) {
        return fp -> fp.getFunctionMeta().functionType() == type;
    }

    @NotNull
    static Predicate<FunctionProviderI> forTypes(@NotNull FunctionType type, FunctionType @Nullable ... others) {
        Predicate<FunctionProviderI> filter = forType(type);

        if (others != null && others.length > 0) {
            for (FunctionType ft: others){
                filter = filter.or(forType(ft));
            }
        }

        return filter;
    }
}
