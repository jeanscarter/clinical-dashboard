package com.cms.ui.dialogs;

import com.cms.domain.Attachment;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * Dialog for viewing clinical history attachments in a gallery format.
 */
public class GalleryDialog extends JDialog {

    private static final Color BACKGROUND = new Color(241, 245, 249);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PRIMARY = new Color(59, 130, 246);

    private final List<Attachment> attachments;
    private JPanel galleryPanel;

    public GalleryDialog(Frame parent, List<Attachment> attachments) {
        super(parent, "Galería de Imágenes", true);
        this.attachments = attachments;
        initUI();
    }

    private void initUI() {
        setSize(900, 700);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Galería de Imágenes (" + attachments.size() + " archivos)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton closeBtn = new JButton("Cerrar");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeBtn.setBackground(new Color(239, 68, 68));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        headerPanel.add(closeBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Gallery content
        galleryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        galleryPanel.setBackground(BACKGROUND);

        if (attachments.isEmpty()) {
            JLabel emptyLabel = new JLabel("No hay imágenes adjuntas");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(new Color(100, 116, 139));
            galleryPanel.add(emptyLabel);
        } else {
            for (Attachment attachment : attachments) {
                JPanel imageCard = createImageCard(attachment);
                galleryPanel.add(imageCard);
            }
        }

        JScrollPane scrollPane = new JScrollPane(galleryPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createImageCard(Attachment attachment) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(10, 10, 10, 10)));
        card.setPreferredSize(new Dimension(200, 220));

        // Load and display thumbnail
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(180, 160));

        try {
            File imageFile = new File(attachment.getRutaArchivo());
            if (imageFile.exists()) {
                BufferedImage img = ImageIO.read(imageFile);
                if (img != null) {
                    ImageIcon thumbnail = createThumbnail(img, 180, 160);
                    imageLabel.setIcon(thumbnail);
                    imageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    imageLabel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            showFullImage(attachment.getNombre(), img);
                        }
                    });
                } else {
                    imageLabel.setText("Sin vista previa");
                }
            } else {
                imageLabel.setText("Archivo no encontrado");
            }
        } catch (Exception e) {
            imageLabel.setText("Error al cargar");
        }

        // File name
        JLabel nameLabel = new JLabel(truncateString(attachment.getNombre(), 25));
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        nameLabel.setForeground(new Color(71, 85, 105));
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(imageLabel, BorderLayout.CENTER);
        card.add(nameLabel, BorderLayout.SOUTH);

        return card;
    }

    private ImageIcon createThumbnail(BufferedImage img, int maxWidth, int maxHeight) {
        int originalWidth = img.getWidth();
        int originalHeight = img.getHeight();

        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        Image scaled = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private void showFullImage(String title, BufferedImage img) {
        JDialog fullImageDialog = new JDialog(this, title, true);
        fullImageDialog.setSize(800, 600);
        fullImageDialog.setLocationRelativeTo(this);
        fullImageDialog.setLayout(new BorderLayout());

        // Scale image to fit dialog while maintaining aspect ratio
        int maxWidth = 780;
        int maxHeight = 560;
        ImageIcon scaledIcon = createThumbnail(img, maxWidth, maxHeight);

        JLabel fullImageLabel = new JLabel(scaledIcon);
        fullImageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane scrollPane = new JScrollPane(fullImageLabel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        fullImageDialog.add(scrollPane, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Cerrar");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeBtn.addActionListener(e -> fullImageDialog.dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.add(closeBtn);
        fullImageDialog.add(btnPanel, BorderLayout.SOUTH);

        fullImageDialog.setVisible(true);
    }

    private String truncateString(String str, int maxLength) {
        if (str == null)
            return "";
        if (str.length() <= maxLength)
            return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}
