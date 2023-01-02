package misc;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.tools.Tool;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// todo bug
public class Keyboard implements KeyListener {

    public static final String TAG = "Keyboard";

    static {
        EventQueue.invokeLater(() -> {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
                @Override
                public boolean dispatchKeyEvent(KeyEvent e) {
                    Log.d(TAG, "dispatchKeyEvent: " + e.getKeyChar());
                    return false;
                }
            });
        });

    }

//    static {
//
//        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(event -> {
//            synchronized (Keyboard.class) {
//                if (event.getID() == KeyEvent.KEY_PRESSED) {
//                    Log.d("key", "pressed  " + event.getKeyChar());
//
//                    pressedKeys.add(event.getKeyCode());
//                } else if (event.getID() == KeyEvent.KEY_RELEASED) {
//                    Log.d("key", "released  " + event.getKeyChar());
//
//                    pressedKeys.remove(event.getKeyCode());
//                }
//
//                return false;
//            }
//        });
//    }

    @NotNull
    private final Set<Integer> pressedKeys = new HashSet<>();

    public Keyboard(@NotNull Component component) {
//        component.addKeyListener(this);
    }

    public boolean isKeyPressed(int keyCode) {   // Any key code from the KeyEvent class
        return pressedKeys.contains(keyCode);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        synchronized (this) {
            pressedKeys.add(e.getKeyCode());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        synchronized (this) {
            pressedKeys.remove(e.getKeyCode());
        }
    }
}