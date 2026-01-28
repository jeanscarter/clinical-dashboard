package com.cms.ui.dialogs;

import com.cms.ui.components.IconFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Custom styled alert dialog with modern design.
 * Provides beautiful, consistent dialogs throughout the application.
 */
public class CustomAlertDialog extends JDialog {

    public enum AlertType {
        SUCCESS(new Color(34, 197, 94), new Color(22, 163, 74)),
        ERROR(new Color(239, 68, 68), new Color(220, 38, 38)),
        WARNING(new Color(245, 158, 11), new Color(217, 119, 6)),
        INFO(new Color(59, 130, 246), new Color(37, 99, 235)),
        QUESTION(new Color(59, 130, 246), new Color(37, 99, 235));

        final Color primary;
        final Color dark;

        AlertType(Color primary, Color dark) {
            this.primary = primary;
            this.dark = dark;
        }
    }

    private boolean confirmed = false;

    private CustomAlertDialog(Window parent, String title, String message, AlertType type, boolean showCancelButton) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        initializeUI(message, type, showCancelButton);
    }

    private void initializeUI(String message, AlertType type, boolean showCancelButton) {
        setLayout(new BorderLayout());
        setSize(420, 260);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);

        // Header with gradient
        JPanel header = createHeader(type);
        add(header, BorderLayout.NORTH);

        // Content
        JPanel content = createContent(message, type);
        add(content, BorderLayout.CENTER);

        // Footer with buttons
        JPanel footer = createFooter(type, showCancelButton);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createHeader(AlertType type) {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gradient = new GradientPaint(0, 0, type.primary, getWidth(), 0, type.dark);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        header.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 15));
        header.setPreferredSize(new Dimension(0, 70));

        // Icon
        Icon icon = getIconForType(type);
        JLabel iconLabel = new JLabel(icon);
        header.add(iconLabel);

        return header;
    }

    private Icon getIconForType(AlertType type) {
        return switch (type) {
            case SUCCESS -> IconFactory.createCheckCircleIcon(40, Color.WHITE);
            case ERROR -> IconFactory.createErrorIcon(40, Color.WHITE);
            case WARNING -> IconFactory.createWarningIcon(40, Color.WHITE);
            case INFO -> IconFactory.createInfoIcon(40, Color.WHITE);
            case QUESTION -> IconFactory.createQuestionIcon(40, Color.WHITE);
        };
    }

    private JPanel createContent(String message, AlertType type) {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(20, 30, 15, 30));

        // Message label with HTML support for multi-line
        String htmlMessage = "<html><center>" + message.replace("\n", "<br>") + "</center></html>";
        JLabel messageLabel = new JLabel(htmlMessage);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        messageLabel.setForeground(new Color(51, 65, 85));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        content.add(Box.createVerticalGlue());
        content.add(messageLabel);
        content.add(Box.createVerticalGlue());

        return content;
    }

    private JPanel createFooter(AlertType type, boolean showCancelButton) {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        footer.setBackground(new Color(248, 250, 252));
        footer.setBorder(new EmptyBorder(15, 20, 20, 20));

        if (showCancelButton) {
            JButton cancelBtn = createButton("No", new Color(100, 116, 139), Color.WHITE, false);
            cancelBtn.addActionListener(e -> {
                confirmed = false;
                dispose();
            });
            footer.add(cancelBtn);

            JButton confirmBtn = createButton("Sí", type.primary, Color.WHITE, true);
            confirmBtn.addActionListener(e -> {
                confirmed = true;
                dispose();
            });
            footer.add(confirmBtn);
        } else {
            JButton okBtn = createButton("Aceptar", type.primary, Color.WHITE, true);
            okBtn.addActionListener(e -> {
                confirmed = true;
                dispose();
            });
            footer.add(okBtn);
        }

        return footer;
    }

    private JButton createButton(String text, Color bgColor, Color fgColor, boolean primary) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", primary ? Font.BOLD : Font.PLAIN, 14));
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setPreferredSize(new Dimension(110, 40));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
        Color hoverColor = primary ? bgColor.darker() : new Color(71, 85, 105);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });

        return btn;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    // ========== Static Factory Methods ==========

    /**
     * Shows a success alert with a single "Aceptar" button.
     */
    public static void showSuccess(Component parent, String title, String message) {
        Window window = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
        CustomAlertDialog dialog = new CustomAlertDialog(window, title, message, AlertType.SUCCESS, false);
        dialog.setVisible(true);
    }

    /**
     * Shows an error alert with a single "Aceptar" button.
     */
    public static void showError(Component parent, String title, String message) {
        Window window = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
        CustomAlertDialog dialog = new CustomAlertDialog(window, title, message, AlertType.ERROR, false);
        dialog.setVisible(true);
    }

    /**
     * Shows a warning alert with a single "Aceptar" button.
     */
    public static void showWarning(Component parent, String title, String message) {
        Window window = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
        CustomAlertDialog dialog = new CustomAlertDialog(window, title, message, AlertType.WARNING, false);
        dialog.setVisible(true);
    }

    /**
     * Shows an info alert with a single "Aceptar" button.
     */
    public static void showInfo(Component parent, String title, String message) {
        Window window = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
        CustomAlertDialog dialog = new CustomAlertDialog(window, title, message, AlertType.INFO, false);
        dialog.setVisible(true);
    }

    /**
     * Shows a confirmation dialog with "Sí" and "No" buttons.
     * 
     * @return true if user clicked "Sí", false otherwise
     */
    public static boolean showConfirm(Component parent, String title, String message) {
        Window window = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
        CustomAlertDialog dialog = new CustomAlertDialog(window, title, message, AlertType.QUESTION, true);
        dialog.setVisible(true);
        return dialog.isConfirmed();
    }

    /**
     * Shows a confirmation dialog with custom alert type and "Sí"/"No" buttons.
     * 
     * @return true if user clicked "Sí", false otherwise
     */
    public static boolean showConfirm(Component parent, String title, String message, AlertType type) {
        Window window = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
        CustomAlertDialog dialog = new CustomAlertDialog(window, title, message, type, true);
        dialog.setVisible(true);
        return dialog.isConfirmed();
    }
}
