package ui.action;

import action.BaseAction;
import org.jetbrains.annotations.NotNull;
import live.Listeners;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

public class UiAction extends BaseAction {

    public interface Listener {
        boolean onAction(@NotNull UiAction action, @NotNull ActionEvent e);

        void onActionPropertyChange(@NotNull UiAction action, @NotNull PropertyChangeEvent e);
    }


    @NotNull
    public final ActionInfo info;
    @NotNull
    private final Listeners<Listener> listeners = new Listeners<>();

    public UiAction(@NotNull ActionInfo info) {
        this.info = info;
        useInfo(info);
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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);

        listeners.forEachListener(l -> l.onActionPropertyChange(UiAction.this, evt));
    }
}
