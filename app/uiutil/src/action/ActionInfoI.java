package action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface ActionInfoI {

    @NotNull
    String displayName();

    @Nullable
    String shortDescription();

    @Nullable
    KeyStroke keyStroke();

    @Nullable
    Icon getLargeIconOnSelect(boolean selected);

    @Nullable
    Icon getSmallIconOnSelect(boolean selected);

}
