package com.cms.ui.views;

import com.cms.di.AppFactory;
import com.cms.repository.PatientRepository;
import com.cms.repository.ClinicalHistoryRepository;
import com.cms.ui.MainFrame;
import com.cms.ui.components.IconFactory;
import com.cms.ui.dialogs.ReportsDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DashboardView extends JPanel {

    private static final Color CARD_BG = Color.WHITE;
    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color WARNING = new Color(234, 179, 8);
    private static final Color INFO = new Color(6, 182, 212);

    private final PatientRepository patientRepository;
    private final ClinicalHistoryRepository clinicalHistoryRepository;
    private final MainFrame mainFrame;

    public DashboardView(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        AppFactory factory = AppFactory.getInstance();
        this.patientRepository = factory.getPatientRepository();
        this.clinicalHistoryRepository = factory.getClinicalHistoryRepository();

        setLayout(new BorderLayout());
        setBackground(new Color(241, 245, 249));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 25, 0));

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(15, 23, 42));

        JLabel subtitle = new JLabel("Bienvenido al Sistema de Gestión Clínica");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 116, 139));

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
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);

        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 20, 0));
        statsGrid.setOpaque(false);

        long totalPatients = patientRepository.count();
        long totalConsultas = clinicalHistoryRepository.count();
        long consultasHoy = countConsultasHoy();

        statsGrid.add(createStatCard("Total Pacientes", String.valueOf(totalPatients),
                IconFactory.createUsersIcon(24, new Color(100, 116, 139)), PRIMARY));
        statsGrid.add(createStatCard("Consultas Hoy", String.valueOf(consultasHoy),
                IconFactory.createCalendarIcon(24, new Color(100, 116, 139)), SUCCESS));
        statsGrid.add(createStatCard("Total Consultas", String.valueOf(totalConsultas),
                IconFactory.createDocumentIcon(24, new Color(100, 116, 139)), INFO));
        statsGrid.add(createStatCard("Pendientes", "0",
                IconFactory.createClockIcon(24, new Color(100, 116, 139)), WARNING));

        content.add(statsGrid, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new GridLayout(1, 2, 20, 0));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(25, 0, 0, 0));

        mainContent.add(createRecentPatientsCard());
        mainContent.add(createQuickActionsCard());

        content.add(mainContent, BorderLayout.CENTER);

        return content;
    }

    private JPanel createStatCard(String title, String value, Icon icon, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(20, 20, 20, 20)));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setPreferredSize(new Dimension(28, 28));

        JPanel accentDot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(accentColor);
                g2d.fillOval(0, 0, 12, 12);
            }
        };
        accentDot.setPreferredSize(new Dimension(12, 12));
        accentDot.setOpaque(false);

        topRow.add(iconLabel, BorderLayout.WEST);
        topRow.add(accentDot, BorderLayout.EAST);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(new Color(15, 23, 42));
        valueLabel.setBorder(new EmptyBorder(10, 0, 5, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(new Color(100, 116, 139));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.add(valueLabel);
        centerPanel.add(titleLabel);

        card.add(topRow, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createRecentPatientsCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel title = new JLabel("Pacientes Recientes");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(15, 23, 42));
        title.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        var recentPatients = patientRepository.findAll();
        int count = 0;
        for (var patient : recentPatients) {
            if (count >= 5)
                break;
            listPanel.add(createPatientRow(patient.getNombreCompleto(), patient.getCedula()));
            listPanel.add(Box.createVerticalStrut(10));
            count++;
        }

        if (recentPatients.isEmpty()) {
            JLabel emptyLabel = new JLabel("No hay pacientes registrados");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            emptyLabel.setForeground(new Color(148, 163, 184));
            listPanel.add(emptyLabel);
        }

        card.add(title, BorderLayout.NORTH);
        card.add(new JScrollPane(listPanel), BorderLayout.CENTER);

        return card;
    }

    private JPanel createPatientRow(String name, String cedula) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameLabel.setForeground(new Color(15, 23, 42));

        JLabel cedulaLabel = new JLabel(cedula);
        cedulaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cedulaLabel.setForeground(new Color(100, 116, 139));

        row.add(nameLabel, BorderLayout.WEST);
        row.add(cedulaLabel, BorderLayout.EAST);

        return row;
    }

    private JPanel createQuickActionsCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel title = new JLabel("Acciones Rápidas");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(15, 23, 42));
        title.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel actionsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        actionsPanel.setOpaque(false);

        JButton newPatientBtn = createActionButton("Nuevo Paciente",
                IconFactory.createPlusIcon(16, Color.WHITE), PRIMARY);
        newPatientBtn.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.navigateToWithAction("patients", "new_patient", null);
            }
        });

        JButton newConsultaBtn = createActionButton("Nueva Consulta",
                IconFactory.createDocumentIcon(16, Color.WHITE), SUCCESS);
        newConsultaBtn.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.navigateToWithAction("history", "new_consultation", null);
            }
        });

        JButton searchPatientBtn = createActionButton("Buscar Paciente",
                IconFactory.createSearchIcon(16, Color.WHITE), INFO);
        searchPatientBtn.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.navigateToWithAction("patients", "search_patient", null);
            }
        });

        JButton reportsBtn = createActionButton("Reportes",
                IconFactory.createReportIcon(16, Color.WHITE), WARNING);
        reportsBtn.addActionListener(e -> showReportsDialog());

        actionsPanel.add(newPatientBtn);
        actionsPanel.add(newConsultaBtn);
        actionsPanel.add(searchPatientBtn);
        actionsPanel.add(reportsBtn);

        card.add(title, BorderLayout.NORTH);
        card.add(actionsPanel, BorderLayout.CENTER);

        return card;
    }

    private JButton createActionButton(String text, Icon icon, Color bgColor) {
        JButton button = new JButton(text, icon);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setIconTextGap(8);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(0, 50));
        return button;
    }

    private void showReportsDialog() {
        ReportsDialog dialog = new ReportsDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                patientRepository,
                clinicalHistoryRepository);
        dialog.setVisible(true);
    }

    private long countConsultasHoy() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return clinicalHistoryRepository.findByFechaConsultaBetween(startOfDay, endOfDay).size();
    }
}
