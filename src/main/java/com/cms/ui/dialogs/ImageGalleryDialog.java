package com.cms.ui.dialogs;

import com.cms.ui.components.ImageViewerPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Diálogo de pantalla completa para visualizar galería de imágenes con
 * controles de zoom y rotación.
 */
public class ImageGalleryDialog extends JDialog {

    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final Color TOOLBAR_BG = new Color(248, 250, 252);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);

    private final List<BufferedImage> images;
    private int currentImageIndex = 0;

    private ImageViewerPanel imageViewer;
    private JLabel imageCounterLabel;
    private JPanel thumbnailContainer;

    public ImageGalleryDialog(Frame parent, List<BufferedImage> images, int initialIndex) {
        super(parent, "Visor de Imágenes", true);
        this.images = images;
        this.currentImageIndex = Math.max(0, Math.min(initialIndex, images.size() - 1));

        initializeUI();

        // Maximize window
        setUndecorated(false);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle screenBounds = ge.getMaximumWindowBounds();
        setSize(screenBounds.width, screenBounds.height);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(30, 30, 30));

        // Header with close button
        add(createHeader(), BorderLayout.NORTH);

        // Main image viewer
        add(createMainContent(), BorderLayout.CENTER);

        // Footer with thumbnails
        add(createFooter(), BorderLayout.SOUTH);

        // Show initial image
        if (!images.isEmpty()) {
            showImage(currentImageIndex);
        }
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Visor de Imágenes");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(PRIMARY);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());

        header.add(title, BorderLayout.WEST);
        header.add(closeBtn, BorderLayout.EAST);

        return header;
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(30, 30, 30));

        // Toolbar
        content.add(createToolbar(), BorderLayout.NORTH);

        // Image viewer
        imageViewer = new ImageViewerPanel();
        content.add(imageViewer, BorderLayout.CENTER);

        return content;
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        toolbar.setBackground(TOOLBAR_BG);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // Navigation buttons
        JButton prevBtn = createToolButton("<", "Imagen anterior");
        prevBtn.addActionListener(e -> showPreviousImage());

        imageCounterLabel = new JLabel("0 / 0");
        imageCounterLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        imageCounterLabel.setForeground(new Color(71, 85, 105));
        imageCounterLabel.setBorder(new EmptyBorder(0, 10, 0, 10));

        JButton nextBtn = createToolButton(">", "Imagen siguiente");
        nextBtn.addActionListener(e -> showNextImage());

        // Zoom controls
        JButton zoomOutBtn = createToolButton("Zoom -", "Alejar imagen");
        zoomOutBtn.addActionListener(e -> imageViewer.zoomOut());

        JButton zoomInBtn = createToolButton("Zoom +", "Acercar imagen");
        zoomInBtn.addActionListener(e -> imageViewer.zoomIn());

        // Rotation controls
        JButton rotateLeftBtn = createToolButton("Rotar Izq", "Rotar 90° izquierda");
        rotateLeftBtn.addActionListener(e -> imageViewer.rotateLeft());

        JButton rotateRightBtn = createToolButton("Rotar Der", "Rotar 90° derecha");
        rotateRightBtn.addActionListener(e -> imageViewer.rotateRight());

        // Reset button
        JButton resetBtn = createToolButton("Restablecer", "Volver a vista original");
        resetBtn.addActionListener(e -> imageViewer.resetView());

        // Add components
        toolbar.add(prevBtn);
        toolbar.add(imageCounterLabel);
        toolbar.add(nextBtn);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(zoomOutBtn);
        toolbar.add(zoomInBtn);
        toolbar.add(Box.createHorizontalStrut(15));
        toolbar.add(rotateLeftBtn);
        toolbar.add(rotateRightBtn);
        toolbar.add(Box.createHorizontalStrut(15));
        toolbar.add(resetBtn);

        return toolbar;
    }

    private JButton createToolButton(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setToolTipText(tooltip);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(51, 65, 85));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(6, 14, 6, 14)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(TOOLBAR_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        // Thumbnails
        thumbnailContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        thumbnailContainer.setOpaque(false);

        for (int i = 0; i < images.size(); i++) {
            final int index = i;
            BufferedImage thumb = createThumbnail(images.get(i), 80, 60);
            JLabel thumbLabel = new JLabel(new ImageIcon(thumb));
            thumbLabel.setBorder(BorderFactory.createLineBorder(
                    i == currentImageIndex ? PRIMARY : new Color(203, 213, 225), 3));
            thumbLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            thumbLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    showImage(index);
                }
            });
            thumbnailContainer.add(thumbLabel);
        }

        JScrollPane scrollPane = new JScrollPane(thumbnailContainer);
        scrollPane.setPreferredSize(new Dimension(0, 90));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(20);

        footer.add(scrollPane, BorderLayout.CENTER);

        // Close button panel
        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        closePanel.setOpaque(false);

        JButton closeBtn = new JButton("Cerrar");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeBtn.setPreferredSize(new Dimension(100, 36));
        closeBtn.setBackground(PRIMARY);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        closePanel.add(closeBtn);

        footer.add(closePanel, BorderLayout.EAST);

        return footer;
    }

    private BufferedImage createThumbnail(BufferedImage original, int width, int height) {
        BufferedImage thumb = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = thumb.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        double scale = Math.min((double) width / original.getWidth(), (double) height / original.getHeight());
        int scaledW = (int) (original.getWidth() * scale);
        int scaledH = (int) (original.getHeight() * scale);
        int x = (width - scaledW) / 2;
        int y = (height - scaledH) / 2;

        g2d.drawImage(original, x, y, scaledW, scaledH, null);
        g2d.dispose();
        return thumb;
    }

    private void showImage(int index) {
        if (index >= 0 && index < images.size()) {
            currentImageIndex = index;
            imageViewer.setImage(images.get(index));
            updateImageCounter();
            updateThumbnailSelection();
        }
    }

    private void showPreviousImage() {
        if (currentImageIndex > 0) {
            showImage(currentImageIndex - 1);
        }
    }

    private void showNextImage() {
        if (currentImageIndex < images.size() - 1) {
            showImage(currentImageIndex + 1);
        }
    }

    private void updateImageCounter() {
        if (!images.isEmpty()) {
            imageCounterLabel.setText((currentImageIndex + 1) + " / " + images.size());
        } else {
            imageCounterLabel.setText("0 / 0");
        }
    }

    private void updateThumbnailSelection() {
        Component[] components = thumbnailContainer.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof JLabel label) {
                label.setBorder(BorderFactory.createLineBorder(
                        i == currentImageIndex ? PRIMARY : new Color(203, 213, 225), 3));
            }
        }
    }
}
