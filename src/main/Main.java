package main;

import main.models.function.provider.Providers;
import main.ui.FourierUi;
import main.util.async.Async;

public class Main {

    public static final String TAG = "Main";


//    private static void setupFourierUi(int initialFuncProviderIndex, boolean preload) {
////        if (preload) {
////            stateManager.loadSync(stateManager.getDefaultInitialRotorCount(), null);
////        }
//
//        Async.postIfNotOnMainThread(() -> {
//            final FourierUi ui = new FourierUi(null, initialFuncProviderIndex);
//        });
//    }



    // TODO: Main production launcher

    private static void launchMain(String[] args) {
//        ComplexDomainFunctionI function = null;
//        Runnable postAction = null;
//
//        try {
//            Log.d(TAG, "Loading function from " + R.PATH_FILE + " ...");
//            final Shape shape = PathUtil.parsePathDataFIle(R.PATH_FILE);
//            final PathFunctionMerger func = PathFunctionMerger.create(shape, 1, true);
//            if (R.EXTERNAL_FUNCTION_HUE_CYCLE) {
//                func.hueCycle();
//            }
//
//            function = func;
//        } catch (FileNotFoundException f_exc) {
//            Log.e(TAG, R.PATH_FILE + " File Not Found", f_exc);
//            postAction = () -> JOptionPane.showMessageDialog(null, "Path data file \"" + R.PATH_FILE + "\" not found!", Ui.MAIN_TITLE, JOptionPane.ERROR_MESSAGE);
//        } catch (ParseException p_exc) {
//            Log.e(TAG, R.PATH_FILE + " Parsing failed", p_exc);
//            postAction = () -> JOptionPane.showMessageDialog(null, "Failed to parse path from \"" + R.PATH_FILE + "\"\nError code: " + p_exc.getMessage(), Ui.MAIN_TITLE, JOptionPane.ERROR_MESSAGE);
//        } catch (Throwable exc) {
//            Log.e(TAG, "Unknown Error while parsing " + R.PATH_FILE, exc);
//            postAction = () -> JOptionPane.showMessageDialog(null, "Unexpected Error: " + exc.getMessage(), Ui.MAIN_TITLE, JOptionPane.ERROR_MESSAGE);
//        }
//
//        if (function == null) {
//            function = R.getFallbackFunction();
//            if (function == null) {
//                function = R.RC_CHARS;
//            }
//        }
//
//        setupFourierUi(new StandardRotorStateManager(function), true);
//        if (postAction != null) {
//            EventQueue.invokeLater(postAction);
//        }

        Async.postIfNotOnMainThread(() -> {
            final FourierUi ui = new FourierUi(null, -1);
            ui.setFunctionProvider(Providers.NoopProvider.getSingleton());          // start with None
        });
    }


    // TODO: Test launcher

    private static void launchTest(String[] args) {
        FourierUi ui = new FourierUi();

//        Log.v(Arrays.toString("asd|asd|a".split("[|]")));
    }


    public static void main(String[] args) {
//        launchTest(args);
        launchMain(args);
    }

}
