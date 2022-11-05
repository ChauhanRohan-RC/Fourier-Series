package ui.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.live.Listeners;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.EnumMap;

public class UiAction extends BaseAction {

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
