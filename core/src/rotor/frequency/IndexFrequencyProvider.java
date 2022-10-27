package rotor.frequency;

public class IndexFrequencyProvider implements RotorFrequencyProviderI {

    public static final double DEFAULT_INDEX_MULTIPLIER = 1;

    private double indexMultiplier = DEFAULT_INDEX_MULTIPLIER;

    public IndexFrequencyProvider(double indexMultiplier) {
        this.indexMultiplier = indexMultiplier;
    }

    public IndexFrequencyProvider() {
    }


    public double getIndexMultiplier() {
        return indexMultiplier;
    }

    public IndexFrequencyProvider setIndexMultiplier(double indexMultiplier) {
        this.indexMultiplier = indexMultiplier;
        return this;
    }

    @Override
    public double getRotorFrequency(int index, int count) {
        return index * indexMultiplier;
    }

    @Override
    public String toString() {
        return "IndexFrequencyProvider{" +
                "indexMultiplier=" + indexMultiplier +
                '}';
    }
}
