package function.definition;

import org.jetbrains.annotations.Nullable;
import rotor.frequency.ExplicitFrequencyProvider;

public interface FrequencySupportProviderI {

    /**
     * Determines whether a frequency is supported i.e. if Fourier Transform at the given frequency makes sense<br>
     * At unsupported frequencies, Fourier Transform should be {@link org.apache.commons.math3.complex.Complex#ZERO ZERO}
     *<br><br>
     * @param frequency the temporal frequency (in Hz)
     * @return if the frequency is supported
     * */
    boolean isFrequencySupported(double frequency);


    /**
     * @return An {@link ExplicitFrequencyProvider} containing explicit internally sensible frequencies, if defined,
     * or {@code null} otherwise
     * */
    @Nullable
    ExplicitFrequencyProvider getExplicitFrequencyProvider();


    /**
     * General contract of this is that<br>
     * <br>
     * <p>
     *     1. if explicit frequencies are NOT defined i.e. {@link #getExplicitFrequencyProvider()} returns {@code null} than this should return true<br>
     *     2. if explicit frequencies are defined<br>
     *     <p>
     *         1. returning true means it also supports other frequencies<br>
     *         2. returning false means it does not support other frequencies, and Fourier Transform at other frequencies should be {@link org.apache.commons.math3.complex.Complex#ZERO ZERO}
     *     </p>
     * </p>
     *
     * @return whether frequencies other than those contained in {@link #getExplicitFrequencyProvider()} are supported
     * */
    boolean frequenciesExceptExplicitSupported();


}
