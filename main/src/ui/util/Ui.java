package ui.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.Format;
import util.async.Consumer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.File;

public interface Ui {

    Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();

    String MAIN_TITLE = "Fourier Series";
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
        JOptionPane.showMessageDialog(parent, msg, Format.isEmpty(title)? MAIN_TITLE: title, type);
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

}
