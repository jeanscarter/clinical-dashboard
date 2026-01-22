package com.cms.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Panel para visualizar imágenes con soporte para zoom, rotación y pan.
 */
public class ImageViewerPanel extends JPanel {

    private BufferedImage originalImage;
    private BufferedImage displayImage;
    private double zoomLevel = 1.0;
    private int rotationAngle = 0; // 0, 90, 180, 270
    private Point panOffset = new Point(0, 0);
    private Point dragStart = null;

    private static final double ZOOM_INCREMENT = 0.2;
    private static final double MIN_ZOOM = 0.1;
    private static final double MAX_ZOOM = 5.0;

    public ImageViewerPanel() {
        setBackground(new Color(30, 30, 30));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Mouse wheel for zoom
        addMouseWheelListener(e -> {
            if (originalImage != null) {
                if (e.getWheelRotation() < 0) {
                    zoomIn();
                } else {
                    zoomOut();
                }
            }
        });

        // Mouse drag for pan
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStart = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragStart = null;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart != null && zoomLevel > 1.0) {
                    int dx = e.getX() - dragStart.x;
                    int dy = e.getY() - dragStart.y;
                    panOffset.x += dx;
                    panOffset.y += dy;
                    dragStart = e.getPoint();
                    repaint();
                }
            }
        });
    }

    public void setImage(BufferedImage image) {
        this.originalImage = image;
        this.rotationAngle = 0;
        this.zoomLevel = 1.0;
        this.panOffset = new Point(0, 0);
        updateDisplayImage();
        repaint();
    }

    private void updateDisplayImage() {
        if (originalImage == null) {
            displayImage = null;
            return;
        }

        // Apply rotation
        if (rotationAngle == 0) {
            displayImage = originalImage;
        } else {
            int w = originalImage.getWidth();
            int h = originalImage.getHeight();

            int newW = (rotationAngle == 90 || rotationAngle == 270) ? h : w;
            int newH = (rotationAngle == 90 || rotationAngle == 270) ? w : h;

            displayImage = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = displayImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            AffineTransform transform = new AffineTransform();
            transform.translate(newW / 2.0, newH / 2.0);
            transform.rotate(Math.toRadians(rotationAngle));
            transform.translate(-w / 2.0, -h / 2.0);

            g2d.setTransform(transform);
            g2d.drawImage(originalImage, 0, 0, null);
            g2d.dispose();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (displayImage == null) {
            // Draw placeholder
            g.setColor(new Color(60, 60, 60));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            String msg = "Sin imagen";
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(msg)) / 2;
            int y = getHeight() / 2;
            g.drawString(msg, x, y);
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Calculate scaled dimensions
        int imgW = displayImage.getWidth();
        int imgH = displayImage.getHeight();

        // Fit to panel if zoom is 1.0
        double fitScale = Math.min((double) getWidth() / imgW, (double) getHeight() / imgH);
        double scale = fitScale * zoomLevel;

        int scaledW = (int) (imgW * scale);
        int scaledH = (int) (imgH * scale);

        // Center the image
        int x = (getWidth() - scaledW) / 2 + panOffset.x;
        int y = (getHeight() - scaledH) / 2 + panOffset.y;

        g2d.drawImage(displayImage, x, y, scaledW, scaledH, null);
        g2d.dispose();
    }

    public void zoomIn() {
        if (zoomLevel < MAX_ZOOM) {
            zoomLevel = Math.min(zoomLevel + ZOOM_INCREMENT, MAX_ZOOM);
            repaint();
        }
    }

    public void zoomOut() {
        if (zoomLevel > MIN_ZOOM) {
            zoomLevel = Math.max(zoomLevel - ZOOM_INCREMENT, MIN_ZOOM);
            if (zoomLevel <= 1.0) {
                panOffset = new Point(0, 0);
            }
            repaint();
        }
    }

    public void rotateLeft() {
        rotationAngle = (rotationAngle - 90 + 360) % 360;
        updateDisplayImage();
        repaint();
    }

    public void rotateRight() {
        rotationAngle = (rotationAngle + 90) % 360;
        updateDisplayImage();
        repaint();
    }

    public void resetView() {
        zoomLevel = 1.0;
        rotationAngle = 0;
        panOffset = new Point(0, 0);
        updateDisplayImage();
        repaint();
    }

    public double getZoomLevel() {
        return zoomLevel;
    }

    public int getRotationAngle() {
        return rotationAngle;
    }
}
