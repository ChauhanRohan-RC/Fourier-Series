package ui.action;

import app.R;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import util.Format;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

public enum ActionInfo {

    CANCEL_RUNNING_TASKS("Cancel",
            "Cancel running operations",
            false,
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)),

    DRAG_UP(String.valueOf(Format.ARROW_UP),
            "Drag Up",
            true,
            KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK)),

    DRAG_DOWN(String.valueOf(Format.ARROW_DOWN),
            "Drag Down",
            true,
            KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK)),

    DRAG_LEFT(String.valueOf(Format.ARROW_LEFT),
            "Drag Left",
            true,
            KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK)),

    DRAG_RIGHT(String.valueOf(Format.ARROW_RIGHT),
            "Drag Right",
            true,
            KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK)),

    SCALE_UP(R.getScaleText(true), R.getScaleShortDescription(true), true, KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK)),
    SCALE_DOWN(R.getScaleText(false), R.getScaleShortDescription(false), true, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK)),

    PLAY(R.getPlayPauseText(false), R.getPlayPauseShortDescription(false), true, null),
    PAUSE(R.getPlayPauseText(true), R.getPlayPauseShortDescription(true), true, KeyStroke.getKeyStroke(KeyEvent.VK_PAUSE, 0)),
    STOP(R.getStopText(), R.getStopShortDescription(), true, null),
    TOGGLE_PLAY_PAUSE(R.getPlayPauseText(false), R.getPlayPauseShortDescription(false), true, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0)),

    TOGGLE_POINTS_JOIN(R.getPointsJoiningText(), R.getPointsJoiningShortDescription(), true, KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_DOWN_MASK)),
    TOGGLE_HUE_CYCLE(R.getHueCycleText(), R.getHueCycleShortDescription(), true, KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK)),
    INVERT_X(R.getInvertXText(), R.getInvertXShortDescription(), false, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.SHIFT_DOWN_MASK)),
    INVERT_Y(R.getInvertYText(), R.getInvertYShortDescription(), false, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.SHIFT_DOWN_MASK)),
    TOGGLE_WAVE(R.getWaveToggleText(false), R.getWaveToggleShortDescription(false), false, KeyStroke.getKeyStroke(KeyEvent.VK_W, 0)),
    TOGGLE_GRAPH_CENTER(R.getGraphInCenterText(), R.getGraphInCenterShortDescription(), false, KeyStroke.getKeyStroke(KeyEvent.VK_C, 0)),
    TOGGLE_AUTO_TRACK(R.getAutoTrackInCenterText(), R.getAutoTrackInCenterShortDescription(), false, KeyStroke.getKeyStroke(KeyEvent.VK_A, 0)),
    RESET_MAIN(R.getResetMainText(), R.getResetMainShortDescription(), true, KeyStroke.getKeyStroke(KeyEvent.VK_R, 0)),
    RESET_SCALE(R.getResetScaleText(), R.getResetScaleShortDescription(), true, null),
    RESET_SCALE_DRAG(R.getResetScaleAndDragText(), R.getResetScaleAndDragShortDescription(), true, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.SHIFT_DOWN_MASK)),
    RESET_FULL(R.getResetFullText(), R.getResetFullShortDescription(), true, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK)),

    TOGGLE_FULLSCREEN(R.getFullscreenText(), R.getFullscreenShortDescription(false), false, KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.SHIFT_DOWN_MASK)),
    TOGGLE_CONTROLS(R.getToggleControlsText(true), R.getToggleControlsShortDescription(true), false, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.SHIFT_DOWN_MASK)),
    TOGGLE_MENUBAR(R.getToggleMenuBarText(true), R.getToggleMenuBarShortDescription(true), false, KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.SHIFT_DOWN_MASK)),

    SHOW_FT_UI("Fourier Transform Ui", "Show Fourier Transform Ui", true, KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK)),

    /* Configurations */
    CONFIGURATIONS("Configuration", "Configure frequency, graphs and more", true, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK)),
    CONFIGURE_ROTOR_FREQUENCY_PROVIDER(R.getConfigureRotorFrequencyProviderText(), R.getConfigureRotorFrequencyProviderShortDescription(), true, KeyStroke.getKeyStroke(KeyEvent.VK_F, 0)),
    CONFIGURE_FUNCTION_GRAPH("Function Graph", "Configure Function Graph", true, null),
    CONFIGURE_FT_GRAPH("FT Graph", "Configure Fourier Transform Graph", true, null),


    SAVE_FUNCTION_STATE_TO_FILE("Save", "Save Function State", true, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK)),
    LOAD_FUNCTION_STATE_FROM_FILE("Load", "Load Function State", false, KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK)),
    CLEAR_FUNCTIONS_WITHOUT_DEFINITION("Clear Without Definition", "Remove all loaded functions without internal definitions", false, null),

    LOAD_EXTERNAL_PATH_FUNCTIONS("Load Files", "Load Path Functions", false, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK)),
    LOAD_EXTERNAL_PATH_FUNCTIONS_FROM_DIR("Scan Folder", "Scan Path Functions from Folder", false, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.SHIFT_DOWN_MASK)),
    CLEAR_EXTERNAL_PATH_FUNCTIONS("Clear External", "Remove all external path functions", false, null),
    CLEAR_INTERNAL_PATH_FUNCTIONS("Clear Internal", "Remove all internal path functions", false, null),
    RESET_PATH_FUNCTIONS("Reset", "Reset all path functions to initial state", false, null),

    LOAD_EXTERNAL_PROGRAMMATIC_FUNCTION("Load Project", "Load external Programmatic Function", false, KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.SHIFT_DOWN_MASK)),
    CLEAR_EXTERNAL_PROGRAMMATIC_FUNCTIONS("Clear External", "Remove all external programmatic functions", false, null),
    CLEAR_INTERNAL_PROGRAMMATIC_FUNCTIONS("Clear Internal", "Remove all internal programmatic functions", false, null),
    RESET_PROGRAMMATIC_FUNCTIONS("Reset", "Reset all programmatic functions to initial state", false, null),

    ;

    @NotNull
    public final String displayName;
    @Nullable
    public final String shortDescription;
    public final boolean functionDependent;
    @Nullable
    public final KeyStroke keyStroke;

    ActionInfo(@NotNull String displayName, @Nullable String shortDescription, boolean functionDependent, @Nullable KeyStroke keyStroke) {
        this.displayName = displayName;
        this.shortDescription = shortDescription;
        this.functionDependent = functionDependent;
        this.keyStroke = keyStroke;
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
