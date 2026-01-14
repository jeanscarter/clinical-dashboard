package com.cms.ui.dialogs;

import com.cms.repository.PatientRepository;
import com.cms.repository.ClinicalHistoryRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ReportsDialog extends JDialog {

    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final Color CARD_BG = Color.WHITE;

    private final PatientRepository patientRepository;
    private final ClinicalHistoryRepository clinicalHistoryRepository;

    public ReportsDialog(Frame parent, PatientRepository patientRepository,
            ClinicalHistoryRepository clinicalHistoryRepository) {
        super(parent, "Reportes", true);
        this.patientRepository = patientRepository;
        this.clinicalHistoryRepository = clinicalHistoryRepository;

        setSize(600, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("📊 Centro de Reportes");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Generar y exportar reportes del sistema");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(219, 234, 254));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        header.add(titlePanel, BorderLayout.WEST);
        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new GridLayout(2, 2, 15, 15));
        content.setBackground(new Color(241, 245, 249));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        content.add(createReportCard(
                "📋 Lista de Pacientes",
                "Exportar lista completa de pacientes registrados",
                "Pacientes: " + patientRepository.count(),
                e -> exportPatientsList()));

        content.add(createReportCard(
                "📝 Historias Clínicas",
                "Exportar historias clínicas por paciente o periodo",
                "Total: " + clinicalHistoryRepository.count(),
                e -> exportClinicalHistories()));

        content.add(createReportCard(
                "📈 Estadísticas Generales",
                "Resumen estadístico del sistema",
                "Vista general",
                e -> showStatistics()));

        content.add(createReportCard(
                "🗓️ Consultas por Periodo",
                "Filtrar consultas por rango de fechas",
                "Personalizable",
                e -> exportByPeriod()));

        return content;
    }

    private JPanel createReportCard(String title, String description, String info,
            java.awt.event.ActionListener action) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(15, 23, 42));

        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(100, 116, 139));

        JLabel infoLabel = new JLabel(info);
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        infoLabel.setForeground(PRIMARY);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(descLabel);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(infoLabel);

        JButton generateBtn = new JButton("Generar");
        generateBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        generateBtn.setBackground(PRIMARY);
        generateBtn.setForeground(Color.WHITE);
        generateBtn.setFocusPainted(false);
        generateBtn.setBorderPainted(false);
        generateBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        generateBtn.addActionListener(action);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(generateBtn);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBorder(new EmptyBorder(10, 20, 10, 20));
        footer.setBackground(CARD_BG);

        JButton closeBtn = new JButton("Cerrar");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        closeBtn.setPreferredSize(new Dimension(100, 35));
        closeBtn.addActionListener(e -> dispose());

        footer.add(closeBtn);
        return footer;
    }

    private void exportPatientsList() {
        JOptionPane.showMessageDialog(this,
                "Función en desarrollo.\n\nEsta característica permitirá exportar la lista de pacientes a PDF o Excel.",
                "Exportar Pacientes",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportClinicalHistories() {
        JOptionPane.showMessageDialog(this,
                "Función en desarrollo.\n\nEsta característica permitirá exportar historias clínicas seleccionadas.",
                "Exportar Historias",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showStatistics() {
        long patients = patientRepository.count();
        long histories = clinicalHistoryRepository.count();
        double avgPerPatient = patients > 0 ? (double) histories / patients : 0;

        String stats = String.format("""
                📊 ESTADÍSTICAS DEL SISTEMA
                ════════════════════════════════

                👥 Total de Pacientes: %d
                📋 Total de Consultas: %d
                📈 Promedio consultas/paciente: %.2f

                ════════════════════════════════
                """, patients, histories, avgPerPatient);

        JTextArea textArea = new JTextArea(stats);
        textArea.setEditable(false);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        textArea.setBackground(new Color(248, 250, 252));

        JOptionPane.showMessageDialog(this, new JScrollPane(textArea),
                "Estadísticas", JOptionPane.PLAIN_MESSAGE);
    }

    private void exportByPeriod() {
        JOptionPane.showMessageDialog(this,
                "Función en desarrollo.\n\nEsta característica permitirá filtrar y exportar consultas por periodo.",
                "Exportar por Periodo",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
