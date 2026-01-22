package com.cms.ui.dialogs;

import com.cms.di.AppFactory;
import com.cms.domain.Attachment;
import com.cms.domain.ClinicalHistory;
import com.cms.domain.Patient;
import com.cms.repository.AttachmentRepository;
import com.cms.repository.PatientRepository;
import com.cms.ui.components.IconFactory;
import com.cms.ui.components.ImageViewerPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Diálogo para visualizar los detalles de una historia clínica con galería de
 * imágenes.
 */
public class ClinicalHistoryViewerDialog extends JDialog {

    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final Color BG_DARK = new Color(30, 30, 30);
    private static final Color CARD_BG = Color.WHITE;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ClinicalHistory history;
    private final List<Attachment> attachments;
    private final List<BufferedImage> images;
    private int currentImageIndex = 0;

    private ImageViewerPanel imageViewer;
    private JLabel imageCounterLabel;
    private JPanel thumbnailPanel;

    public ClinicalHistoryViewerDialog(Frame parent, ClinicalHistory history) {
        super(parent, "Detalles de Consulta", true);
        this.history = history;

        AppFactory factory = AppFactory.getInstance();
        AttachmentRepository attachmentRepo = factory.getAttachmentRepository();
        PatientRepository patientRepo = factory.getPatientRepository();

        // Load attachments
        this.attachments = attachmentRepo.findByClinicalHistoryId(history.getId());
        this.images = new ArrayList<>();

        // Convert attachments to images
        for (Attachment att : attachments) {
            if (att.getTipo() != null && att.getTipo().startsWith("image/")) {
                try {
                    java.io.File imageFile = new java.io.File(att.getRutaArchivo());
                    if (imageFile.exists()) {
                        BufferedImage img = ImageIO.read(imageFile);
                        if (img != null) {
                            images.add(img);
                        }
                    }
                } catch (IOException e) {
                    // Skip invalid images
                }
            }
        }

        // Get patient name
        String patientName = patientRepo.findById(history.getPatientId())
                .map(Patient::getNombreCompleto)
                .orElse("Desconocido");

        setSize(1000, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        add(createHeader(patientName), BorderLayout.NORTH);
        add(createContent(patientName), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader(String patientName) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Consulta - " + patientName);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        String dateStr = history.getFechaConsulta() != null
                ? history.getFechaConsulta().format(DATE_FORMATTER)
                : "Sin fecha";
        JLabel dateLabel = new JLabel(dateStr);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(new Color(219, 234, 254));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(dateLabel);

        header.add(titlePanel, BorderLayout.WEST);
        return header;
    }

    private JPanel createContent(String patientName) {
        JPanel content = new JPanel(new BorderLayout(15, 0));
        content.setBackground(new Color(241, 245, 249));
        content.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Left panel - Info
        JPanel infoPanel = createInfoPanel(patientName);
        infoPanel.setPreferredSize(new Dimension(320, 0));

        // Right panel - Image viewer
        JPanel imagePanel = createImagePanel();

        content.add(infoPanel, BorderLayout.WEST);
        content.add(imagePanel, BorderLayout.CENTER);

        return content;
    }

    private JPanel createInfoPanel(String patientName) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(15, 15, 15, 15)));

        addInfoSection(panel, "Motivo de Consulta", history.getMotivoConsulta());
        addInfoSection(panel, "Antecedentes", history.getAntecedentes());
        addInfoSection(panel, "Examen Físico", history.getExamenFisico());
        addInfoSection(panel, "Diagnóstico", history.getDiagnostico());
        addInfoSection(panel, "Conducta", history.getConducta());
        addInfoSection(panel, "Observaciones", history.getObservaciones());
        addInfoSection(panel, "Médico", history.getMedico());

        panel.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private void addInfoSection(JPanel panel, String label, String value) {
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelComp.setForeground(new Color(100, 116, 139));
        labelComp.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea valueComp = new JTextArea(value != null && !value.isEmpty() ? value : "N/A");
        valueComp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        valueComp.setForeground(new Color(15, 23, 42));
        valueComp.setLineWrap(true);
        valueComp.setWrapStyleWord(true);
        valueComp.setEditable(false);
        valueComp.setOpaque(false);
        valueComp.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueComp.setMaximumSize(new Dimension(280, 100));

        panel.add(labelComp);
        panel.add(Box.createVerticalStrut(4));
        panel.add(valueComp);
        panel.add(Box.createVerticalStrut(12));
    }

    private JPanel createImagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));

        // Toolbar
        JPanel toolbar = createImageToolbar();
        panel.add(toolbar, BorderLayout.NORTH);

        // Image viewer
        imageViewer = new ImageViewerPanel();
        panel.add(imageViewer, BorderLayout.CENTER);

        // Thumbnails
        if (!images.isEmpty()) {
            thumbnailPanel = createThumbnailPanel();
            panel.add(thumbnailPanel, BorderLayout.SOUTH);
            showImage(0);
        } else {
            JLabel noImages = new JLabel("Sin imágenes adjuntas", SwingConstants.CENTER);
            noImages.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            noImages.setForeground(new Color(100, 116, 139));
            panel.add(noImages, BorderLayout.CENTER);
        }

        return panel;
    }

    private JPanel createImageToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        toolbar.setBackground(new Color(248, 250, 252));
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        JButton prevBtn = createTextToolButton("<", "Imagen anterior");
        prevBtn.addActionListener(e -> showPreviousImage());

        JButton nextBtn = createTextToolButton(">", "Imagen siguiente");
        nextBtn.addActionListener(e -> showNextImage());

        imageCounterLabel = new JLabel("0 / 0");
        imageCounterLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        imageCounterLabel.setForeground(new Color(71, 85, 105));
        imageCounterLabel.setBorder(new EmptyBorder(0, 8, 0, 8));

        JButton zoomInBtn = createTextToolButton("Zoom +", "Acercar imagen");
        zoomInBtn.addActionListener(e -> imageViewer.zoomIn());

        JButton zoomOutBtn = createTextToolButton("Zoom -", "Alejar imagen");
        zoomOutBtn.addActionListener(e -> imageViewer.zoomOut());

        JButton rotateLeftBtn = createTextToolButton("Rotar Izq", "Rotar 90° izquierda");
        rotateLeftBtn.addActionListener(e -> imageViewer.rotateLeft());

        JButton rotateRightBtn = createTextToolButton("Rotar Der", "Rotar 90° derecha");
        rotateRightBtn.addActionListener(e -> imageViewer.rotateRight());

        JButton resetBtn = createTextToolButton("Restablecer", "Volver a vista original");
        resetBtn.addActionListener(e -> imageViewer.resetView());

        toolbar.add(prevBtn);
        toolbar.add(imageCounterLabel);
        toolbar.add(nextBtn);
        toolbar.add(Box.createHorizontalStrut(15));
        toolbar.add(zoomOutBtn);
        toolbar.add(zoomInBtn);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(rotateLeftBtn);
        toolbar.add(rotateRightBtn);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(resetBtn);

        return toolbar;
    }

    private JButton createTextToolButton(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setToolTipText(tooltip);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(51, 65, 85));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(4, 10, 4, 10)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createThumbnailPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        for (int i = 0; i < images.size(); i++) {
            final int index = i;
            BufferedImage thumb = createThumbnail(images.get(i), 60, 45);
            JLabel thumbLabel = new JLabel(new ImageIcon(thumb));
            thumbLabel.setBorder(BorderFactory.createLineBorder(
                    i == currentImageIndex ? PRIMARY : new Color(203, 213, 225), 2));
            thumbLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            thumbLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    showImage(index);
                }
            });
            panel.add(thumbLabel);
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setPreferredSize(new Dimension(0, 65));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
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
        if (thumbnailPanel != null) {
            Component[] components = ((JPanel) ((JScrollPane) thumbnailPanel.getComponent(0)).getViewport().getView())
                    .getComponents();
            for (int i = 0; i < components.length; i++) {
                if (components[i] instanceof JLabel label) {
                    label.setBorder(BorderFactory.createLineBorder(
                            i == currentImageIndex ? PRIMARY : new Color(203, 213, 225), 2));
                }
            }
        }
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBorder(new EmptyBorder(10, 15, 10, 15));
        footer.setBackground(CARD_BG);

        JButton closeBtn = new JButton("Cerrar");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeBtn.setPreferredSize(new Dimension(100, 36));
        closeBtn.setBackground(PRIMARY);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.addActionListener(e -> dispose());

        footer.add(closeBtn);
        return footer;
    }
}
