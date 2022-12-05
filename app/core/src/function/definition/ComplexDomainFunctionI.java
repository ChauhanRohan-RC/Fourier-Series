package function.definition;

import models.RealTransform;
import models.FunctionGraphMode;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rotor.RotorState;
import rotor.frequency.CenteringFrequencyProvider;
import rotor.frequency.ExplicitFrequencyProvider;
import rotor.frequency.RotorFrequencyProviderI;
import util.json.JsonParsable;
import util.main.ComplexUtil;

public interface ComplexDomainFunctionI extends ComplexFunctionI, DomainProviderI, FrequencySupportProviderI, CacheRotorStateProvider, JsonParsable {

    /* ............................. Sampling ....................... */

    default double @NotNull[] createSamplesDomain(int sampleCount) {
        if (sampleCount < 1)
            return new double[0];

        if (sampleCount == 1)
            return new double[] { getDomainStart() };

        final double start = getDomainStart();
        final double step = getSampleDomainStep(sampleCount);

        final double[] arr = new double[sampleCount];
        for (int i=0; i < sampleCount; i++) {
            arr[i] = start + (i * step);
        }

        return arr;
    }

    @NotNull
    default Complex @NotNull[] createSamplesRange(int sampleCount) {
        if (sampleCount < 1)
            return new Complex[0];

        final double start = getDomainStart();
        if (sampleCount == 1)
            return new Complex[] { compute(start) };

        final double step = getSampleDomainStep(sampleCount);

        final Complex[] range = new Complex[sampleCount];
        for (int i=0; i < sampleCount; i++) {
            range[i] = compute(start + (i * step));
        }

        return range;
    }

    @NotNull
    default Complex @NotNull[] createSamplesRange(double @NotNull[] samplesDomain) {
        if (samplesDomain.length == 0) {
            return new Complex[0];
        }

        final Complex[] range = new Complex[samplesDomain.length];
        for (int i=0; i < samplesDomain.length; i++) {
            range[i] = compute(samplesDomain[i]);
        }

        return range;
    }


    default double @NotNull[] createSamplesRealRange(int sampleCount, @NotNull RealTransform realTransform) {
        if (sampleCount < 1)
            return new double[0];

        final double start = getDomainStart();
        if (sampleCount == 1)
            return new double[] { realTransform.toReal(compute(start)) };

        final double step = getSampleDomainStep(sampleCount);

        final double[] range = new double[sampleCount];
        for (int i=0; i < sampleCount; i++) {
            range[i] = realTransform.toReal(compute(start + (i * step)));
        }

        return range;
    }

    default double @NotNull[] createSamplesRealRange(double @NotNull[] samplesDomain, @NotNull RealTransform realTransform) {
        if (samplesDomain.length == 0) {
            return new double[0];
        }

        final double[] realRange = new double[samplesDomain.length];
        for (int i=0; i < samplesDomain.length; i++) {
            realRange[i] = realTransform.toReal(compute(samplesDomain[i]));
        }

        return realRange;
    }


    /* .................... Cache Rotor States .................... */

    @Override
    default boolean containsCachedRotorState(double frequency) {
        return false;
    }

    @Nullable
    @Override
    default RotorState getCachedRotorState(double frequency) {
        return null;
    }


    /* .......................... Frequency .................. */
    /**
     * {@inheritDoc}
     * */
    @Override
    default boolean isFrequencySupported(double frequency) {
        return true;
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

    @Nullable
    default RotorFrequencyProviderI getFunctionDefaultFrequencyProvider() {
        return getDefaultFrequencyProvider(getDomainRange());
    }

    @Override
    @Nullable
    default ExplicitFrequencyProvider getExplicitFrequencyProvider() {
        return null;
    }

    @Override
    default boolean frequenciesExceptExplicitSupported() {
        return true;
    }


    /* ........................... Misc ........................... */

    /**
     * @return the default {@link FunctionGraphMode GraphMode}
     * */
    @Nullable
    default FunctionGraphMode getDefaultGraphMode() {
        return FunctionGraphMode.OUTPUT_SPACE;
    }


}
