package ui.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.Listeners;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.EnumMap;

public class UiAction extends AbstractAction {

    public interface Listener {
        void onAction(@NotNull UiAction action, @NotNull ActionEvent e);

        void onActionPropertyChange(@NotNull UiAction action, @NotNull PropertyChangeEvent e);
    }

    @Nullable
    private static EnumMap<ActionInfo, UiAction> sMap;

    @NotNull
    public static UiAction get(@NotNull ActionInfo info) {
        UiAction uia = null;
        if (sMap == null) {
            sMap = new EnumMap<>(ActionInfo.class);
        } else {
            uia = sMap.get(info);
        }

        if (uia == null) {
            uia = new UiAction(info);
            sMap.put(info, uia);
        }

        return uia;
    }


    @NotNull
    public final ActionInfo info;
    @NotNull
    private final Listeners<Listener> listeners = new Listeners<>();

    private UiAction(@NotNull ActionInfo info) {
        this.info = info;

        setName(info.displayName)
                .setShortDescription(info.shortDescription)
                .setAccelerator(info.keyStroke);

        addPropertyChangeListener(e -> listeners.forEachListener(l -> l.onActionPropertyChange(UiAction.this, e)));
    }

    public UiAction setName(@Nullable String name) {
        putValue(NAME, name);
        return this;
    }

    @Nullable
    public String getName() {
        return (String) getValue(NAME);
    }

    public UiAction setSmallIcon(@Nullable Icon icon) {
        putValue(SMALL_ICON, icon);
        return this;
    }

    @Nullable
    public Icon getSmallIcon() {
        return (Icon) getValue(SMALL_ICON);
    }

    public UiAction setLargeIcon(@Nullable Icon icon) {
        putValue(LARGE_ICON_KEY, icon);
        return this;
    }

    @Nullable
    public Icon getLargeIcon() {
        return (Icon) getValue(LARGE_ICON_KEY);
    }

    public UiAction setActionCommand(@Nullable String actionCommand) {
        putValue(ACTION_COMMAND_KEY, actionCommand);
        return this;
    }

    @Nullable
    public String getActionCommand() {
        return (String) getValue(ACTION_COMMAND_KEY);
    }


    public UiAction setSelected(boolean selected) {
        putValue(SELECTED_KEY, selected);
        return this;
    }

    public boolean isSelected() {
        return Boolean.TRUE.equals(getValue(SELECTED_KEY));
    }

    public UiAction setShortDescription(@Nullable String shortDescription) {
        putValue(SHORT_DESCRIPTION, shortDescription);
        return this;
    }

    @Nullable
    public String getShortDescription() {
        return (String) getValue(SHORT_DESCRIPTION);
    }

    public UiAction setLongDescription(@Nullable String longDescription) {
        putValue(SHORT_DESCRIPTION, longDescription);
        return this;
    }

    @Nullable
    public String getLongDescription() {
        return (String) getValue(LONG_DESCRIPTION);
    }

    public UiAction setAccelerator(KeyStroke accelerator) {
        putValue(ACCELERATOR_KEY, accelerator);
        return this;
    }

    @Nullable
    public KeyStroke getAccelerator() {
        return (KeyStroke) getValue(ACCELERATOR_KEY);
    }


    public boolean addListener(@NotNull Listener listener) {
        return listeners.addListener(listener);
    }

    public boolean ensureListener(@NotNull Listener listener) {
        return listeners.ensureListener(listener);
    }

    public boolean removeListener(@NotNull Listener listener) {
        return listeners.removeListener(listener);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        listeners.forEachListener(l -> l.onAction(UiAction.this, e));
    }
}
