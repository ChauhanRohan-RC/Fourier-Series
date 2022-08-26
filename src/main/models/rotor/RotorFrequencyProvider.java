package main.models.rotor;

import org.jetbrains.annotations.NotNull;

public interface RotorFrequencyProvider {

    double getRotorFrequency(int index, int count);





    @NotNull
    RotorFrequencyProvider SIMPLE = RotorFrequencyProvider::simple;

    @NotNull
    RotorFrequencyProvider CENTERING = RotorFrequencyProvider::centering;


    /* ...................  Implementations  .....................*/

    static double simple(int index, int count) {
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
