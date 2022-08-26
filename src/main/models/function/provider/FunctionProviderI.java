package main.models.function.provider;

import main.models.function.ComplexDomainFunctionI;
import main.util.Log;
import org.apache.batik.parser.ParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FunctionProviderI {

    String TAG = "FunctionProviderI";


    @NotNull
    String getDisplayTitle();

    @NotNull
    ComplexDomainFunctionI requireFunction() throws ParseException, Providers.NoOpProviderException;

    @Nullable
    default ComplexDomainFunctionI getFunction() {
        try {
            return requireFunction();
        } catch (Throwable t) {
            Log.e(TAG, "Failed to create function: " + getDisplayTitle(), t);
        }

        return null;
    }


    /**
     * Should return display title
     *  */
    String toString();
}
