package function;

import function.definition.ColorHandler;
import function.definition.ColorProviderI;
import function.definition.ComplexDomainFunctionI;
import function.definition.DomainAnimationDurationScalerI;
import models.FunctionGraphMode;
import models.RealTransform;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rotor.RotorState;
import rotor.frequency.ExplicitFrequencyProvider;
import rotor.frequency.RotorFrequencyProviderI;

import java.awt.*;

public class ComplexDomainFunctionWrapper implements ComplexDomainFunctionI, ColorHandler, DomainAnimationDurationScalerI {

    @NotNull
    protected final ComplexDomainFunctionI base;

    @Nullable
    protected ColorProviderI colorProviderOverride;

    protected float domainAnimationDurationScale = 1;

    public ComplexDomainFunctionWrapper(@NotNull ComplexDomainFunctionI base) {
        this.base = base;
    }

    @NotNull
    public final ComplexDomainFunctionI getBaseFunction() {
        return base;
    }

    @Override
    public @NotNull Complex compute(double input) {
        return base.compute(input);
    }

    /* Cache Rotor States */

    @Override
    public boolean containsCachedRotorState(double frequency) {
        return base.containsCachedRotorState(frequency);
    }

    @Override
    public @Nullable RotorState getCachedRotorState(double frequency) {
        return base.getCachedRotorState(frequency);
    }


    /* Frequency */

    @Override
    public boolean isFrequencySupported(double frequency) {
        return base.isFrequencySupported(frequency);
    }

    @Override
    public boolean frequenciesExceptExplicitSupported() {
        return base.frequenciesExceptExplicitSupported();
    }

    @Override
    public @Nullable ExplicitFrequencyProvider getExplicitFrequencyProvider() {
        return base.getExplicitFrequencyProvider();
    }

    @Override
    public @Nullable RotorFrequencyProviderI getFunctionDefaultFrequencyProvider() {
        return base.getFunctionDefaultFrequencyProvider();
    }

    @Override
    public @Nullable FunctionGraphMode getDefaultGraphMode() {
        return base.getDefaultGraphMode();
    }

    /* Domain */

    @Override
    public double getDomainStart() {
        return base.getDomainStart();
    }

    @Override
    public double getDomainEnd() {
        return base.getDomainEnd();
    }

    @Override
    public boolean isWithinDomain(double input) {
        return base.isWithinDomain(input);
    }

    @Override
    public double getDomainRange() {
        return base.getDomainRange();
    }

    @Override
    public double getSampleDomainStep(int sampleCount) {
        return base.getSampleDomainStep(sampleCount);
    }

    @Override
    public int getNumericalIntegrationIntervalCount() {
        return base.getNumericalIntegrationIntervalCount();
    }

    @Override
    public long getDomainAnimationDurationMsDefault() {
        return (long) (base.getDomainAnimationDurationMsDefault() * domainAnimationDurationScale);
    }

    @Override
    public long getDomainAnimationDurationMsMin() {
        return (long) (base.getDomainAnimationDurationMsMin() * domainAnimationDurationScale);
    }

    @Override
    public long getDomainAnimationDurationMsMax() {
        return (long) (base.getDomainAnimationDurationMsMax() * domainAnimationDurationScale);
    }

    @Override
    public float getDomainAnimationDurationScale() {
        return domainAnimationDurationScale;
    }

    @Override
    public ComplexDomainFunctionWrapper setDomainAnimationDurationScale(float domainAnimationDurationScale) {
        if (domainAnimationDurationScale > 0) {
            this.domainAnimationDurationScale = domainAnimationDurationScale;
        }

        return this;
    }

    //    @Override
//    public float durationMsToDomainAnimationSpeedFraction(long durationMs) {
//        return base.durationMsToDomainAnimationSpeedFraction(durationMs);
//    }
//
//    @Override
//    public long domainAnimationSpeedFractionToDurationMs(float fraction) {
//        return base.domainAnimationSpeedFractionToDurationMs(fraction);
//    }
    
    /* Sampling */

    @Override
    public double @NotNull [] createSamplesDomain(int sampleCount) {
        return base.createSamplesDomain(sampleCount);
    }

    @Override
    public @NotNull Complex @NotNull [] createSamplesRange(int sampleCount) {
        return base.createSamplesRange(sampleCount);
    }

    @Override
    public @NotNull Complex @NotNull [] createSamplesRange(double @NotNull [] samplesDomain) {
        return base.createSamplesRange(samplesDomain);
    }

    @Override
    public double @NotNull [] createSamplesRealRange(int sampleCount, @NotNull RealTransform realTransform) {
        return base.createSamplesRealRange(sampleCount, realTransform);
    }

    @Override
    public double @NotNull [] createSamplesRealRange(double @NotNull [] samplesDomain, @NotNull RealTransform realTransform) {
        return base.createSamplesRealRange(samplesDomain, realTransform);
    }

    /* Colors */

    public ComplexDomainFunctionWrapper setColorProviderOverride(@Nullable ColorProviderI colorProviderOverride) {
        this.colorProviderOverride = colorProviderOverride;
        return this;
    }

    @Nullable
    public ColorProviderI getColorProviderOverride() {
        return colorProviderOverride;
    }


    @Override
    public ComplexDomainFunctionWrapper setColorProvider(@Nullable ColorProviderI colorProvider) {
        if (base instanceof ColorHandler) {
            ((ColorHandler) base).setColorProvider(colorProvider);
        } else {
            setColorProviderOverride(colorProvider);
        }

        return this;
    }

    @Override
    public @Nullable ColorProviderI getColorProvider() {
        if (colorProviderOverride != null) {
            return colorProviderOverride;
        }

        if (base instanceof ColorHandler) {
            return ((ColorHandler) base).getColorProvider();
        }

        return null;
    }

    @Override
    public ComplexDomainFunctionWrapper hueCycle(float hueStart, float hueEnd) {
        if (base instanceof ColorHandler) {
            ((ColorHandler) base).hueCycle(hueStart, hueEnd);
        } else {
            setColorProviderOverride(new HueCycle(base, hueStart, hueEnd));
        }

        return this;
    }

    @Override
    public ColorHandler transparent() {
        return ColorHandler.super.transparent();
    }

    @Override
    public ColorHandler hueCycle() {
        return ColorHandler.super.hueCycle();
    }

    @Override
    @Nullable
    public Color getColor(double input) {
        return colorProviderOverride != null? colorProviderOverride.getColor(input): base.getColor(input);
    }


    /* Object */

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (o instanceof ComplexDomainFunctionWrapper wrapper) {
            return base.equals(wrapper.base);
        }

        if (o instanceof ComplexDomainFunctionI func) {
            return base.equals(func);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "Wrapper(" + base + ")";
    }

}
