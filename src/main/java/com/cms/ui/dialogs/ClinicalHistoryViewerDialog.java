package com.cms.ui.dialogs;

import com.cms.di.AppFactory;
import com.cms.domain.Attachment;
import com.cms.domain.ClinicalHistory;
import com.cms.domain.Patient;
import com.cms.repository.AttachmentRepository;
import com.cms.repository.PatientRepository;
import com.cms.ui.components.IconFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Diálogo de pantalla completa para visualizar los detalles de una historia
 * clínica.
 * Diseño moderno y llamativo con componentes visuales avanzados.
 */
public class ClinicalHistoryViewerDialog extends JDialog {

    // Color palette - Modern blue theme
    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final Color PRIMARY_DARK = new Color(37, 99, 235);
    private static final Color PRIMARY_LIGHT = new Color(147, 197, 253);
    private static final Color ACCENT_GREEN = new Color(34, 197, 94);
    private static final Color ACCENT_ORANGE = new Color(249, 115, 22);
    private static final Color ACCENT_PURPLE = new Color(139, 92, 246);
    private static final Color ACCENT_PINK = new Color(236, 72, 153);
    private static final Color BG_GRADIENT_START = new Color(248, 250, 252);
    private static final Color BG_GRADIENT_END = new Color(226, 232, 240);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 32);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 18);
    private static final Font SECTION_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font VALUE_FONT = new Font("Segoe UI", Font.PLAIN, 18);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 13);

    private final ClinicalHistory history;
    private final List<BufferedImage> images;
    private final String patientName;
    private final Integer patientAge;

    public ClinicalHistoryViewerDialog(Frame parent, ClinicalHistory history) {
        super(parent, "Detalles de Consulta", true);
        this.history = history;

        AppFactory factory = AppFactory.getInstance();
        AttachmentRepository attachmentRepo = factory.getAttachmentRepository();
        PatientRepository patientRepo = factory.getPatientRepository();

        // Load attachments and convert to images
        List<Attachment> attachments = attachmentRepo.findByClinicalHistoryId(history.getId());
        this.images = new ArrayList<>();

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

        // Get patient name and age
        Patient patient = patientRepo.findById(history.getPatientId()).orElse(null);
        this.patientName = patient != null ? patient.getNombreCompleto() : "Desconocido";
        this.patientAge = patient != null ? patient.getAge() : null;

        // Fullscreen setup
        setUndecorated(false);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle screenBounds = ge.getMaximumWindowBounds();
        setSize(screenBounds.width, screenBounds.height);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Main panel with gradient background
        JPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(createHeader(), BorderLayout.NORTH);
        mainPanel.add(createContent(), BorderLayout.CENTER);
        mainPanel.add(createFooter(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    // Custom gradient panel
    private class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gradient = new GradientPaint(0, 0, BG_GRADIENT_START, 0, getHeight(), BG_GRADIENT_END);
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.dispose();
        }
    }

    private JPanel createHeader() {
        // Header with gradient
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gradient = new GradientPaint(0, 0, PRIMARY, getWidth(), 0, PRIMARY_DARK);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(25, 40, 25, 40));

        // Left side - Title and info
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        // Patient badge
        JPanel patientBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        patientBadge.setOpaque(false);
        patientBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel patientIcon = new JLabel(IconFactory.createUserIcon(24, Color.WHITE));
        JLabel patientLabel = new JLabel(patientName);
        patientLabel.setFont(TITLE_FONT);
        patientLabel.setForeground(Color.WHITE);

        patientBadge.add(patientIcon);
        patientBadge.add(patientLabel);

        // Date and ID badge
        JPanel infoBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        infoBadge.setOpaque(false);
        infoBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Date chip
        JPanel dateChip = createInfoChip(
                IconFactory.createCalendarIcon(16, PRIMARY_LIGHT),
                history.getFechaConsulta() != null ? history.getFechaConsulta().format(DATE_FORMATTER) : "Sin fecha");

        // ID chip
        JPanel idChip = createInfoChip(
                IconFactory.createDocumentIcon(16, PRIMARY_LIGHT),
                "ID: " + history.getId());

        // Age chip
        if (patientAge != null) {
            JPanel ageChip = createInfoChip(
                    IconFactory.createUserIcon(16, PRIMARY_LIGHT),
                    patientAge + " años");
            infoBadge.add(ageChip);
        }

        // Images count chip
        if (!images.isEmpty()) {
            JPanel imagesChip = createInfoChip(null, "📷 " + images.size() + " imágenes");
            infoBadge.add(imagesChip);
        }

        infoBadge.add(dateChip);
        infoBadge.add(idChip);

        titlePanel.add(patientBadge);
        titlePanel.add(Box.createVerticalStrut(10));
        titlePanel.add(infoBadge);

        // Right side - Status badge
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusPanel.setOpaque(false);

        JLabel statusBadge = new JLabel("✓ Consulta Registrada");
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusBadge.setForeground(Color.WHITE);
        statusBadge.setOpaque(true);
        statusBadge.setBackground(ACCENT_GREEN);
        statusBadge.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(20, ACCENT_GREEN),
                new EmptyBorder(8, 16, 8, 16)));

        statusPanel.add(statusBadge);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(statusPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createInfoChip(Icon icon, String text) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        chip.setOpaque(false);

        if (icon != null) {
            chip.add(new JLabel(icon));
        }

        JLabel label = new JLabel(text);
        label.setFont(SUBTITLE_FONT);
        label.setForeground(PRIMARY_LIGHT);
        chip.add(label);

        return chip;
    }

    private JScrollPane createContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Grid of cards - 2 columns
        JPanel cardGrid = new JPanel(new GridLayout(0, 2, 25, 25));
        cardGrid.setOpaque(false);

        // Main consultation card
        cardGrid.add(createSectionCard("📋 Motivo de Consulta", history.getMotivoConsulta(), PRIMARY, true));
        cardGrid.add(createSectionCard("🔬 Diagnóstico", history.getDiagnostico(), ACCENT_PURPLE, true));
        cardGrid.add(createSectionCard("📜 Antecedentes", history.getAntecedentes(), ACCENT_ORANGE, false));
        cardGrid.add(createSectionCard("🩺 Examen Físico", history.getExamenFisico(), ACCENT_GREEN, false));
        cardGrid.add(createSectionCard("💊 Conducta", history.getConducta(), ACCENT_PINK, false));
        cardGrid.add(createSectionCard("📝 Observaciones", history.getObservaciones(), TEXT_SECONDARY, false));

        content.add(cardGrid);

        // Doctor info bar
        content.add(Box.createVerticalStrut(25));
        content.add(createDoctorBar());

        // Image gallery section
        if (!images.isEmpty()) {
            content.add(Box.createVerticalStrut(25));
            content.add(createImageGallerySection());
        }

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        return scrollPane;
    }

    private JPanel createSectionCard(String title, String value, Color accentColor, boolean highlight) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fill(new RoundRectangle2D.Float(4, 4, getWidth() - 4, getHeight() - 4, 16, 16));

                // Card background
                g2d.setColor(CARD_BG);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 4, 16, 16));

                // Top accent bar
                g2d.setColor(accentColor);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, 6, 16, 16));
                g2d.fillRect(0, 4, getWidth() - 4, 4);

                g2d.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setOpaque(false);

        // Title with icon
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(SECTION_TITLE_FONT);
        titleLabel.setForeground(accentColor);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Separator
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setForeground(BORDER_COLOR);

        // Value
        String displayValue = (value != null && !value.isEmpty()) ? value : "N/A";
        JTextArea valueArea = new JTextArea(displayValue);
        valueArea.setFont(highlight ? new Font("Segoe UI", Font.PLAIN, 20) : VALUE_FONT);
        valueArea.setForeground(displayValue.equals("N/A") ? TEXT_SECONDARY : TEXT_PRIMARY);
        valueArea.setLineWrap(true);
        valueArea.setWrapStyleWord(true);
        valueArea.setEditable(false);
        valueArea.setOpaque(false);
        valueArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(separator);
        card.add(Box.createVerticalStrut(15));
        card.add(valueArea);

        return card;
    }

    private JPanel createDoctorBar() {
        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background
                GradientPaint gradient = new GradientPaint(0, 0, PRIMARY, getWidth(), 0, ACCENT_PURPLE);
                g2d.setPaint(gradient);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));

                g2d.dispose();
            }
        };
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 15));
        bar.setOpaque(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // Doctor icon
        JLabel doctorIcon = new JLabel(IconFactory.createUserIcon(32, Color.WHITE));

        // Doctor info
        JPanel doctorInfo = new JPanel();
        doctorInfo.setLayout(new BoxLayout(doctorInfo, BoxLayout.Y_AXIS));
        doctorInfo.setOpaque(false);

        JLabel label = new JLabel("Médico Tratante");
        label.setFont(LABEL_FONT);
        label.setForeground(PRIMARY_LIGHT);

        String doctorName = history.getMedico();
        JLabel nameLabel = new JLabel(doctorName != null && !doctorName.isEmpty() ? doctorName : "No especificado");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        nameLabel.setForeground(Color.WHITE);

        doctorInfo.add(label);
        doctorInfo.add(nameLabel);

        bar.add(doctorIcon);
        bar.add(doctorInfo);

        return bar;
    }

    private JPanel createImageGallerySection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Section title
        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleBar.setOpaque(false);
        titleBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel iconLabel = new JLabel("📷");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JLabel titleLabel = new JLabel("Imágenes Adjuntas");
        titleLabel.setFont(SECTION_TITLE_FONT);
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel countBadge = new JLabel(String.valueOf(images.size()));
        countBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        countBadge.setForeground(Color.WHITE);
        countBadge.setOpaque(true);
        countBadge.setBackground(PRIMARY);
        countBadge.setBorder(new EmptyBorder(4, 10, 4, 10));

        titleBar.add(iconLabel);
        titleBar.add(titleLabel);
        titleBar.add(Box.createHorizontalStrut(10));
        titleBar.add(countBadge);

        // Thumbnail gallery with cards
        JPanel gallery = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        gallery.setOpaque(false);
        gallery.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i < images.size(); i++) {
            final int index = i;
            JPanel thumbCard = createThumbnailCard(images.get(i), i + 1);
            thumbCard.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    openImageGallery(index);
                }
            });
            gallery.add(thumbCard);
        }

        section.add(titleBar);
        section.add(Box.createVerticalStrut(15));
        section.add(gallery);

        return section;
    }

    private JPanel createThumbnailCard(BufferedImage image, int number) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fill(new RoundRectangle2D.Float(4, 4, getWidth() - 4, getHeight() - 4, 12, 12));

                // Background
                g2d.setColor(CARD_BG);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 4, 12, 12));

                g2d.dispose();
            }
        };
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(160, 140));
        card.setOpaque(false);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Thumbnail image
        BufferedImage thumb = createThumbnail(image, 140, 100);
        JLabel thumbLabel = new JLabel(new ImageIcon(thumb));
        thumbLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Number badge overlay
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(140, 100));

        thumbLabel.setBounds(0, 0, 140, 100);
        layeredPane.add(thumbLabel, JLayeredPane.DEFAULT_LAYER);

        JLabel numberBadge = new JLabel(String.valueOf(number));
        numberBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        numberBadge.setForeground(Color.WHITE);
        numberBadge.setOpaque(true);
        numberBadge.setBackground(PRIMARY);
        numberBadge.setBorder(new EmptyBorder(2, 6, 2, 6));
        numberBadge.setBounds(5, 5, 22, 18);
        layeredPane.add(numberBadge, JLayeredPane.PALETTE_LAYER);

        card.add(layeredPane, BorderLayout.CENTER);

        // Hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(12, PRIMARY),
                        new EmptyBorder(6, 6, 6, 6)));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBorder(new EmptyBorder(8, 8, 8, 8));
            }
        });

        return card;
    }

    private BufferedImage createThumbnail(BufferedImage original, int width, int height) {
        BufferedImage thumb = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = thumb.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setColor(new Color(240, 240, 240));
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

    private void openImageGallery(int initialIndex) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        ImageGalleryDialog gallery = new ImageGalleryDialog(owner, images, initialIndex);
        gallery.setVisible(true);
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(new EmptyBorder(20, 40, 20, 40));
        footer.setBackground(CARD_BG);

        // Left side - Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        actionPanel.setOpaque(false);

        // Print button with icon
        JButton printBtn = createStyledButton("🖨️ Imprimir", ACCENT_GREEN, Color.WHITE);
        printBtn.addActionListener(e -> printHistory());
        actionPanel.add(printBtn);

        footer.add(actionPanel, BorderLayout.WEST);

        // Right side - Close button
        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closePanel.setOpaque(false);

        JButton closeBtn = createStyledButton("Cerrar", PRIMARY, Color.WHITE);
        closeBtn.setPreferredSize(new Dimension(140, 48));
        closeBtn.addActionListener(e -> dispose());
        closePanel.add(closeBtn);

        footer.add(closePanel, BorderLayout.EAST);
        return footer;
    }

    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(bgColor.brighter());
                } else {
                    g2d.setColor(bgColor);
                }

                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2d.dispose();

                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(fgColor);
        btn.setPreferredSize(new Dimension(140, 48));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void printHistory() {
        JOptionPane.showMessageDialog(this,
                "Función de impresión en desarrollo.\nSe implementará con JasperReports.",
                "Imprimir",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Custom rounded border
    private static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(2));
            g2d.draw(new RoundRectangle2D.Float(x, y, width - 1, height - 1, radius, radius));
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 2, 2);
        }
    }
}
