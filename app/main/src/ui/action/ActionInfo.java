package ui.action;

import action.ActionInfoI;
import app.R;
import misc.Format;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

public enum ActionInfo implements ActionInfoI {

    CANCEL_RUNNING_TASKS("Cancel",
            "Cancel running operations",
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            false),

    DRAG_UP(String.valueOf(Format.ARROW_UP),
            "Drag Up",
            KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK),
            true),

    DRAG_DOWN(String.valueOf(Format.ARROW_DOWN),
            "Drag Down",
            KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK),
            true),

    DRAG_LEFT(String.valueOf(Format.ARROW_LEFT),
            "Drag Left",
            KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK),
            true),

    DRAG_RIGHT(String.valueOf(Format.ARROW_RIGHT),
            "Drag Right",
            KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK),
            true),

    SCALE_UP(R.getScaleText(true), R.getScaleShortDescription(true), KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK), true),
    SCALE_DOWN(R.getScaleText(false), R.getScaleShortDescription(false), KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK), true),

    PLAY(R.getPlayPauseText(false), R.getPlayPauseShortDescription(false), null, true),
    PAUSE(R.getPlayPauseText(true), R.getPlayPauseShortDescription(true), KeyStroke.getKeyStroke(KeyEvent.VK_PAUSE, 0), true),
    STOP(R.getStopText(), R.getStopShortDescription(), null, true),
    TOGGLE_PLAY_PAUSE(R.getPlayPauseText(false), R.getPlayPauseShortDescription(false), KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), true),

    TOGGLE_DRAW_AXIS(R.getDrawAxisText(), R.getDrawAxisShortDescription(), null, true),
    TOGGLE_DRAW_COM(R.getDrawCOMText(), R.getDrawCOMShortDescription(), null, true),

    TOGGLE_POINTS_JOIN(R.getPointsJoiningText(), R.getPointsJoiningShortDescription(), KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_DOWN_MASK), true),
    TOGGLE_HUE_CYCLE(R.getHueCycleText(), R.getHueCycleShortDescription(), KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK), true),
    INVERT_X(R.getInvertXText(), R.getInvertXShortDescription(), KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.SHIFT_DOWN_MASK), false),
    INVERT_Y(R.getInvertYText(), R.getInvertYShortDescription(), KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.SHIFT_DOWN_MASK), false),
    TOGGLE_WAVE(R.getWaveToggleText(false), R.getWaveToggleShortDescription(false), KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), false),
    TOGGLE_GRAPH_CENTER(R.getGraphInCenterText(), R.getGraphInCenterShortDescription(), KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), false),
    TOGGLE_AUTO_TRACK(R.getAutoTrackInCenterText(), R.getAutoTrackInCenterShortDescription(), KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), false),
    RESET_MAIN(R.getResetMainText(), R.getResetMainShortDescription(), KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), true),
    RESET_SCALE(R.getResetScaleText(), R.getResetScaleShortDescription(), null, true),
    RESET_DRAG(R.getResetDragText(), R.getResetDragShortDescription(),  null, true),
    RESET_SCALE_DRAG(R.getResetScaleAndDragText(), R.getResetScaleAndDragShortDescription(),  KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.SHIFT_DOWN_MASK), true),
    RESET_FULL(R.getResetFullText(), R.getResetFullShortDescription(), KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK), true),
    UNDO(R.getUndoText(), R.getUndoShortDescription(), KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), false),
    REDO(R.getRedoText(), R.getRedoShortDescription(), KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK), false),

    TOGGLE_FULLSCREEN(R.getFullscreenText(), R.getFullscreenShortDescription(false), KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.SHIFT_DOWN_MASK), false),
    TOGGLE_CONTROLS(R.getToggleControlsText(true), R.getToggleControlsShortDescription(true), KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.SHIFT_DOWN_MASK), false),
    TOGGLE_MENUBAR(R.getToggleMenuBarText(true), R.getToggleMenuBarShortDescription(true), KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.SHIFT_DOWN_MASK), false),
    TOGGLE_PRESENTATION_MODE(R.getTogglePresentationModeText(false), R.getTogglePresentationModeShortDescription(false), KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.SHIFT_DOWN_MASK), false),


    SHOW_FT_UI("Fourier Transform Ui", "Show Fourier Transform Ui", KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), true, false),

    /* FT Configurations */
    CONFIGURE_ROTOR_FREQUENCY_PROVIDER(R.getConfigureRotorFrequencyProviderText(), R.getConfigureRotorFrequencyProviderShortDescription(), KeyStroke.getKeyStroke(KeyEvent.VK_F, 0),true, false),
    CONFIGURE_FUNCTION_GRAPH("Function Graph", "Configure Function Graph",  null, true, false),
    CONFIGURE_FT_GRAPH("FT Graph", "Configure Fourier Transform Graph", null, true, false),
    CONFIGURE_FT_WINDER_PANEL("FT Winder Graph", "Configure Fourier Transform Winder Graph", null, true, false),


    SAVE_FUNCTION_STATE_TO_FILE("Save State", "Save Function State", KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), true),
    LOAD_FUNCTION_STATE_FROM_FILE("Load State", "Load Function State", KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), false),
    CLEAR_FUNCTIONS_WITHOUT_DEFINITION("Clear Without Definition", "Remove all loaded functions without internal definitions", null, false),

    SAVE_ALL_ROTOR_STATES_TO_CSV("Save As CSV", "Save all loaded Rotor States to CSV file",  null, true, false),
    LOAD_EXTERNAL_ROTOR_STATES_FROM_CSV("Load From CSV", "Load external Rotor States for current function from CSV file",  null, true, false),
    LOAD_EXTERNAL_ROTOR_STATE_FUNCTION_FROM_CSV("Load Function From CSV", "Load external Rotor State Function from CSV file",  null, false, false),

    CLEAR_AND_RESET_ROTOR_STATE_MANAGER("Delete All", "Clear all loaded Rotor States (EXPENSIVE)",  null, true, false),
    CLEAR_AND_RELOAD_ROTOR_STATE_MANAGER("Reload", "Clear all loaded Rotor States and Reload (EXPENSIVE)",  null, true, true),

    LOAD_EXTERNAL_PATH_FUNCTIONS("Load Files", "Load Path Functions", KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK), false),
    LOAD_EXTERNAL_PATH_FUNCTIONS_FROM_DIR("Scan Folder", "Scan Path Functions from Folder", KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), false),
    CONVERT_SVG_TO_PATH_DATA("Extract SVG vectors", "Load path vectors from a SVG file", null, false),

    LAUNCH_PATH_DRAWING_UI("Draw Custom", "Draw custom path functions", KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), false),
    LAUNCH_PATH_DRAWING_UI_EXPORT_CURRENT_FUNCTION("Draw over current", "Draw custom path functions based on current function", KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), true, false),
    CLEAR_EXTERNAL_PATH_FUNCTIONS("Clear External", "Remove all external path functions", null, false),
    CLEAR_INTERNAL_PATH_FUNCTIONS("Clear Internal", "Remove all internal path functions", null, false),
    RESET_PATH_FUNCTIONS("Reset", "Reset all path functions to initial state", null, false),

    LOAD_EXTERNAL_PROGRAMMATIC_FUNCTION("Load Project", "Load external Programmatic Function",  KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.SHIFT_DOWN_MASK), false),
    CLEAR_EXTERNAL_PROGRAMMATIC_FUNCTIONS("Clear External", "Remove all external programmatic functions",  null, false),
    CLEAR_INTERNAL_PROGRAMMATIC_FUNCTIONS("Clear Internal", "Remove all internal programmatic functions",  null, false),
    RESET_PROGRAMMATIC_FUNCTIONS("Reset", "Reset all programmatic functions to initial state",  null, false),
    ;

    @NotNull
    public final String displayName;
    @Nullable
    public final String shortDescription;
    @Nullable
    public final KeyStroke keyStroke;
    public final boolean functionDependent;
    public final boolean rotorsDependent;

    @Nullable
    private Icon largeIconSelected;
    @Nullable
    private Icon largeIconUnselected;

    @Nullable
    private Icon smallIconSelected;
    @Nullable
    private Icon smallIconUnselected;

    ActionInfo(@NotNull String displayName,
               @Nullable String shortDescription,
               @Nullable KeyStroke keyStroke,
               boolean functionDependent,
               boolean rotorsDependent) {
        this.displayName = displayName;
        this.shortDescription = shortDescription;
        this.keyStroke = keyStroke;
        this.functionDependent = functionDependent;
        this.rotorsDependent = rotorsDependent;
    }

    ActionInfo(@NotNull String displayName,
               @Nullable String shortDescription,
               @Nullable KeyStroke keyStroke,
               boolean functionDependent) {
        this(displayName, shortDescription, keyStroke, functionDependent, functionDependent);
    }

    @Override
    public @NotNull String displayName() {
        return displayName;
    }

    @Override
    public @Nullable String shortDescription() {
        return shortDescription;
    }

    @Override
    public @Nullable KeyStroke keyStroke() {
        return keyStroke;
    }


    public ActionInfo setLargeIconOnSelect(boolean selected, @Nullable Icon largeIcon) {
        if (selected) {
            largeIconSelected = largeIcon;
        } else {
            largeIconUnselected = largeIcon;
        }

        return this;
    }

    @Nullable
    @Override
    public Icon getLargeIconOnSelect(boolean selected) {
        return selected? largeIconSelected: largeIconUnselected;
    }


    public ActionInfo setSmallIconOnSelect(boolean selected, @Nullable Icon smallIcon) {
        if (selected) {
            smallIconSelected = smallIcon;
        } else {
            smallIconUnselected = smallIcon;
        }

        return this;
    }

    @Nullable
    @Override
    public Icon getSmallIconOnSelect(boolean selected) {
        return selected? smallIconSelected: smallIconUnselected;
    }


    static {
//        TOGGLE_FULLSCREEN.setLargeIconOnSelect(false, R.createLargeIcon(R.IMG_MAXIMISE_ACCENT_64));
//        TOGGLE_FULLSCREEN.setLargeIconOnSelect(true, R.createLargeIcon(R.IMG_MINIMISE_ACCENT_64));

        TOGGLE_FULLSCREEN.setSmallIconOnSelect(false, R.createSmallIcon(R.IMG_MAXIMISE_ACCENT_64));
        TOGGLE_FULLSCREEN.setSmallIconOnSelect(true, R.createSmallIcon(R.IMG_MINIMISE_ACCENT_64));
    }

    @Nullable
    @Unmodifiable
    private static List<ActionInfo> sSharedValues;

    @NotNull
    public static List<ActionInfo> sharedValues() {
        if (sSharedValues == null) {
            sSharedValues = List.of(values());
        }

        return sSharedValues;
    }
}
