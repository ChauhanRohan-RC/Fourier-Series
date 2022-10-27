package ui.action;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class BaseAction extends AbstractAction {

    public BaseAction setName(@Nullable String name) {
        putValue(NAME, name);
        return this;
    }

    @Nullable
    public String getName() {
        return (String) getValue(NAME);
    }

    public BaseAction setSmallIcon(@Nullable Icon icon) {
        putValue(SMALL_ICON, icon);
        return this;
    }

    @Nullable
    public Icon getSmallIcon() {
        return (Icon) getValue(SMALL_ICON);
    }

    public BaseAction setLargeIcon(@Nullable Icon icon) {
        putValue(LARGE_ICON_KEY, icon);
        return this;
    }

    @Nullable
    public Icon getLargeIcon() {
        return (Icon) getValue(LARGE_ICON_KEY);
    }

    public BaseAction setActionCommand(@Nullable String actionCommand) {
        putValue(ACTION_COMMAND_KEY, actionCommand);
        return this;
    }

    @Nullable
    public String getActionCommand() {
        return (String) getValue(ACTION_COMMAND_KEY);
    }


    public BaseAction setSelected(boolean selected) {
        putValue(SELECTED_KEY, selected);
        return this;
    }

    public boolean isSelected() {
        return Boolean.TRUE.equals(getValue(SELECTED_KEY));
    }

    public BaseAction setShortDescription(@Nullable String shortDescription) {
        putValue(SHORT_DESCRIPTION, shortDescription);
        return this;
    }

    @Nullable
    public String getShortDescription() {
        return (String) getValue(SHORT_DESCRIPTION);
    }

    public BaseAction setLongDescription(@Nullable String longDescription) {
        putValue(SHORT_DESCRIPTION, longDescription);
        return this;
    }

    @Nullable
    public String getLongDescription() {
        return (String) getValue(LONG_DESCRIPTION);
    }

    public BaseAction setAccelerator(KeyStroke accelerator) {
        putValue(ACCELERATOR_KEY, accelerator);
        return this;
    }

    @Nullable
    public KeyStroke getAccelerator() {
        return (KeyStroke) getValue(ACCELERATOR_KEY);
    }
}
