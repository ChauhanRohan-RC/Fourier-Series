package function;

import async.BiConsumer;
import function.definition.ComplexDomainFunctionI;
import misc.CollectionUtil;
import misc.MathUtil;
import models.ComplexBuilder;
import models.FunctionGraphMode;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import rotor.RotorState;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import rotor.frequency.ExplicitFrequencyProvider;
import rotor.frequency.RotorFrequencyProviderI;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that synthesize a function from Rotor States of a different function<br>
 *<br>
 * If provided, delegates to base function for every call,
 * otherwise it uses given Rotor States to synthesize a function (Inverse Fourier Series)
 * */
public class RotorStatesFunction implements ComplexDomainFunctionI {

    public enum ComputeMode {
        REAL_COS("Cos (Real)", (sum, c) -> sum.addReal(c.getReal())),
        REAL_SINE("Sine (Real)", (sum, c) -> sum.addReal(c.getImaginary())),
        IMG_COS("Cos (imaginary)", (sum, c) -> sum.addImaginary(c.getReal())),
        IMG_SINE("Sine (imaginary)", (sum, c) -> sum.addImaginary(c.getImaginary())),
        COMPLEX("Cos (Real) + Sine (Img)", ComplexBuilder::add);

        @NotNull
        public final String displayName;
        @NotNull
        public final BiConsumer<ComplexBuilder, Complex> adder;

        ComputeMode(@NotNull String displayName, @NotNull BiConsumer<ComplexBuilder, Complex> adder) {
            this.displayName = displayName;
            this.adder = adder;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }



    public static final boolean DEFAULT_OTHER_FREQUENCIES_SUPPOrTED = false;
    public static final double DEFAULT_DOMAIN_START = 0;
    public static final double DEFAULT_DOMAIN_END = MathUtil.TWO_PI;
    @NotNull
    public static final ComputeMode DEFAULT_COMPUTE_MODE = ComputeMode.COMPLEX;

    @NotNull
    @Unmodifiable
    private static Map<Double, RotorState> toUnmodifiableMap(Collection<RotorState> states) {
        if (CollectionUtil.isEmpty(states))
            return Collections.emptyMap();

        final Map<Double, RotorState> map = new HashMap<>();
        states.forEach(rs -> map.put(rs.getFrequency(), rs));

        return Collections.unmodifiableMap(map);
    }




    @Nullable
    private final ComplexDomainFunctionI function;
    @NotNull
    @Unmodifiable
    private final Map<Double, RotorState> states;

    /* ....................  Defaults  ................... */
    private final double defaultDomainStart;
    private final double defaultDomainEnd;
    private final int defaultNumericalIntegrationIntervalCount;

    private final long defaultDomainAnimMsDefault;
    private final long defaultDomainAnimMsMin;
    private final long defaultDomainAnimMsMax;

    @NotNull
    private volatile ComputeMode mComputeMode = DEFAULT_COMPUTE_MODE;

    /**
     * Determines whether frequencies other than stored are supported, in case of no {@link #function base function}
     * */
    private boolean mFrequenciesExceptExplicitSupported = DEFAULT_OTHER_FREQUENCIES_SUPPOrTED;

    @Nullable
    private final RotorFrequencyProviderI defaultFrequencyProvider;

    @Nullable
    private volatile ExplicitFrequencyProvider explicitFrequencyProvider;

    public RotorStatesFunction(@Nullable ComplexDomainFunctionI function,
                               @NotNull Collection<RotorState> states,
                               double defaultDomainStart,
                               double defaultDomainEnd,
                               int defaultNumericalIntegrationIntervalCount,
                               long defaultDomainAnimMsDefault,
                               long defaultDomainAnimMsMin,
                               long defaultDomainAnimMsMax,
                               @Nullable RotorFrequencyProviderI defaultFrequencyProvider) {

        this.function = function;
        this.states = toUnmodifiableMap(states);
        this.defaultDomainStart = defaultDomainStart;
        this.defaultDomainEnd = defaultDomainEnd;
        this.defaultNumericalIntegrationIntervalCount = defaultNumericalIntegrationIntervalCount;
        this.defaultDomainAnimMsDefault = defaultDomainAnimMsDefault;
        this.defaultDomainAnimMsMin = defaultDomainAnimMsMin;
        this.defaultDomainAnimMsMax = defaultDomainAnimMsMax;
        this.defaultFrequencyProvider = defaultFrequencyProvider;
    }

    public RotorStatesFunction(@Nullable Double domainStart,
                               @Nullable Double domainEnd,
                               @NotNull Collection<RotorState> states) {
        this(null,
                states,
                domainStart != null? domainStart: DEFAULT_DOMAIN_START,
                domainEnd != null? domainEnd: DEFAULT_DOMAIN_END,
                -1,
                -1,
                -1,
                -1,
                null);
    }

    @Nullable
    public ComplexDomainFunctionI getBaseFunction() {
        return function;
    }

    public boolean hasBaseFunction() {
        return function != null;
    }

    @Override
    public double getDomainStart() {
        return function != null? function.getDomainStart(): defaultDomainStart;
    }

    @Override
    public double getDomainEnd() {
        return function != null? function.getDomainEnd(): defaultDomainEnd;
    }

    @Override
    public int getNumericalIntegrationIntervalCount() {
        return function != null? function.getNumericalIntegrationIntervalCount(): defaultNumericalIntegrationIntervalCount;
    }

    @Override
    public @NotNull Complex compute(double input) {
        if (function != null) {
            return function.compute(input);
        }

        final ComputeMode computeMode = mComputeMode;
        final ComplexBuilder r = new ComplexBuilder(0, 0);

        states.values().forEach(s -> computeMode.adder.consume(r, s.getTip(input)));
        return r.toComplex();
    }


    @NotNull
    public ComputeMode getComputeMode() {
        return mComputeMode;
    }

    public RotorStatesFunction setComputeMode(@NotNull ComputeMode computeMode) {
        mComputeMode = computeMode;
        return this;
    }


    @Override
    public int getInitialRotorCount() {
        if (function != null) {
            return function.getInitialRotorCount();
        }

        return mFrequenciesExceptExplicitSupported? ComplexDomainFunctionI.super.getInitialRotorCount(): states.size();
    }

    @Override
    public boolean containsCachedRotorState(double frequency) {
        return states.containsKey(frequency);
    }

    @Override
    public @Nullable RotorState getCachedRotorState(double frequency) {
        return states.get(frequency);
    }

    @Override
    public boolean isFrequencySupported(double frequency) {
        if (function != null) {
            return function.isFrequencySupported(frequency);
        }

        return mFrequenciesExceptExplicitSupported || containsCachedRotorState(frequency);
//        return containsCachedRotorState(frequency);        // since no other frequency will ever make it to the end
    }

    @Override
    public boolean frequenciesExceptExplicitSupported() {
        if (function != null) {
            return function.frequenciesExceptExplicitSupported();
        }

        return mFrequenciesExceptExplicitSupported;
    }

    /**
     * Determines whether frequencies other than stored are supported, in case of no {@link #function base definition}
     *
     * @see #containsCachedRotorState(double)
     * @see #isFrequencySupported(double)
     * */
    public final RotorStatesFunction setFrequenciesExceptExplicitSupported(boolean otherFrequenciesSupported) {
        mFrequenciesExceptExplicitSupported = otherFrequenciesSupported;
        return this;
    }

    @Nullable
    public final ExplicitFrequencyProvider getExplicitFrequencyProvider() {
        if (function != null) {
            return function.getExplicitFrequencyProvider();
        }

        ExplicitFrequencyProvider fp = explicitFrequencyProvider;
        if (fp == null) {
            synchronized (this) {
                fp = explicitFrequencyProvider;
                if (fp == null) {
                    fp = new ExplicitFrequencyProvider(true, states.keySet());
                    explicitFrequencyProvider = fp;
                }
            }
        }

        return fp;
    }

    @Override
    @Nullable
    public final RotorFrequencyProviderI getFunctionDefaultFrequencyProvider() {
        if (function != null) {
            return function.getFunctionDefaultFrequencyProvider();
        }

        if (mFrequenciesExceptExplicitSupported)
            return defaultFrequencyProvider;

        return getExplicitFrequencyProvider();
    }


    @Override
    public @Nullable FunctionGraphMode getDefaultGraphMode() {
        return function != null? function.getDefaultGraphMode(): ComplexDomainFunctionI.super.getDefaultGraphMode();
    }

    @Override
    public long getDomainAnimationDurationMsDefault() {
        if (function != null) {
            return function.getDomainAnimationDurationMsDefault();
        }

        return defaultDomainAnimMsDefault > 0? defaultDomainAnimMsDefault: ComplexDomainFunctionI.super.getDomainAnimationDurationMsDefault();
    }

    @Override
    public long getDomainAnimationDurationMsMin() {
        if (function != null) {
            return function.getDomainAnimationDurationMsMin();
        }

        return defaultDomainAnimMsMin > 0? defaultDomainAnimMsMin: ComplexDomainFunctionI.super.getDomainAnimationDurationMsMin();
    }

    @Override
    public long getDomainAnimationDurationMsMax() {
        if (function != null) {
            return function.getDomainAnimationDurationMsMax();
        }

        return defaultDomainAnimMsMax > 0? defaultDomainAnimMsMax: ComplexDomainFunctionI.super.getDomainAnimationDurationMsMax();
    }
}
