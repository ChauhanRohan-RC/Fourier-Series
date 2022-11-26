package ui.util;

import app.R;
import app.Settings;
import function.definition.ComplexDomainFunctionI;
import models.Wrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import provider.FunctionMeta;
import provider.FunctionProviderI;
import provider.FunctionType;
import provider.SimpleFunctionProvider;
import rotor.FunctionState;
import rotor.RotorStateManager;
import rotor.frequency.RotorFrequencyProviderI;
import ui.action.ActionInfo;
import ui.frames.FTUi;
import ui.frames.FourierUi;
import ui.panels.ExternalProgramPanel;
import ui.panels.ExternalRotorStatesLoadPanel;
import ui.panels.FrequencyProviderSelectorPanel;
import util.*;
import util.async.*;
import util.main.ComplexUtil;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.*;

public interface Ui {

    Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();

    String TITLE_MAIN = "Fourier Series";
    String TITLE_FT = "Fourier Transform";

    String TITLE_CONFIGURATIONS = "Configuration";

    static String getWindowTitle(@NotNull String mainTitle, @NotNull RotorStateManager sm) {
        String title = mainTitle;
        if (!sm.isNoOp()) {
            title += " (" + (sm.isLoading()? "Loading ": "") + Format.ellipse(sm.getFunctionMeta().displayName(), 40) + ")";
        }

        return title;
    }


    int DEFAULT_LOOPER_DELAY_MS = 10;

    @NotNull
    static Rectangle windowBoundsCenterScreen(int width, int height) {
        return new Rectangle((SCREEN_SIZE.width - width) / 2, (SCREEN_SIZE.height - height) / 2, width, height);
    }

    @NotNull
    static ActionListener actionListener(@NotNull Runnable run) {
        return e -> run.run();
    }

    @NotNull
    static Timer createLooper(@Nullable Runnable action, int delayMs) {
        final ActionListener l = action != null? actionListener(action): null;
        final Timer timer = new Timer(delayMs, l);
        timer.setRepeats(true);
        return timer;
    }

    @NotNull
    static Timer createLooper(@Nullable Runnable action) {
        return createLooper(action, DEFAULT_LOOPER_DELAY_MS);
    }

    static void extractDialog(@NotNull Component component, @NotNull Consumer<Dialog> dialogConsumer, boolean oneShot) {
        component.addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                final Window window = SwingUtilities.getWindowAncestor(component);
                if (window instanceof final Dialog dialog) {
                    dialogConsumer.consume(dialog);
                    if (oneShot) {
                        component.removeHierarchyListener(this);
                    }
                }
            }
        });
    }

    /* Abstract */

    @Nullable
    JFrame getFrame();

    /* .......................................  Message Dialog  ............................. */

    static void showMessageDialog(@Nullable Component parent, @NotNull Object msg, @Nullable String title, int type) {
        JOptionPane.showMessageDialog(parent, msg, Format.isEmpty(title)? TITLE_MAIN : title, type);
    }

    static void showPlainMessageDialog(@Nullable Component parent, @NotNull Object msg, @Nullable String title) {
        showMessageDialog(parent, msg, title, JOptionPane.PLAIN_MESSAGE);
    }

    static void showInfoMessageDialog(@Nullable Component parent, @NotNull Object msg, @Nullable String title) {
        showMessageDialog(parent, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    static void showWarnMessageDialog(@Nullable Component parent, @NotNull Object msg, @Nullable String title) {
        showMessageDialog(parent, msg, title, JOptionPane.WARNING_MESSAGE);
    }

    static void showErrorMessageDialog(@Nullable Component parent, @NotNull Object msg, @Nullable String title) {
        showMessageDialog(parent, msg, title, JOptionPane.ERROR_MESSAGE);
    }


    default void showMessageDialog(@NotNull Object msg, @Nullable String title, int type) {
        showMessageDialog(getFrame(), msg, title, type);
    }

    default void showPlainMessageDialog(@NotNull Object msg, @Nullable String title) {
        showPlainMessageDialog(getFrame(), msg, title);
    }

    default void showInfoMessageDialog(@NotNull Object msg, @Nullable String title) {
        showInfoMessageDialog(getFrame(), msg, title);
    }

    default void showWarnMessageDialog(@NotNull Object msg, @Nullable String title) {
        showWarnMessageDialog(getFrame(), msg, title);
    }

    default void showErrorMessageDialog(@NotNull Object msg, @Nullable String title) {
        showErrorMessageDialog(getFrame(), msg, title);
    }


    @Nullable
    default File[] showFileChooser(@NotNull ChooserConfig config) {
        return ChooserConfig.showFileChooser(getFrame(), config);
    }


    static void close(@NotNull JFrame frame) {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }


    /* Menu */

    @NotNull
    static JMenu createThemeSelectorMenu() {
        final JMenu menu = new JMenu("Theme");
        final String current = Settings.getCurrentLookAndFeelClassName();

        final ButtonGroup group = new ButtonGroup();
        final Map<String, ButtonModel> map = new HashMap<>();

        final Function<UIManager.LookAndFeelInfo, JRadioButtonMenuItem> func = laf -> {
            final String name = laf.getName();
            final String cn = laf.getClassName();
            final JRadioButtonMenuItem item = new JRadioButtonMenuItem(name);
            final ButtonModel model = item.getModel();
            map.put(cn, model);

            model.addItemListener(e -> {
                if (item.isSelected()) {
                    final boolean done = Settings.getSingleton().setLookAndFeel(cn);
                    if (!done) {
                        final ButtonModel cur = map.get(Settings.getCurrentLookAndFeelClassName());
                        if (cur != null) {
                            Async.uiPost(() -> cur.setSelected(true));
                        }

                        Ui.showErrorMessageDialog(null, "Failed to set Theme " + name, "Theme");
                    }
                }
            });

            group.add(item);
            return item;
        };

        // Internal
        if (CollectionUtil.notEmpty(R.LOOK_AND_FEELS_INTERNAL)) {
            final JMenu internalMenu = new JMenu("System");
            final String system = UIManager.getSystemLookAndFeelClassName();
            final String crossPlatform = UIManager.getCrossPlatformLookAndFeelClassName();
            final boolean hasSystem = Format.notEmpty(system) && !system.equals(crossPlatform);

            for (UIManager.LookAndFeelInfo info: R.LOOK_AND_FEELS_INTERNAL) {
                final String cn = info.getClassName();
                String _name = info.getName();
                String qualifier = null;
                if (hasSystem && cn.equals(system)) {
                    qualifier = "System";
                } else if (Format.notEmpty(crossPlatform) && cn.equals(crossPlatform)) {
                    qualifier = "Cross Platform";
                }

                final JRadioButtonMenuItem item;
                if (Format.notEmpty(qualifier)) {
                    item = func.apply(new UIManager.LookAndFeelInfo(_name + " (" + qualifier + ")", cn));
                } else {
                    item = func.apply(info);
                }

                internalMenu.add(item);
            }

            menu.add(internalMenu);
        }

        // Flat LAF
        if (CollectionUtil.notEmpty(R.LOOK_AND_FEELS_FLAT_LAF)) {
            final JMenu lafMenu = new JMenu("Flat");
            for (UIManager.LookAndFeelInfo info: R.LOOK_AND_FEELS_FLAT_LAF) {
                final JRadioButtonMenuItem item = func.apply(info);
                lafMenu.add(item);
            }

            menu.add(lafMenu);
        }

        // Flat Material LAF
        if (CollectionUtil.notEmpty(R.LOOK_AND_FEELS_FLAT_LAF_MATERIAL)) {
            final JMenu matMenu = new JMenu("Material");
            for (UIManager.LookAndFeelInfo info: R.LOOK_AND_FEELS_FLAT_LAF_MATERIAL) {
                final JRadioButtonMenuItem item = func.apply(info);
                matMenu.add(item);
            }

            menu.add(matMenu);
        }

        if (Format.notEmpty(current)) {
            final ButtonModel model = map.get(current);;
            if (model != null) {
                model.setSelected(true);
            }
        }

        return menu;
    }

    @NotNull
    static JMenu createSettingsAppearanceMenu() {
        final JMenu menu = new JMenu("Appearance");

        // 1. Theme
        menu.add(createThemeSelectorMenu());

        // Last - Reset
        menu.addSeparator();
        final JMenuItem resetAppearance = new JMenuItem("Reset Appearance");
        resetAppearance.addActionListener(e -> Settings.getSingleton().resetAppearance());
        menu.add(resetAppearance);

        return menu;
    }


    @NotNull
    static JMenu createSettingsConfigurationMenu(@NotNull Ui ui) {
        final JMenu menu = new JMenu(TITLE_CONFIGURATIONS);

        // 1. Numerical Integration
        final JMenuItem numericalIntegrationIntervals = new JMenuItem("Integration intervals");
        numericalIntegrationIntervals.addActionListener(e -> askConfigureNumericalIntegrationIntervalCount(ui));
        menu.add(numericalIntegrationIntervals);

        // Last - Reset

        menu.addSeparator();
        final JMenuItem resetConfig = new JMenuItem("Reset Config");
        resetConfig.addActionListener(e -> Settings.getSingleton().resetConfig());
        menu.add(resetConfig);

        return menu;
    }

    @NotNull
    static JMenu createSettingsLogsMenu() {
        final JMenu menu = new JMenu("Logs");

        menu.add(new JCheckBoxMenuItem(Log.getDebugAction()));
        menu.add(new JCheckBoxMenuItem(Log.getLogToConsoleAction()));
        menu.add(new JCheckBoxMenuItem(Log.getLogToFileAction()));
        menu.addSeparator();
        menu.add(new JMenuItem(Log.getResetAction()));
        return menu;
    }

    @NotNull
    static JMenu createSettingsMenu(@NotNull Ui ui) {
        final JMenu menu = new JMenu("Settings");

        // 1. Appearance
        menu.add(createSettingsAppearanceMenu());

        // 2. Config
        menu.add(createSettingsConfigurationMenu(ui));

        // 3. Logs
        menu.add(createSettingsLogsMenu());

        // Last: Reset All
        menu.addSeparator();
        menu.add(Settings.getResetAllAction());

        return menu;
    }

    @NotNull
    static JMenu createRotorStatesMenu(@NotNull Function<ActionInfo, ? extends Action> creator) {
        final JMenu menu = new JMenu("Rotor States");
        menu.add(creator.apply(ActionInfo.SAVE_ALL_ROTOR_STATES_TO_CSV));
        menu.add(creator.apply(ActionInfo.LOAD_EXTERNAL_ROTOR_STATES_FROM_CSV));
        menu.addSeparator();
        menu.add(creator.apply(ActionInfo.CLEAR_AND_RESET_ROTOR_STATE_MANAGER));
        menu.add(creator.apply(ActionInfo.CLEAR_AND_RELOAD_ROTOR_STATE_MANAGER));
        return menu;
    }


    @NotNull
    static JMenu createViewMenu(@NotNull Function<ActionInfo, ? extends Action> creator) {
        final JMenu menu = new JMenu("View");
        menu.add(creator.apply(ActionInfo.TOGGLE_MENUBAR));
        menu.add(creator.apply(ActionInfo.TOGGLE_CONTROLS));
        menu.addSeparator();
        menu.add(creator.apply(ActionInfo.TOGGLE_FULLSCREEN));
        menu.add(creator.apply(ActionInfo.TOGGLE_PRESENTATION_MODE));
        return menu;
    }

    static void askConfigureNumericalIntegrationIntervalCount(@NotNull Ui ui) {
        final String msg = "Set Numerical integration interval count (blank to reset)\nRotor States needs to be reloaded to take effect Go to Menu > Rotor States > Reload\n\nMinimum: " + ComplexUtil.FOURIER_TRANSFORM_SIMPSON_13_N_MIN + "\nDefault: " + ComplexUtil.FOURIER_TRANSFORM_SIMPSON_13_N_DEFAULT;

        final String input = (String) JOptionPane.showInputDialog(ui.getFrame(), msg, TITLE_CONFIGURATIONS, JOptionPane.PLAIN_MESSAGE, null, null, Settings.getCurrentFTIntegrationIntervalCount());

        try {
            final int val =  Format.notEmpty(input)? Integer.parseInt(input): -1;
            if (Settings.getSingleton().setFTIntegrationIntervalCount(val)) {
                // todo: show snackbar done
            } else {
                ui.showWarnMessageDialog("Could not set Numerical Integration interval count to " + val, TITLE_CONFIGURATIONS);
            }
        } catch (NumberFormatException exc) {
            ui.showErrorMessageDialog("Invalid Input: " + input + "\nCan only be an integer", TITLE_CONFIGURATIONS);
        }
    }


    /* Loading ans Saving */

    @Nullable
    static FTUi showFtUi(@NotNull Ui ui, @NotNull RotorStateManager manager) {
        if (manager.isNoOp()) {
            ui.showErrorMessageDialog("No function selected yet\nSelect a function to view Fourier Transform Ui", null);
            return null;
        }

        return new FTUi(manager);
    }

    static void askClearAndResetRotorStateManager(@NotNull Ui ui, @NotNull RotorStateManager manager, boolean reload) {
        if (manager.isNoOp() || manager.getAllLoadedRotorStatesCount() == 0) {
            return;
        }

        if (reload && manager.getRotorCount() == 0) {
            reload = false;     // cannot reload
        }

        final String title = reload? "Reload Rotor States": "Delete Rotor States";
        final String msg = "This will delete all loaded Rotor States. They have to be loaded again for future use. This is highly EXPENSIVE\n\nDo you wish to continue?";

        final int option = JOptionPane.showConfirmDialog(ui.getFrame(), msg, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (option == JOptionPane.YES_OPTION) {
            if (reload) {
                manager.clearAndReloadAsync();
            } else {
                manager.clearAndResetSync();
            }
        }
    }


    static void askLoadExternalRotorStatesFromCSV(@NotNull Ui ui, @NotNull RotorStateManager manager) {
        final ExternalRotorStatesLoadPanel panel = new ExternalRotorStatesLoadPanel(ui);
        panel.showDialog(manager);
    }

    static void askSaveRotorStatesToCSV(@NotNull Ui ui, @NotNull RotorStateManager manager) {
        final String err;

        if (manager.isNoOp()) {
            err = "No function selected yet!";
        } else if (manager.isLoading()) {
            err = "Rotor States are still loading!";
        } else if (manager.getAllLoadedRotorStatesCount() < 1) {
            err = "Nothing loaded yet!";
        } else {
            err = null;
        }

        if (Format.notEmpty(err)) {          // Invalid save request
            ui.showErrorMessageDialog("Invalid Save Request\nError: " + err, null);
            return;
        }

        final FunctionState functionState = manager.createFunctionState();

        final String dialogTitle = "Save Rotor States";
        final ChooserConfig config = ChooserConfig.saveFileSingle()
                .setDialogTitle(dialogTitle)
                .setStartDir(R.DIR_FUNCTION_STATE_SAVES)
                .setUseAcceptAllFIleFilter(false)
                .setFileHidingEnabled(false)
                .setChoosableFileFilters(R.EXT_ROTOR_STATES_CSV_FILE_FILTER)
                .setApproveButtonText("Save")
                .setApproveButtonTooltipText(dialogTitle)
                .build();

        final File[] files = config.showFIleChooser(ui.getFrame());
        if (files == null || files.length == 0 || files[0] == null)
            return;

        final Path outPath = FileUtil.getNonExistingFile(FileUtil.ensureExtension(files[0].toPath(), R.EXT_ROTOR_STATES_CSV_FILE_EXTENSION));

        // todo show snackbar
        final Canceller c = functionState.writeRotorStatesASCSVAsync(outPath, new TaskConsumer<>() {
            @Override
            public void onFailed(@Nullable Throwable t) {
                final String errorMsg = t == null? "Unknown": t.getClass().getSimpleName() + " -> " + t.getMessage();
                final String msg = String.format("Failed To save Rotor States\n\nFunction: %s\nFile: %s\nError: %s", manager.getFunctionMeta().getTypedFunctionDisplayName(), outPath, errorMsg);
                ui.showErrorMessageDialog(msg, dialogTitle);
            }

            @Override
            public void consume(Void data) {
                final String msg = String.format("Rotor States saved\n\nFile: %s\nFunction: %s", outPath, manager.getFunctionMeta().getTypedFunctionDisplayName());
                ui.showInfoMessageDialog(msg, dialogTitle);
            }

            @Override
            public void onCancelled(@Nullable Void dataProcessedYet) {
                TaskConsumer.super.onCancelled(dataProcessedYet);
            }
        });
    }

    static void askConfigureFrequencyProvider(@NotNull Ui ui, @NotNull RotorStateManager manager) {
        if (manager.isNoOp()) {
            ui.showErrorMessageDialog("No function selected yet\nSelect a function to configure frequency provider", null);
            return;
        }

        final FrequencyProviderSelectorPanel panel = new FrequencyProviderSelectorPanel(manager.getManagerRotorFrequencyProviderOrDefault(), manager.getManagerDefaultRotorFrequencyProvider());
        final RotorFrequencyProviderI freqProvider = panel.showDialog(ui.getFrame());
        if (freqProvider != null) {
            manager.setRotorFrequencyProvider(freqProvider);
        }
    }


    static void askSaveFunctionStateToFIle(@NotNull Ui ui, @NotNull RotorStateManager manager) {
        final String err;

        if (manager.isNoOp()) {
            err = "No function selected yet!";
        } else if (manager.isLoading()) {
            err = "Rotor States are still loading!";
        } else if (manager.getRotorCount() < 1) {
            err = "Nothing loaded yet!";
        } else {
            err = null;
        }

        if (Format.notEmpty(err)) {          // Invalid save request
            ui.showErrorMessageDialog("Invalid Save Request\nError: " + err, null);
            return;
        }

        final FunctionState functionState = manager.createFunctionState();
        functionState.setSerializeFunction(true);       // 1st with serialization

        final String functionTitle = manager.getFunctionMeta().getTypedFunctionDisplayName();
        final String dialogTitle = "Save Function State";

        final ChooserConfig config = ChooserConfig.saveFileSingle()
                .setDialogTitle(dialogTitle)
                .setStartDir(R.DIR_FUNCTION_STATE_SAVES)
                .setChoosableFileFilters(R.FUNCTION_STATE_SAVE_FILE_FILTER)
                .setUseAcceptAllFIleFilter(false)
                .setFileHidingEnabled(true)
                .setApproveButtonText("Save")
                .setApproveButtonTooltipText(dialogTitle)
                .build();

        final File[] files = ui.showFileChooser(config);
        if (files == null || files.length == 0 || files[0] == null)
            return;

        final Path outPath = FileUtil.getNonExistingFile(FileUtil.ensureExtension(files[0].toPath(), R.FUNCTION_STATE_SAVE_FILE_EXTENSION));

        // TODO: show snackbar saving
        final Canceller c = functionState.writeJsonAsync(outPath, new TaskConsumer<>() {

            @Override
            public void onFailed(@Nullable Throwable t) {
                Log.e(FourierUi.TAG, "failed to save function state", t);

                final boolean retry = functionState.hasSerialisedFunction();

                final String errorMsg = t == null? "Unknown": t.getClass().getSimpleName() + " -> " + t.getMessage();
                final String msg = String.format("Failed To save Function State.%s\n\nFunction: %s\nError: %s", retry? " Try saving without Function Definition?": "", functionTitle, errorMsg);

                if (retry) {
                    final int option = JOptionPane.showConfirmDialog(ui.getFrame(), msg, dialogTitle, JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                    if (option == JOptionPane.YES_OPTION) {
                        functionState.setSerializeFunction(false);          // without function serialization
                        functionState.writeJsonAsync(outPath, this);
                    }
                } else {
                    ui.showErrorMessageDialog(msg, dialogTitle);
                }
            }

            private void onSuccess() {
                final String msg = String.format("Function State saved\n\nFile: %s\nFunction: %s\nDefinition saved: %s", outPath, functionTitle, functionState.hasSerialisedFunction());
                ui.showInfoMessageDialog(msg, dialogTitle);
            }

            @Override
            public void consume(Void data) {
                onSuccess();
            }

            @Override
            public void onCancelled(@Nullable Void dataProcessedYet) {
                ui.showInfoMessageDialog("Function State save CANCELLED", dialogTitle);
            }
        });
    }

    static void askLoadFunctionStateFromFile(@NotNull Ui ui, @NotNull Consumer<FunctionProviderI> successConsumer) {
        R.ensureFunctionStateSaveDir();

        final String dialogTitle = "Load Function State";

        final ChooserConfig config = ChooserConfig.openFileSingle()
                .setDialogTitle(dialogTitle)
                .setStartDir(R.DIR_FUNCTION_STATE_SAVES)
                .setChoosableFileFilters(R.FUNCTION_STATE_SAVE_FILE_FILTER)
                .setUseAcceptAllFIleFilter(false)
                .setFileHidingEnabled(false)
                .setApproveButtonText("Load")
                .setApproveButtonTooltipText(dialogTitle)
                .build();

        final File[] files = ui.showFileChooser(config);
        if (files == null || files.length == 0 || files[0] == null) {
            return;
        }

        final Path file = files[0].toPath();

        final Wrapper.Bool withFunctionDefinition = new Wrapper.Bool(true);

        // todo: show snack-bar
        final Canceller c = FunctionState.loadFromJsonAsync(file, true, new TaskConsumer<>() {

            @Override
            public void onFailed(@Nullable Throwable e) {
                Log.e(FourierUi.TAG, "failed to load function state, withFunctionState: " + withFunctionDefinition.get(), e);

                final boolean retry = withFunctionDefinition.get();
                withFunctionDefinition.set(false);

                final String errMsg = e == null? "Unknown": e.getClass().getSimpleName() + " -> " + e.getMessage();
                final String msg = String.format("Failed to load Function State%s\n\nFile: %s\nError: %s", retry? ". Try loading without Function Definition?": "", file, errMsg);

                if (retry) {
                    final int option = JOptionPane.showConfirmDialog(ui.getFrame(), msg, dialogTitle, JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                    if (option == JOptionPane.YES_OPTION) {
                        // todo snackbar
                        final Canceller c2 = FunctionState.loadFromJsonAsync(file, false, this);
                    }
                } else {
                    ui.showErrorMessageDialog(msg, dialogTitle);
                }
            }

            private void done(@NotNull FunctionProviderI provider) {
                successConsumer.consume(provider);

                final String msg = String.format("Function State loaded\n\nFile: %s\nFunction: %s\nHas Definition: %s", file, provider.getFunctionMeta().getTypedFunctionDisplayName(), provider.getFunctionMeta().hasBaseDefinition());
                ui.showInfoMessageDialog(msg, null);
            }

            @NotNull
            private FunctionProviderI toProvider(@NotNull FunctionState state) {
                return state.toProvider(file.getFileName().toString(), true);
            }

            @Override
            public void consume(FunctionState state) {
                if (state == null) {
                    onFailed(null);
                } else {
                    // todo: additional csv load
                    done(toProvider(state));
                }
            }

            @Override
            public void onCancelled(@Nullable FunctionState state) {
                if (state == null) {
                    ui.showInfoMessageDialog("Function State load Cancelled\n\nFile: " + file, dialogTitle);
                } else {
                    // todo remove
                    final FunctionProviderI provider = toProvider(state);
                    final String msg = String.format("Function State load CANCELLED, but it was already loaded\nDo you still want to add this function?\n\nFunction: %s\nFile: %s", provider.getFunctionMeta().getTypedFunctionDisplayName(), file);

                    final int option = JOptionPane.showConfirmDialog(ui.getFrame(), msg, dialogTitle, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    if (option == JOptionPane.YES_OPTION) {
                        done(provider);
                    }
                }
            }
        });


//        RotorStateManager.loadFunctionStateFileAsync(file.toPath(), fp -> {
//            if (fp != null) {
//                functionProviders.ensureAddSelect(fp);
//
//                final String displayName = fp.getFunctionMeta().displayName();
//                final FunctionType type = fp.getFunctionMeta().functionType();
//                final ComplexDomainFunctionI func = fp.getFunction();
//                final boolean hasDefinition = (type != FunctionType.EXTERNAL_ROTOR_STATE) || (func instanceof RotorStatesFunction && ((RotorStatesFunction) func).hasBaseFunction());
//
//                final String msg = String.format("Rotor States Function loaded\nFile: %s\nFunction: %s (%s)\nHas Definition: %s", file.getPath(), displayName, type, hasDefinition);
//                showInfoMessageDialog(msg, null);
//            } else {
//                showErrorMessageDialog("Failed to load Rotor States. FIle might be corrupted or of invalid format\n\nFile: " + file.getPath(), null);
//            }
//        });
    }

    static void askLoadExternalPathFunctions(@NotNull Ui ui, @NotNull Consumer<java.util.List<FunctionProviderI>> successConsumer) {
        final String dialogTitle = "Load Path Functions";

        final ChooserConfig config = ChooserConfig.openFile(true)
                .setDialogTitle(dialogTitle)
                .setStartDir(R.DIR_EXTERNAL_PATH_FUNCTIONS)
                .setUseAcceptAllFIleFilter(false)
                .setChoosableFileFilters(R.PATH_DATA_FILE_FILTER)
                .setFileHidingEnabled(false)
                .setApproveButtonText("Load")
                .setApproveButtonTooltipText(dialogTitle)
                .build();

        final File[] files = ui.showFileChooser(config);
        if (files == null || files.length == 0)
            return;

        final Path[] paths = Arrays.stream(files).map(File::toPath).toArray(Path[]::new);

        // todo show message loading
        final Canceller canceller = R.loadExternalPathFunctionsAsync(paths, R.DEFAULT_VALIDATE_EXTERNAL_FILES, new Consumer<>() {
            @Override
            public void consume(FunctionProviderI[] data) {
                if (data == null || data.length == 0) {
                    ui.showErrorMessageDialog("FAILED to load path functions", dialogTitle);
                    return;
                }

                final StringJoiner err = new StringJoiner("\n");
                final java.util.List<FunctionProviderI> providers = new ArrayList<>();
                for (int i=0; i < data.length; i++) {
                    final FunctionProviderI fp = data[i];
                    if (fp != null) {
                        providers.add(fp);
                    } else {
                        err.add(paths[i].getFileName().toString());
                    }
                }

                successConsumer.consume(providers);

                final int loadedCount = providers.size();
                String msg = (loadedCount > 0? String.valueOf(loadedCount): "No") + " Path Function" + (loadedCount > 1? "s": "") + " loaded";

                final boolean hasErr = err.length() > 0;
                if (hasErr) {
                    msg += "\n\nFAILED to load\n" + err;
                }

                ui.showMessageDialog(msg, dialogTitle, hasErr? loadedCount > 0? JOptionPane.WARNING_MESSAGE: JOptionPane.ERROR_MESSAGE: JOptionPane.INFORMATION_MESSAGE);
            }

            @Override
            public void onCancelled(FunctionProviderI @Nullable [] dataProcessedYet) {
                ui.showErrorMessageDialog("Path Functions load CANCELLED", dialogTitle);
            }
        });
    }

    static void askLoadExternalPathFunctionsFromDir(@NotNull Ui ui, @NotNull Consumer<java.util.List<FunctionProviderI>> successConsumer) {
        final ChooserConfig config = ChooserConfig.openDirSingle()
                .setDialogTitle("Scan Folder for Path Functions")
                .setStartDir(R.DIR_EXTERNAL_PATH_FUNCTIONS)
                .setFileHidingEnabled(false)
                .setApproveButtonText("Scan")
                .setApproveButtonTooltipText("Scan Path Functions in folder")
                .build();

        final File[] files = ui.showFileChooser(config);
        if (files == null || files.length == 0 || files[0] == null) {
            return;
        }

        final File dir = files[0];
        // TODO: show snackbar
        final Canceller canceller = R.loadExternalPathFunctionsAsync(dir.toPath(), R.DEFAULT_VALIDATE_EXTERNAL_FILES, new Consumer<>() {
            @Override
            public void consume(R.LoadResult data) {
                String msg;
                int msgType;
                if (data == null) {
                    msg = "FAILED to scan folder for Path Functions";
                    msgType = JOptionPane.ERROR_MESSAGE;
                } else {
                    successConsumer.consume(data.getFunctionProviders());
                    final int loadCount = data.successFiles();
                    final int failCount = data.failedFiles();
                    msg = (loadCount > 0? String.valueOf(loadCount): "No") + " Path Function" + (loadCount > 1? "s": "") + " loaded";
                    if (failCount > 0) {
                        msg += "\nFAILED to load " + failCount + " Path Function" + (failCount > 1? "s": "");
                        msgType = loadCount > 0? JOptionPane.WARNING_MESSAGE: JOptionPane.ERROR_MESSAGE;
                    } else {
                        msgType = loadCount > 0? JOptionPane.INFORMATION_MESSAGE: JOptionPane.WARNING_MESSAGE;
                    }
                }

                msg += "\nSource: " + dir.getPath();
                ui.showMessageDialog(msg, null, msgType);
            }

            @Override
            public void onCancelled(R.@Nullable LoadResult dataProcessedYet) {
                Consumer.super.onCancelled(dataProcessedYet);
            }
        });

    }

    static void askLoadExternalProgrammaticFunctions(@NotNull Ui ui, @NotNull Consumer<FunctionProviderI> successConsumer) {
        final ExternalProgramPanel dialog = new ExternalProgramPanel(ui.getFrame(), null);
        final ExternalJava.Location location = dialog.showDialog();

        if (location == null)
            return;

        // TODO: show snackbar
        Async.execute(() -> {
            final ComplexDomainFunctionI function = R.compileAndLoadExternalProgramFunction(location);

            final String displayTitle = R.createExternalProgramFunctionDisplayName(FileUtil.getFullName(location.relativeSourcePath));
            final FunctionMeta meta = new FunctionMeta(FunctionType.EXTERNAL_PROGRAM, displayTitle);

            return new SimpleFunctionProvider(meta, function);
        }, new TaskConsumer<>() {
            @Override
            public void onFailed(@Nullable Throwable err) {
                String errMsg;

                if (err instanceof ExternalJava.CompilationException) {
                    errMsg = "Failed to compile External Java Project";
                } else if (err instanceof NoSuchMethodException || err instanceof IllegalAccessException || err instanceof IllegalArgumentException) {
                    errMsg = "Function java class must have a public no-argument constructor";
                } else if (err instanceof InstantiationException || err instanceof InvocationTargetException) {
                    errMsg = "failed to Instantiate function class";
                } else {
                    errMsg = "Unknown Error in compilation and instantiation of function class";
                }

                errMsg += "\n\nProject Folder: " + location.classpath + "\nFunction Class: " + location.getClassName();
                if (err != null) {
                    errMsg += "\nError: " + err.getClass().getSimpleName() + " -> " + err.getMessage();
                }

                Log.e(FourierUi.TAG, errMsg, err);
                ui.showErrorMessageDialog(errMsg, null);
            }

            @Override
            public void onCancelled(@Nullable SimpleFunctionProvider dataProcessedYet) {
                TaskConsumer.super.onCancelled(dataProcessedYet);
            }

            @Override
            public void consume(SimpleFunctionProvider data) {
                successConsumer.consume(data);

                final String msg = "External programmatic function loaded -> " + data.getFunctionMeta().displayName() + "\n\nProject Folder: " + location.classpath + "\nFunction Class: " + location.getClassName();
                ui.showInfoMessageDialog(msg, null);
            }
        });
    }

}
