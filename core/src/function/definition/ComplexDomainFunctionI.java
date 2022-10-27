package function.definition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rotor.frequency.CenteringFrequencyProvider;
import rotor.frequency.RotorFrequencyProviderI;
import util.json.JsonParsable;
import util.main.ComplexUtil;

public interface ComplexDomainFunctionI extends ComplexFunctionI, DomainProviderI, JsonParsable {

    @Nullable
    default RotorFrequencyProviderI getFunctionDefaultFrequencyProvider() {
        return getDefaultFrequencyProvider(getDomainRange());
    }

    @NotNull
    static RotorFrequencyProviderI getDefaultFrequencyProvider(double divisor) {
        final CenteringFrequencyProvider fp = new CenteringFrequencyProvider();
        double multiplier = divisor != 0? 1 / divisor: 1;

        if (!ComplexUtil.FOURIER_TRANSFORM_USE_TWO_PI) {
            multiplier *= ComplexUtil.TWo_PI;
        }

        fp.setFrequencyMultiplier(multiplier);
        return fp;
    }

}
