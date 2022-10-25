package rotor;

import org.jetbrains.annotations.NotNull;

public enum RotorFrequencyProviderE implements RotorFrequencyProviderI {

    POSITIVE_INT(0, RotorFrequencyProviderI.POSITIVE_INT),

    CENTERING_INT(1, RotorFrequencyProviderI.CENTERING_INT),
    ;


    private final int id;
    @NotNull
    private final RotorFrequencyProviderI frequencyProvider;

    RotorFrequencyProviderE(int id, @NotNull RotorFrequencyProviderI frequencyProvider) {
        this.id = id;
        this.frequencyProvider = frequencyProvider;
    }

    public int getId() {
        return id;
    }

    @Override
    public double getRotorFrequency(int index, int count) {
        return frequencyProvider.getRotorFrequency(index, count);
    }
}
