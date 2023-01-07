package particle;

import function.definition.AbstractSignal;
import function.internal.basic.SineSignal;

public class LocalisedParticleSignal extends AbstractSignal {

    @Override
    public double getDomainStart() {
        return 0;
    }

    @Override
    public double getDomainEnd() {
        return 10;
    }

    private final SineSignal sine1 = new SineSignal(0.25, 2, 0, 0, 0.2);
    private final SineSignal sine2 = new SineSignal(0.25, 2, 0, 0, 5);

    @Override
    public double getSignalIntensity(double input) {
        if (input > 10) {
            input %= 10;
        }

        if (input > 5)
            return getSignalIntensity(10 - input);

        if (input <= 1)
            return 0;

        if (input <= 3)
            return sine1.getSignalIntensity(input - 1);

        return sine2.getSignalIntensity(input - 3);
    }

}
