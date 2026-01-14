package com.cms.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.Queue;

public class NotificationPanel extends JPanel {

    public enum NotificationType {
        SUCCESS(new Color(34, 197, 94), new Color(220, 252, 231), "✓"),
        INFO(new Color(59, 130, 246), new Color(219, 234, 254), "ℹ"),
        WARNING(new Color(234, 179, 8), new Color(254, 249, 195), "⚠"),
        ERROR(new Color(239, 68, 68), new Color(254, 226, 226), "✕");

        final Color textColor;
        final Color bgColor;
        final String icon;

        NotificationType(Color textColor, Color bgColor, String icon) {
            this.textColor = textColor;
            this.bgColor = bgColor;
            this.icon = icon;
        }
    }

    private static final int NOTIFICATION_WIDTH = 350;
    private static final int NOTIFICATION_HEIGHT = 60;
    private static final int SPACING = 10;
    private static final int AUTO_DISMISS_MS = 5000;

    private final Queue<NotificationItem> queue = new LinkedList<>();
    private final java.util.List<NotificationItem> visible = new java.util.ArrayList<>();
    private static NotificationPanel instance;

    public NotificationPanel() {
        setLayout(null);
        setOpaque(false);
    }

    public static NotificationPanel getInstance(JFrame frame) {
        if (instance == null) {
            instance = new NotificationPanel();
            JLayeredPane layeredPane = frame.getLayeredPane();
            instance.setBounds(0, 0, frame.getWidth(), frame.getHeight());
            layeredPane.add(instance, JLayeredPane.POPUP_LAYER);

            frame.addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    instance.setBounds(0, 0, frame.getWidth(), frame.getHeight());
                    instance.repositionNotifications();
                }
            });
        }
        return instance;
    }

    public static void success(JFrame frame, String message) {
        getInstance(frame).show(message, NotificationType.SUCCESS);
    }

    public static void info(JFrame frame, String message) {
        getInstance(frame).show(message, NotificationType.INFO);
    }

    public static void warning(JFrame frame, String message) {
        getInstance(frame).show(message, NotificationType.WARNING);
    }

    public static void error(JFrame frame, String message) {
        getInstance(frame).show(message, NotificationType.ERROR);
    }

    public void show(String message, NotificationType type) {
        NotificationItem item = createNotification(message, type);
        SwingUtilities.invokeLater(() -> {
            visible.add(item);
            add(item);
            repositionNotifications();
            revalidate();
            repaint();

            Timer dismissTimer = new Timer(AUTO_DISMISS_MS, e -> dismiss(item));
            dismissTimer.setRepeats(false);
            dismissTimer.start();
        });
    }

    private NotificationItem createNotification(String message, NotificationType type) {
        NotificationItem item = new NotificationItem(message, type);
        item.setPreferredSize(new Dimension(NOTIFICATION_WIDTH, NOTIFICATION_HEIGHT));
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dismiss(item);
            }
        });
        return item;
    }

    private void dismiss(NotificationItem item) {
        SwingUtilities.invokeLater(() -> {
            visible.remove(item);
            remove(item);
            repositionNotifications();
            revalidate();
            repaint();
        });
    }

    private void repositionNotifications() {
        int x = getWidth() - NOTIFICATION_WIDTH - 20;
        int y = 20;

        for (NotificationItem item : visible) {
            item.setBounds(x, y, NOTIFICATION_WIDTH, NOTIFICATION_HEIGHT);
            y += NOTIFICATION_HEIGHT + SPACING;
        }
    }

    private static class NotificationItem extends JPanel {
        NotificationItem(String message, NotificationType type) {
            setLayout(new BorderLayout());
            setBackground(type.bgColor);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(type.textColor.darker(), 1),
                    new EmptyBorder(10, 15, 10, 15)));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel iconLabel = new JLabel(type.icon);
            iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            iconLabel.setForeground(type.textColor);
            iconLabel.setBorder(new EmptyBorder(0, 0, 0, 10));

            JLabel messageLabel = new JLabel("<html>" + message + "</html>");
            messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            messageLabel.setForeground(type.textColor.darker());

            JLabel closeLabel = new JLabel("×");
            closeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            closeLabel.setForeground(new Color(148, 163, 184));

            add(iconLabel, BorderLayout.WEST);
            add(messageLabel, BorderLayout.CENTER);
            add(closeLabel, BorderLayout.EAST);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(getBackground());
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2d.dispose();
        }
    }
}
