package action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public abstract class BaseAction extends AbstractAction implements PropertyChangeListener {

    private static final String KEY_LARGE_ICON_UNSELECTED = "field_large_icon_unselected";
    private static final String KEY_LARGE_ICON_SELECTED = "field_large_icon_selected";

    private static final String KEY_SMALL_ICON_UNSELECTED = "field_small_icon_unselected";
    private static final String KEY_SMALL_ICON_SELECTED = "field_small_icon_selected";

    public BaseAction() {
        super();
        addPropertyChangeListener(this);
    }

    public BaseAction(String name) {
        super(name);
    }

    public BaseAction(String name, Icon icon) {
        super(name, icon);
    }

    public BaseAction useInfo(@NotNull ActionInfoI info) {
        setName(info.displayName())
                .setShortDescription(info.shortDescription())
                .setAccelerator(info.keyStroke())
                .setSmallIconOnSelect(false, info.getSmallIconOnSelect(false))
                .setSmallIconOnSelect(true, info.getSmallIconOnSelect(true))
                .setLargeIconOnSelect(false, info.getLargeIconOnSelect(false))
                .setLargeIconOnSelect(true, info.getLargeIconOnSelect(true));

        return this;
    }


    public void sync() {
    }

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


    public BaseAction setSmallAndLargeLargeIcon(@Nullable Icon icon) {
        setSmallIcon(icon);
        setLargeIcon(icon);
        return this;
    }


    public BaseAction setLargeIconOnSelect(boolean selected, @Nullable Icon largeIcon) {
        putValue(selected? KEY_LARGE_ICON_SELECTED: KEY_LARGE_ICON_UNSELECTED, largeIcon);
        return this;
    }

    @Nullable
    public Icon getLargeIconOnSelect(boolean selected) {
        return (Icon) getValue(selected? KEY_LARGE_ICON_SELECTED: KEY_LARGE_ICON_UNSELECTED);
    }


    public BaseAction setSmallIconOnSelect(boolean selected, @Nullable Icon smallIcon) {
        putValue(selected? KEY_SMALL_ICON_SELECTED: KEY_SMALL_ICON_UNSELECTED, smallIcon);
        return this;
    }

    @Nullable
    public Icon getSmallIconOnSelect(boolean selected) {
        return (Icon) getValue(selected? KEY_SMALL_ICON_SELECTED: KEY_SMALL_ICON_UNSELECTED);
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


    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final String pn = evt.getPropertyName();

        if (SELECTED_KEY.equals(pn)) {
            final boolean selected = isSelected();
            Icon large = getLargeIconOnSelect(selected);
            if (large != null) {
                setLargeIcon(large);
            }

            Icon small = getSmallIconOnSelect(selected);
            if (small != null) {
                setSmallIcon(small);
            }
        } else if (KEY_LARGE_ICON_SELECTED.equals(pn) || KEY_LARGE_ICON_UNSELECTED.equals(pn)) {
            final boolean selected = isSelected();
            Icon large = getLargeIconOnSelect(selected);
            if (large != null) {
                setLargeIcon(large);
            }
        } else if (KEY_SMALL_ICON_SELECTED.equals(pn) || KEY_SMALL_ICON_UNSELECTED.equals(pn)) {
            final boolean selected = isSelected();
            Icon small = getSmallIconOnSelect(selected);
            if (small != null) {
                setSmallIcon(small);
            }
        }
    }
}
