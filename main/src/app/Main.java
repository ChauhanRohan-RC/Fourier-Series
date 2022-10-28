package app;

import provider.Providers;
import rotor.frequency.BoundedFrequencyProvider;
import rotor.frequency.CenteringFrequencyProvider;
import rotor.frequency.RotorFrequencyProviderI;
import ui.FourierUi;
import ui.FrequencyProviderSelectorPanel;
import util.Log;
import util.async.Async;
import util.main.ComplexUtil;

public class Main {

    public static final String TAG = "Main";

//    private static void setupFourierUi(int initialFuncProviderIndex, boolean preload) {
////        if (preload) {
////            stateManager.loadSync(stateManager.getDefaultInitialRotorCount(), null);
////        }

//        Async.postIfNotOnMainThread(() -> {
//            final FourierUi ui = new FourierUi(null, initialFuncProviderIndex);
//        });
//    }

    // TODO: Main production launcher

    private static void launchMain(String[] args) {
        R.init();

        Async.postIfNotOnMainThread(() -> {
            final FourierUi ui = new FourierUi(null, -1);
            ui.setFunctionProvider(Providers.NoopProvider.getSingleton());          // start with None
        });
    }


    // TODO: Test launcher

    private static void launchTest(String[] args) {
        R.init();

        final FrequencyProviderSelectorPanel panel = new FrequencyProviderSelectorPanel(
                new BoundedFrequencyProvider(4d, 10d),
                new CenteringFrequencyProvider(ComplexUtil.TWo_PI)
        );

        final RotorFrequencyProviderI fp = panel.showDialog(null);
        Log.d(TAG, "Provider: " + fp);
    }

    public static void main(String[] args) {
//        launchTest(args);
        launchMain(args);
    }

}
