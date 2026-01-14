package com.cms.ui.components;

import javax.swing.*;
import java.awt.*;

public class LoadingOverlay extends JPanel {

    private static final Color OVERLAY_COLOR = new Color(255, 255, 255, 200);
    private static final Color SPINNER_COLOR = new Color(59, 130, 246);

    private final JLabel messageLabel;
    private boolean isLoading = false;
    private int spinnerAngle = 0;
    private Timer animationTimer;

    public LoadingOverlay() {
        setOpaque(false);
        setLayout(new GridBagLayout());

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JPanel spinnerContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawSpinner(g);
            }
        };
        spinnerContainer.setOpaque(false);
        spinnerContainer.setPreferredSize(new Dimension(50, 50));
        spinnerContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        messageLabel = new JLabel("Cargando...");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(71, 85, 105));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(spinnerContainer);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(messageLabel);

        add(centerPanel);

        animationTimer = new Timer(50, e -> {
            spinnerAngle = (spinnerAngle + 15) % 360;
            repaint();
        });
    }

    private void drawSpinner(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = 40;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (int i = 0; i < 8; i++) {
            int alpha = 255 - (i * 30);
            if (alpha < 50)
                alpha = 50;
            g2d.setColor(new Color(
                    SPINNER_COLOR.getRed(),
                    SPINNER_COLOR.getGreen(),
                    SPINNER_COLOR.getBlue(),
                    alpha));

            double angle = Math.toRadians(spinnerAngle - (i * 45));
            int x1 = x + size / 2 + (int) (Math.cos(angle) * 12);
            int y1 = y + size / 2 + (int) (Math.sin(angle) * 12);
            int x2 = x + size / 2 + (int) (Math.cos(angle) * 18);
            int y2 = y + size / 2 + (int) (Math.sin(angle) * 18);

            g2d.drawLine(x1, y1, x2, y2);
        }

        g2d.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (isLoading) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(OVERLAY_COLOR);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.dispose();
        }
        super.paintComponent(g);
    }

    public void showLoading(String message) {
        messageLabel.setText(message != null ? message : "Cargando...");
        isLoading = true;
        setVisible(true);
        animationTimer.start();
        repaint();
    }

    public void hideLoading() {
        isLoading = false;
        animationTimer.stop();
        setVisible(false);
    }

    public boolean isLoading() {
        return isLoading;
    }

    public static void showLoadingOn(JComponent parent, String message) {
        LoadingOverlay overlay = findOrCreateOverlay(parent);
        overlay.showLoading(message);
    }

    public static void hideLoadingOn(JComponent parent) {
        LoadingOverlay overlay = findOrCreateOverlay(parent);
        overlay.hideLoading();
    }

    private static LoadingOverlay findOrCreateOverlay(JComponent parent) {
        for (Component comp : parent.getComponents()) {
            if (comp instanceof LoadingOverlay) {
                return (LoadingOverlay) comp;
            }
        }

        LoadingOverlay overlay = new LoadingOverlay();
        overlay.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        parent.add(overlay, 0);
        parent.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                overlay.setBounds(0, 0, parent.getWidth(), parent.getHeight());
            }
        });
        return overlay;
    }
}
