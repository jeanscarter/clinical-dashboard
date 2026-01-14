package com.cms.ui.dialogs;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageViewerDialog extends JDialog {

    public ImageViewerDialog(Window owner, String title, String imagePath) {
        super(owner, title, ModalityType.MODELESS);
        initializeUI(imagePath);
    }

    private void initializeUI(String imagePath) {
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            if (image != null) {
                ImagePanel imagePanel = new ImagePanel(image);
                JScrollPane scrollPane = new JScrollPane(imagePanel);
                scrollPane.setBorder(null);
                scrollPane.getViewport().setBackground(Color.DARK_GRAY);
                add(scrollPane, BorderLayout.CENTER);

                // Toolbar
                JPanel toolbar = createToolbar(imagePanel);
                add(toolbar, BorderLayout.NORTH);

                // Set size based on image
                int width = Math.min(image.getWidth() + 50, 1200);
                int height = Math.min(image.getHeight() + 100, 800);
                setSize(width, height);
            } else {
                showError("No se pudo cargar la imagen");
            }
        } catch (Exception e) {
            showError("Error al cargar la imagen: " + e.getMessage());
        }

        setLocationRelativeTo(getOwner());
    }

    private JPanel createToolbar(ImagePanel imagePanel) {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        toolbar.setBackground(new Color(50, 50, 50));
        toolbar.setBorder(new EmptyBorder(5, 10, 5, 10));

        JButton zoomInBtn = createToolbarButton("🔍+", "Acercar");
        zoomInBtn.addActionListener(e -> imagePanel.zoomIn());

        JButton zoomOutBtn = createToolbarButton("🔍-", "Alejar");
        zoomOutBtn.addActionListener(e -> imagePanel.zoomOut());

        JButton fitBtn = createToolbarButton("⬜", "Ajustar");
        fitBtn.addActionListener(e -> imagePanel.fitToWindow());

        JButton actualBtn = createToolbarButton("1:1", "Tamaño real");
        actualBtn.addActionListener(e -> imagePanel.actualSize());

        toolbar.add(zoomOutBtn);
        toolbar.add(zoomInBtn);
        toolbar.add(fitBtn);
        toolbar.add(actualBtn);

        return toolbar;
    }

    private JButton createToolbarButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        button.setBackground(new Color(70, 70, 70));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void showError(String message) {
        JLabel errorLabel = new JLabel(message, SwingConstants.CENTER);
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(errorLabel, BorderLayout.CENTER);
        setSize(400, 200);
    }

    private static class ImagePanel extends JPanel {
        private final BufferedImage image;
        private double scale = 1.0;

        ImagePanel(BufferedImage image) {
            this.image = image;
            setBackground(Color.DARK_GRAY);
            updatePreferredSize();
        }

        void zoomIn() {
            scale = Math.min(scale * 1.25, 5.0);
            updatePreferredSize();
        }

        void zoomOut() {
            scale = Math.max(scale / 1.25, 0.1);
            updatePreferredSize();
        }

        void fitToWindow() {
            Container parent = getParent();
            if (parent != null) {
                double scaleX = (double) parent.getWidth() / image.getWidth();
                double scaleY = (double) parent.getHeight() / image.getHeight();
                scale = Math.min(scaleX, scaleY) * 0.95;
                updatePreferredSize();
            }
        }

        void actualSize() {
            scale = 1.0;
            updatePreferredSize();
        }

        private void updatePreferredSize() {
            int width = (int) (image.getWidth() * scale);
            int height = (int) (image.getHeight() * scale);
            setPreferredSize(new Dimension(width, height));
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int width = (int) (image.getWidth() * scale);
            int height = (int) (image.getHeight() * scale);
            int x = (getWidth() - width) / 2;
            int y = (getHeight() - height) / 2;

            g2d.drawImage(image, x, y, width, height, null);
        }
    }
}
