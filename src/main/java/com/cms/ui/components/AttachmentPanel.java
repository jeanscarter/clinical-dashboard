package com.cms.ui.components;

import com.cms.domain.Attachment;
import com.cms.util.ClipboardImageHandler;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AttachmentPanel extends JPanel {

    private static final Color PANEL_BG = new Color(248, 250, 252);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color HOVER_COLOR = new Color(219, 234, 254);
    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final Color DANGER = new Color(239, 68, 68);

    private final List<Attachment> attachments;
    private final JPanel thumbnailsPanel;
    private final ClipboardImageHandler clipboardHandler;

    private Consumer<Void> onPasteFromClipboard;
    private Consumer<String> onAddFromFile;
    private Consumer<Integer> onRemoveAttachment;
    private Consumer<Attachment> onViewAttachment;

    public AttachmentPanel() {
        this.attachments = new ArrayList<>();
        this.clipboardHandler = new ClipboardImageHandler();

        setLayout(new BorderLayout());
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(15, 15, 15, 15)));

        add(createHeader(), BorderLayout.NORTH);

        thumbnailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        thumbnailsPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(thumbnailsPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(0, 150));

        add(scrollPane, BorderLayout.CENTER);

        updateEmptyState();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel title = new JLabel("📎 Adjuntos");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(new Color(15, 23, 42));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonsPanel.setOpaque(false);

        JButton pasteBtn = createButton("📋 Pegar", PRIMARY);
        pasteBtn.setToolTipText("Pegar imagen del portapapeles (Ctrl+V)");
        pasteBtn.addActionListener(e -> {
            if (onPasteFromClipboard != null) {
                onPasteFromClipboard.accept(null);
            }
        });

        JButton addFileBtn = createButton("📁 Archivo", new Color(100, 116, 139));
        addFileBtn.addActionListener(e -> selectFile());

        buttonsPanel.add(pasteBtn);
        buttonsPanel.add(addFileBtn);

        header.add(title, BorderLayout.WEST);
        header.add(buttonsPanel, BorderLayout.EAST);

        return header;
    }

    private JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 30));
        return button;
    }

    private void selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory())
                    return true;
                String name = f.getName().toLowerCase();
                return name.endsWith(".png") || name.endsWith(".jpg") ||
                        name.endsWith(".jpeg") || name.endsWith(".gif") ||
                        name.endsWith(".pdf") || name.endsWith(".doc") ||
                        name.endsWith(".docx");
            }

            @Override
            public String getDescription() {
                return "Imágenes y Documentos";
            }
        });

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (onAddFromFile != null) {
                onAddFromFile.accept(selectedFile.getAbsolutePath());
            }
        }
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments.clear();
        this.attachments.addAll(attachments);
        refreshThumbnails();
    }

    public void addAttachment(Attachment attachment) {
        this.attachments.add(attachment);
        refreshThumbnails();
    }

    private void refreshThumbnails() {
        thumbnailsPanel.removeAll();

        if (attachments.isEmpty()) {
            updateEmptyState();
        } else {
            for (Attachment attachment : attachments) {
                thumbnailsPanel.add(createThumbnail(attachment));
            }
        }

        thumbnailsPanel.revalidate();
        thumbnailsPanel.repaint();
    }

    private void updateEmptyState() {
        if (attachments.isEmpty()) {
            thumbnailsPanel.removeAll();
            JLabel emptyLabel = new JLabel("No hay adjuntos. Use los botones para agregar.");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            emptyLabel.setForeground(new Color(148, 163, 184));
            thumbnailsPanel.add(emptyLabel);
        }
    }

    private JPanel createThumbnail(Attachment attachment) {
        JPanel thumbnail = new JPanel(new BorderLayout());
        thumbnail.setPreferredSize(new Dimension(120, 120));
        thumbnail.setBackground(Color.WHITE);
        thumbnail.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        thumbnail.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);

        if (isImageFile(attachment.getTipo())) {
            loadThumbnailImage(attachment.getRutaArchivo(), imageLabel);
        } else {
            imageLabel.setText(getFileIcon(attachment.getTipo()));
            imageLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        }

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(3, 5, 3, 5));

        String displayName = attachment.getNombre();
        if (displayName.length() > 12) {
            displayName = displayName.substring(0, 10) + "...";
        }

        JLabel nameLabel = new JLabel(displayName);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        nameLabel.setForeground(new Color(71, 85, 105));
        nameLabel.setToolTipText(attachment.getNombre());

        JButton deleteBtn = new JButton("×");
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deleteBtn.setForeground(DANGER);
        deleteBtn.setBackground(null);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.setPreferredSize(new Dimension(20, 20));
        deleteBtn.addActionListener(e -> {
            if (onRemoveAttachment != null) {
                onRemoveAttachment.accept(attachment.getId());
            }
        });

        infoPanel.add(nameLabel, BorderLayout.CENTER);
        infoPanel.add(deleteBtn, BorderLayout.EAST);

        thumbnail.add(imageLabel, BorderLayout.CENTER);
        thumbnail.add(infoPanel, BorderLayout.SOUTH);

        thumbnail.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                thumbnail.setBackground(HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                thumbnail.setBackground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && onViewAttachment != null) {
                    onViewAttachment.accept(attachment);
                }
            }
        });

        return thumbnail;
    }

    private void loadThumbnailImage(String path, JLabel label) {
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() {
                try {
                    BufferedImage img = ImageIO.read(new File(path));
                    if (img != null) {
                        Image scaled = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaled);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        label.setIcon(icon);
                        label.setText("");
                    } else {
                        label.setText("🖼️");
                        label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
                    }
                } catch (Exception e) {
                    label.setText("⚠️");
                }
            }
        };
        worker.execute();
    }

    private boolean isImageFile(String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }

    private String getFileIcon(String mimeType) {
        if (mimeType == null)
            return "📄";
        if (mimeType.contains("pdf"))
            return "📕";
        if (mimeType.contains("word"))
            return "📘";
        if (mimeType.startsWith("image/"))
            return "🖼️";
        return "📄";
    }

    public void setOnPasteFromClipboard(Consumer<Void> handler) {
        this.onPasteFromClipboard = handler;
    }

    public void setOnAddFromFile(Consumer<String> handler) {
        this.onAddFromFile = handler;
    }

    public void setOnRemoveAttachment(Consumer<Integer> handler) {
        this.onRemoveAttachment = handler;
    }

    public void setOnViewAttachment(Consumer<Attachment> handler) {
        this.onViewAttachment = handler;
    }
}
