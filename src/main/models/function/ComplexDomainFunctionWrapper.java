package main.models.function;

import main.models.ColorHandler;
import main.models.function.provider.ColorProviderI;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class ComplexDomainFunctionWrapper implements ComplexDomainFunctionI, ColorHandler {

    @NotNull
    protected final ComplexDomainFunctionI base;

    @Nullable
    protected ColorProviderI colorProviderOverride;

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
        return base instanceof ColorHandler? ((ColorHandler) base).getColorProvider(): getColorProviderOverride();
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
    @Nullable
    public Color getColor(double input) {
        return colorProviderOverride != null? colorProviderOverride.getColor(input): base.getColor(input);
    }

    @Override
    public double getDomainStart() {
        return base.getDomainStart();
    }

    @Override
    public double getDomainEnd() {
        return base.getDomainEnd();
    }

    @Override
    public double getDomainRange() {
        return base.getDomainRange();
    }

    @Override
    public double getDomainRangeTravelMsDefault() {
        return base.getDomainRangeTravelMsDefault();
    }

    @Override
    public double getDomainRangeTravelMsMin() {
        return base.getDomainRangeTravelMsMin();
    }

    @Override
    public double getDomainRangeTravelMsMax() {
        return base.getDomainRangeTravelMsMax();
    }
}
