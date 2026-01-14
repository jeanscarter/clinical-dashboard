package com.cms.ui.views;

import com.cms.di.AppFactory;
import com.cms.domain.ClinicalHistory;
import com.cms.domain.Patient;
import com.cms.repository.ClinicalHistoryRepository;
import com.cms.repository.PatientRepository;
import com.cms.ui.MainFrame;
import com.cms.ui.dialogs.ClinicalHistoryDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClinicalHistoryView extends JPanel implements MainFrame.RefreshableView {

    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ClinicalHistoryRepository historyRepository;
    private final PatientRepository patientRepository;
    private final MainFrame mainFrame;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JComboBox<PatientItem> patientCombo;

    public ClinicalHistoryView(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        AppFactory factory = AppFactory.getInstance();
        this.historyRepository = factory.getClinicalHistoryRepository();
        this.patientRepository = factory.getPatientRepository();

        setLayout(new BorderLayout());
        setBackground(new Color(241, 245, 249));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        loadHistories();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 25, 0));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Historias Clínicas");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(15, 23, 42));

        JLabel subtitle = new JLabel("Gestionar consultas y registros médicos");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 116, 139));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        JButton addButton = new JButton("➕ Nueva Consulta");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addButton.setBackground(PRIMARY);
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorderPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.setPreferredSize(new Dimension(160, 40));
        addButton.addActionListener(e -> showNewConsultationDialog());

        header.add(titlePanel, BorderLayout.WEST);
        header.add(addButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(20, 20, 20, 20)));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setOpaque(false);
        filterPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        filterPanel.add(new JLabel("Filtrar por paciente:"));
        patientCombo = new JComboBox<>();
        patientCombo.addItem(new PatientItem(null, "Todos los pacientes"));
        for (Patient p : patientRepository.findAll()) {
            patientCombo.addItem(new PatientItem(p.getId(), p.getNombreCompleto() + " - " + p.getCedula()));
        }
        patientCombo.setPreferredSize(new Dimension(300, 35));
        patientCombo.addActionListener(e -> filterByPatient());
        filterPanel.add(patientCombo);

        String[] columns = { "ID", "Paciente", "Fecha", "Motivo", "Diagnóstico", "Médico", "Acciones" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        historyTable.setRowHeight(45);
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        historyTable.getTableHeader().setBackground(new Color(248, 250, 252));
        historyTable.setSelectionBackground(new Color(219, 234, 254));
        historyTable.setGridColor(new Color(226, 232, 240));

        historyTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(200);
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        historyTable.getColumnModel().getColumn(6).setPreferredWidth(100);

        historyTable.getColumnModel().getColumn(6)
                .setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
                    JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
                    panel.setOpaque(false);

                    JButton viewBtn = new JButton("👁️");
                    viewBtn.setToolTipText("Ver detalles");
                    viewBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
                    viewBtn.setPreferredSize(new Dimension(35, 30));
                    viewBtn.setBackground(new Color(219, 234, 254));
                    viewBtn.setBorderPainted(false);

                    JButton editBtn = new JButton("✏️");
                    editBtn.setToolTipText("Editar");
                    editBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
                    editBtn.setPreferredSize(new Dimension(35, 30));
                    editBtn.setBackground(new Color(254, 243, 199));
                    editBtn.setBorderPainted(false);

                    panel.add(viewBtn);
                    panel.add(editBtn);
                    return panel;
                });

        historyTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int column = historyTable.columnAtPoint(e.getPoint());
                int row = historyTable.rowAtPoint(e.getPoint());
                if (column == 6 && row >= 0) {
                    int x = e.getPoint().x - historyTable.getCellRect(row, column, true).x;
                    if (x < 45) {
                        viewHistoryDetails(row);
                    } else if (x < 90) {
                        editHistory(row);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        content.add(filterPanel, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);

        return content;
    }

    private void loadHistories() {
        tableModel.setRowCount(0);
        List<ClinicalHistory> histories = historyRepository.findAll();
        for (ClinicalHistory history : histories) {
            String patientName = "";
            if (history.getPatientId() != null) {
                patientName = patientRepository.findById(history.getPatientId())
                        .map(Patient::getNombreCompleto)
                        .orElse("Desconocido");
            }

            tableModel.addRow(new Object[] {
                    history.getId(),
                    patientName,
                    history.getFechaConsulta() != null ? history.getFechaConsulta().format(DATE_FORMATTER) : "",
                    history.getMotivoConsulta(),
                    history.getDiagnostico(),
                    history.getMedico(),
                    ""
            });
        }
    }

    private void filterByPatient() {
        PatientItem selected = (PatientItem) patientCombo.getSelectedItem();
        if (selected == null || selected.id == null) {
            loadHistories();
        } else {
            tableModel.setRowCount(0);
            List<ClinicalHistory> histories = historyRepository.findByPatientId(selected.id);
            for (ClinicalHistory history : histories) {
                tableModel.addRow(new Object[] {
                        history.getId(),
                        selected.display,
                        history.getFechaConsulta() != null ? history.getFechaConsulta().format(DATE_FORMATTER) : "",
                        history.getMotivoConsulta(),
                        history.getDiagnostico(),
                        history.getMedico(),
                        ""
                });
            }
        }
    }

    public void showNewConsultationDialog() {
        ClinicalHistoryDialog dialog = new ClinicalHistoryDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setOnSaveCallback(saved -> {
            loadHistories();
            refreshPatientCombo();
        });
        dialog.setVisible(true);
    }

    public void selectPatient(Integer patientId) {
        for (int i = 0; i < patientCombo.getItemCount(); i++) {
            PatientItem item = patientCombo.getItemAt(i);
            if (item.id != null && item.id.equals(patientId)) {
                patientCombo.setSelectedIndex(i);
                filterByPatient();
                break;
            }
        }
    }

    private void editHistory(int row) {
        int id = (int) tableModel.getValueAt(row, 0);
        historyRepository.findById(id).ifPresent(history -> {
            ClinicalHistoryDialog dialog = new ClinicalHistoryDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this), history);
            dialog.setOnSaveCallback(saved -> loadHistories());
            dialog.setVisible(true);
        });
    }

    private void viewHistoryDetails(int row) {
        int id = (int) tableModel.getValueAt(row, 0);
        historyRepository.findById(id).ifPresent(history -> {
            String patientName = patientRepository.findById(history.getPatientId())
                    .map(Patient::getNombreCompleto)
                    .orElse("Desconocido");

            String details = String.format("""
                    Paciente: %s
                    Fecha: %s
                    Motivo: %s

                    Antecedentes:
                    %s

                    Examen Físico:
                    %s

                    Diagnóstico:
                    %s

                    Conducta:
                    %s

                    Observaciones:
                    %s

                    Médico: %s
                    """,
                    patientName,
                    history.getFechaConsulta() != null ? history.getFechaConsulta().format(DATE_FORMATTER) : "N/A",
                    history.getMotivoConsulta(),
                    history.getAntecedentes() != null ? history.getAntecedentes() : "N/A",
                    history.getExamenFisico() != null ? history.getExamenFisico() : "N/A",
                    history.getDiagnostico() != null ? history.getDiagnostico() : "N/A",
                    history.getConducta() != null ? history.getConducta() : "N/A",
                    history.getObservaciones() != null ? history.getObservaciones() : "N/A",
                    history.getMedico() != null ? history.getMedico() : "N/A");

            JTextArea textArea = new JTextArea(details);
            textArea.setEditable(false);
            textArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 450));

            JOptionPane.showMessageDialog(this, scrollPane, "Detalles de Consulta", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void refreshPatientCombo() {
        PatientItem selected = (PatientItem) patientCombo.getSelectedItem();
        patientCombo.removeAllItems();
        patientCombo.addItem(new PatientItem(null, "Todos los pacientes"));
        for (Patient p : patientRepository.findAll()) {
            patientCombo.addItem(new PatientItem(p.getId(), p.getNombreCompleto() + " - " + p.getCedula()));
        }
        if (selected != null && selected.id != null) {
            selectPatient(selected.id);
        }
    }

    @Override
    public void refresh() {
        loadHistories();
        refreshPatientCombo();
    }

    private record PatientItem(Integer id, String display) {
        @Override
        public String toString() {
            return display;
        }
    }
}
