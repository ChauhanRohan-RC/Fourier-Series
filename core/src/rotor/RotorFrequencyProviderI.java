package rotor;

import org.jetbrains.annotations.NotNull;

public interface RotorFrequencyProviderI {

    double getRotorFrequency(int index, int count);


    @NotNull
    RotorFrequencyProviderI POSITIVE_INT = RotorFrequencyProviderI::positiveInt;

    @NotNull
    RotorFrequencyProviderI CENTERING_INT = RotorFrequencyProviderI::centering;


    /* ...................  Implementations  .....................*/

    static double positiveInt(int index, int count) {
        return index;
    }

    static double centering(int index, int count) {
        if (count % 2 == 0) {
            count++;
        }

        final int f = (index + 1) - ((count + 1) / 2);          // int frequencies
        return f;
    }

}
