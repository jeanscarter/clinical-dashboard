package com.cms.ui.components;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageViewerDialog extends JDialog {

    private BufferedImage image;
    private double zoomFactor = 1.0;
    private Point imageOffset = new Point(0, 0);
    private Point lastDragPoint;
    private final JLabel imageLabel;
    private final JLabel zoomLabel;

    public ImageViewerDialog(Frame parent, String title) {
        super(parent, title, true);
        setSize(900, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel toolbar = createToolbar();
        add(toolbar, BorderLayout.NORTH);

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);

        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(new Color(30, 30, 30));
        imagePanel.add(imageLabel, BorderLayout.CENTER);

        imagePanel.addMouseWheelListener(this::handleMouseWheel);
        imagePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastDragPoint = e.getPoint();
            }
        });
        imagePanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleDrag(e);
            }
        });

        JScrollPane scrollPane = new JScrollPane(imagePanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        zoomLabel = new JLabel("100%");
        zoomLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        zoomLabel.setBorder(new EmptyBorder(5, 10, 5, 10));

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusBar.add(zoomLabel);
        add(statusBar, BorderLayout.SOUTH);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setBackground(new Color(248, 250, 252));
        toolbar.setBorder(new EmptyBorder(5, 10, 5, 10));

        JButton zoomInBtn = createToolbarButton("🔍+", "Acercar");
        zoomInBtn.addActionListener(e -> zoom(1.2));

        JButton zoomOutBtn = createToolbarButton("🔍-", "Alejar");
        zoomOutBtn.addActionListener(e -> zoom(0.8));

        JButton fitBtn = createToolbarButton("⬜", "Ajustar a ventana");
        fitBtn.addActionListener(e -> fitToWindow());

        JButton originalBtn = createToolbarButton("1:1", "Tamaño original");
        originalBtn.addActionListener(e -> resetZoom());

        JButton rotateLeftBtn = createToolbarButton("↺", "Rotar izquierda");
        rotateLeftBtn.addActionListener(e -> rotate(-90));

        JButton rotateRightBtn = createToolbarButton("↻", "Rotar derecha");
        rotateRightBtn.addActionListener(e -> rotate(90));

        toolbar.add(zoomInBtn);
        toolbar.add(zoomOutBtn);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(fitBtn);
        toolbar.add(originalBtn);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(rotateLeftBtn);
        toolbar.add(rotateRightBtn);

        return toolbar;
    }

    private JButton createToolbarButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setPreferredSize(new Dimension(45, 35));
        button.setFocusPainted(false);
        return button;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        this.zoomFactor = 1.0;
        this.imageOffset = new Point(0, 0);
        updateImageDisplay();
    }

    public void loadImage(String filePath) {
        try {
            BufferedImage img = ImageIO.read(new File(filePath));
            if (img != null) {
                setImage(img);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar imagen: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateImageDisplay() {
        if (image == null)
            return;

        int newWidth = (int) (image.getWidth() * zoomFactor);
        int newHeight = (int) (image.getHeight() * zoomFactor);

        Image scaled = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(scaled));

        zoomLabel.setText(String.format("%.0f%%", zoomFactor * 100));

        imageLabel.revalidate();
        imageLabel.repaint();
    }

    private void zoom(double factor) {
        double newZoom = zoomFactor * factor;
        if (newZoom >= 0.1 && newZoom <= 10.0) {
            zoomFactor = newZoom;
            updateImageDisplay();
        }
    }

    private void resetZoom() {
        zoomFactor = 1.0;
        imageOffset = new Point(0, 0);
        updateImageDisplay();
    }

    private void fitToWindow() {
        if (image == null)
            return;

        Dimension viewSize = getContentPane().getSize();
        double widthRatio = (viewSize.width - 50.0) / image.getWidth();
        double heightRatio = (viewSize.height - 100.0) / image.getHeight();
        zoomFactor = Math.min(widthRatio, heightRatio);
        updateImageDisplay();
    }

    private void rotate(int degrees) {
        if (image == null)
            return;

        int w = image.getWidth();
        int h = image.getHeight();

        BufferedImage rotated;
        if (Math.abs(degrees) == 90 || Math.abs(degrees) == 270) {
            rotated = new BufferedImage(h, w, image.getType());
        } else {
            rotated = new BufferedImage(w, h, image.getType());
        }

        Graphics2D g2d = rotated.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (degrees == 90) {
            g2d.translate(h, 0);
        } else if (degrees == -90 || degrees == 270) {
            g2d.translate(0, w);
        } else if (degrees == 180) {
            g2d.translate(w, h);
        }

        g2d.rotate(Math.toRadians(degrees));
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        this.image = rotated;
        updateImageDisplay();
    }

    private void handleMouseWheel(MouseWheelEvent e) {
        if (e.getWheelRotation() < 0) {
            zoom(1.1);
        } else {
            zoom(0.9);
        }
    }

    private void handleDrag(MouseEvent e) {
        if (lastDragPoint != null) {
            int dx = e.getX() - lastDragPoint.x;
            int dy = e.getY() - lastDragPoint.y;
            imageOffset.translate(dx, dy);
            lastDragPoint = e.getPoint();
        }
    }

    public static void showImage(Frame parent, String filePath, String title) {
        ImageViewerDialog dialog = new ImageViewerDialog(parent, title);
        dialog.loadImage(filePath);
        dialog.setVisible(true);
    }

    public static void showImage(Frame parent, BufferedImage image, String title) {
        ImageViewerDialog dialog = new ImageViewerDialog(parent, title);
        dialog.setImage(image);
        dialog.setVisible(true);
    }
}
