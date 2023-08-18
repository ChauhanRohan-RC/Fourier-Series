package function.definition;

import models.FunctionGraphMode;
import models.RealTransform;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rotor.RotorState;
import rotor.frequency.ExplicitFrequencyProvider;
import rotor.frequency.FundamentalFrequencyProvider;
import rotor.frequency.RotorFrequencyProviderI;
import util.json.JsonParsable;

public interface ComplexDomainFunctionI extends ComplexFunctionI, DomainProviderI, FrequencySupportProviderI, CacheRotorStateProvider, JsonParsable {

    ComplexDomainFunctionI NOOP = new ComplexDomainFunctionI() {
        @Override
        public double getDomainStart() {
            return 0;
        }

        @Override
        public double getDomainEnd() {
            return 0;
        }

        @Override
        public @NotNull Complex compute(double input) {
            return Complex.ZERO;
        }
    };


    default boolean isNoop() {
        return this == NOOP;
    }


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
    static RotorFrequencyProviderI getDefaultFrequencyProvider(double domainRange) {
//        final CenteringFrequencyProvider fp = new CenteringFrequencyProvider();
//        double multiplier = domainRange != 0? 1 / domainRange: 1;
//
//        if (!ComplexUtil.FOURIER_TRANSFORM_USE_TWO_PI) {
//            multiplier *= MathUtil.TWO_PI;
//        }
//
//        fp.setFrequencyMultiplier(multiplier);
//        return fp;

        return new FundamentalFrequencyProvider(domainRange);
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
