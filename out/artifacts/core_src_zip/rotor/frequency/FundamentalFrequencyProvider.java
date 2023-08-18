package rotor.frequency;

import function.definition.DomainProviderI;

import java.util.Objects;

public final class FundamentalFrequencyProvider implements RotorFrequencyProviderI {

    public static final boolean DEFAULT_CENTERING = true;

    private final double domainRange;
    private final double fundamentalFrequency;

    private volatile boolean centering = DEFAULT_CENTERING;

    public FundamentalFrequencyProvider(double domainRange) {
        this.domainRange = domainRange;
        this.fundamentalFrequency = DomainProviderI.getFundamentalFrequency(domainRange);
    }

    public double getDomainRange() {
        return domainRange;
    }

    public double getFundamentalFrequency() {
        return fundamentalFrequency;
    }

    public FundamentalFrequencyProvider setCentering(boolean centering) {
        this.centering = centering;
        return this;
    }

    public boolean isCentering() {
        return centering;
    }

    @Override
    public double getRotorFrequency(int index, int count) {
        if (!centering) {
            return fundamentalFrequency * index;
        }

        if (count % 2 == 0) {
            count++;
        }

        final int f = (index + 1) - ((count + 1) / 2);          // int frequencies
        return f * fundamentalFrequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass()) return false;
        final FundamentalFrequencyProvider that = (FundamentalFrequencyProvider) o;
        return domainRange == that.domainRange && fundamentalFrequency == that.fundamentalFrequency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(domainRange, fundamentalFrequency);
    }

    @Override
    public String toString() {
        return "FundamentalFrequencyProvider{" +
                "domainRange=" + domainRange +
                ", fundamentalFrequency=" + fundamentalFrequency +
                '}';
    }
}
