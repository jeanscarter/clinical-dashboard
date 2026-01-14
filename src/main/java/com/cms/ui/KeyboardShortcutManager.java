package com.cms.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class KeyboardShortcutManager {

    private static final Map<KeyStroke, Action> globalShortcuts = new HashMap<>();
    private static JFrame mainFrame;

    public static void initialize(JFrame frame) {
        mainFrame = frame;
        registerGlobalShortcuts();
    }

    private static void registerGlobalShortcuts() {
        JRootPane rootPane = mainFrame.getRootPane();
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();

        registerShortcut(inputMap, actionMap, "ctrl N", "new", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleNew();
            }
        });

        registerShortcut(inputMap, actionMap, "ctrl S", "save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSave();
            }
        });

        registerShortcut(inputMap, actionMap, "ctrl F", "find", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleFind();
            }
        });

        registerShortcut(inputMap, actionMap, "ctrl P", "print", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePrint();
            }
        });

        registerShortcut(inputMap, actionMap, "ctrl Q", "logout", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogout();
            }
        });

        registerShortcut(inputMap, actionMap, "F1", "help", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleHelp();
            }
        });

        registerShortcut(inputMap, actionMap, "ESCAPE", "cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCancel();
            }
        });
    }

    private static void registerShortcut(InputMap inputMap, ActionMap actionMap,
            String keyStroke, String actionName, Action action) {
        KeyStroke ks = KeyStroke.getKeyStroke(keyStroke);
        inputMap.put(ks, actionName);
        actionMap.put(actionName, action);
        globalShortcuts.put(ks, action);
    }

    private static void handleNew() {
        Component focusedComponent = getFocusedView();
        if (focusedComponent != null && focusedComponent instanceof ShortcutHandler) {
            ((ShortcutHandler) focusedComponent).onNew();
        }
    }

    private static void handleSave() {
        Component focusedComponent = getFocusedView();
        if (focusedComponent != null && focusedComponent instanceof ShortcutHandler) {
            ((ShortcutHandler) focusedComponent).onSave();
        }
    }

    private static void handleFind() {
        Component focusedComponent = getFocusedView();
        if (focusedComponent != null && focusedComponent instanceof ShortcutHandler) {
            ((ShortcutHandler) focusedComponent).onFind();
        }
    }

    private static void handlePrint() {
        Component focusedComponent = getFocusedView();
        if (focusedComponent != null && focusedComponent instanceof ShortcutHandler) {
            ((ShortcutHandler) focusedComponent).onPrint();
        }
    }

    private static void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(mainFrame,
                "¿Está seguro de cerrar sesión?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private static void handleHelp() {
        showShortcutsHelp();
    }

    private static void handleCancel() {
        Window focusedWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
        if (focusedWindow instanceof JDialog) {
            ((JDialog) focusedWindow).dispose();
        }
    }

    private static Component getFocusedView() {
        Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        while (focused != null) {
            if (focused instanceof ShortcutHandler) {
                return focused;
            }
            focused = focused.getParent();
        }
        return mainFrame.getContentPane();
    }

    public static void showShortcutsHelp() {
        String helpText = """
                <html>
                <h2>Atajos de Teclado</h2>
                <table style='font-family: Segoe UI; font-size: 12px;'>
                <tr><td><b>Ctrl+N</b></td><td>Nuevo (según contexto)</td></tr>
                <tr><td><b>Ctrl+S</b></td><td>Guardar</td></tr>
                <tr><td><b>Ctrl+F</b></td><td>Buscar</td></tr>
                <tr><td><b>Ctrl+P</b></td><td>Imprimir</td></tr>
                <tr><td><b>Ctrl+Q</b></td><td>Cerrar sesión</td></tr>
                <tr><td><b>F1</b></td><td>Ayuda</td></tr>
                <tr><td><b>Escape</b></td><td>Cancelar/Cerrar diálogo</td></tr>
                <tr><td colspan='2'><hr></td></tr>
                <tr><td><b>Ctrl+E</b></td><td>Editar seleccionado</td></tr>
                <tr><td><b>Delete</b></td><td>Eliminar seleccionado</td></tr>
                <tr><td><b>Ctrl+A</b></td><td>Adjuntar archivo</td></tr>
                <tr><td><b>Ctrl+V</b></td><td>Ver detalles</td></tr>
                </table>
                </html>
                """;

        JLabel label = new JLabel(helpText);
        JOptionPane.showMessageDialog(mainFrame, label, "Atajos de Teclado", JOptionPane.INFORMATION_MESSAGE);
    }

    public interface ShortcutHandler {
        default void onNew() {
        }

        default void onSave() {
        }

        default void onFind() {
        }

        default void onPrint() {
        }

        default void onEdit() {
        }

        default void onDelete() {
        }
    }
}
