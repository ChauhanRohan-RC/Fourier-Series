package rotor.frequency;

import util.json.JsonParsable;

public interface RotorFrequencyProviderI extends JsonParsable {

    /**
     * Internal logic-based Frequency of a rotor (without any post processing)
     *
     * @param index rotor index
     * @param count total rotor count
     *
     * @return Frequency of a rotor, as computed internally (without any post processing)
     * */
    double getRotorFrequency(int index, int count);


    String toString();

    boolean equals(Object o);

    int hashCode();


//    /**
//     * Internal logic-based Frequency of a rotor (without any post processing)
//     *
//     * @param index rotor index
//     * @param count total rotor count
//     *
//     * @return Frequency of a rotor, as computed internally (without any post processing)
//     * */
//    double getInternalRotorFrequency(int index, int count);
//
//
//    /* .............................. POST-PROCESSING ............................. */
//    /**
//     * POST-PROCESSING FILTER
//     * A constant multiplier with which internally computed frequencies will be multiplied
//     * at post-processing stage
//     *
//     * @return constant multiplier with which internally computed frequencies will be multiplied
//     * */
//    double getFrequencyMultiplier();
//
//    /**
//     * POST-PROCESSING FILTER
//     * A constant multiplier with which internally computed frequencies will be multiplied
//     * at post-processing stage
//     *
//     * @param frequencyMultiplier constant multiplier with which internally computed frequencies will be multiplied
//     * */
//    void setFrequencyMultiplier(double frequencyMultiplier);
//
//
//    /**
//     * Transforms internally computed frequency i.e apply post-processing transformations
//     *
//     * @param internalFrequency internally computed frequency
//     * @return transformed frequency
//     * */
//    double transformInternalFrequency(double internalFrequency);
//
//    /**
//     * FINAL frequency of the rotor after post-processing transformations
//     *
//     * @param index rotor index
//     * @param count total rotor count
//     * @return final frequency of the rotor after post processing
//     * */
//    double getFinalRotorFrequency(int index, int count);

}
