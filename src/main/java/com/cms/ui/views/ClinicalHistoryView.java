package com.cms.ui.views;

import com.cms.di.AppFactory;
import com.cms.domain.ClinicalHistory;
import com.cms.domain.Patient;
import com.cms.repository.ClinicalHistoryRepository;
import com.cms.repository.PatientRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClinicalHistoryView extends JPanel {

    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ClinicalHistoryRepository historyRepository;
    private final PatientRepository patientRepository;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JComboBox<PatientItem> patientCombo;

    public ClinicalHistoryView() {
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
        addButton.addActionListener(e -> showHistoryDialog(null));

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

                    panel.add(viewBtn);
                    return panel;
                });

        historyTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int column = historyTable.columnAtPoint(e.getPoint());
                int row = historyTable.rowAtPoint(e.getPoint());
                if (column == 6 && row >= 0) {
                    viewHistoryDetails(row);
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

    private void showHistoryDialog(ClinicalHistory history) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                history == null ? "Nueva Consulta" : "Editar Consulta", true);
        dialog.setSize(600, 550);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        JComboBox<PatientItem> patientField = new JComboBox<>();
        for (Patient p : patientRepository.findAll()) {
            patientField.addItem(new PatientItem(p.getId(), p.getNombreCompleto() + " - " + p.getCedula()));
        }

        JTextField motivoField = new JTextField(history != null ? history.getMotivoConsulta() : "");
        JTextArea antecedentesArea = new JTextArea(history != null ? history.getAntecedentes() : "", 3, 30);
        JTextArea diagnosticoArea = new JTextArea(history != null ? history.getDiagnostico() : "", 3, 30);
        JTextArea conductaArea = new JTextArea(history != null ? history.getConducta() : "", 3, 30);
        JTextField medicoField = new JTextField(history != null ? history.getMedico() : "");

        int row = 0;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        form.add(new JLabel("Paciente *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        patientField.setPreferredSize(new Dimension(350, 35));
        form.add(patientField, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        form.add(new JLabel("Motivo *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        motivoField.setPreferredSize(new Dimension(350, 35));
        form.add(motivoField, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        form.add(new JLabel("Antecedentes"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        form.add(new JScrollPane(antecedentesArea), gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        form.add(new JLabel("Diagnóstico"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        form.add(new JScrollPane(diagnosticoArea), gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        form.add(new JLabel("Conducta"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        form.add(new JScrollPane(conductaArea), gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        form.add(new JLabel("Médico"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        medicoField.setPreferredSize(new Dimension(350, 35));
        form.add(medicoField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("Cancelar");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = new JButton("Guardar");
        saveBtn.setBackground(PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            PatientItem selectedPatient = (PatientItem) patientField.getSelectedItem();
            if (selectedPatient == null || motivoField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Paciente y Motivo son obligatorios", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            ClinicalHistory h = history != null ? history : new ClinicalHistory();
            h.setPatientId(selectedPatient.id);
            h.setMotivoConsulta(motivoField.getText().trim());
            h.setAntecedentes(antecedentesArea.getText().trim());
            h.setDiagnostico(diagnosticoArea.getText().trim());
            h.setConducta(conductaArea.getText().trim());
            h.setMedico(medicoField.getText().trim());

            historyRepository.save(h);
            loadHistories();
            dialog.dispose();
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        form.add(buttonPanel, gbc);

        dialog.add(new JScrollPane(form));
        dialog.setVisible(true);
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

                    Diagnóstico:
                    %s

                    Conducta:
                    %s

                    Médico: %s
                    """,
                    patientName,
                    history.getFechaConsulta() != null ? history.getFechaConsulta().format(DATE_FORMATTER) : "N/A",
                    history.getMotivoConsulta(),
                    history.getAntecedentes() != null ? history.getAntecedentes() : "N/A",
                    history.getDiagnostico() != null ? history.getDiagnostico() : "N/A",
                    history.getConducta() != null ? history.getConducta() : "N/A",
                    history.getMedico() != null ? history.getMedico() : "N/A");

            JTextArea textArea = new JTextArea(details);
            textArea.setEditable(false);
            textArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 400));

            JOptionPane.showMessageDialog(this, scrollPane, "Detalles de Consulta", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private record PatientItem(Integer id, String display) {
        @Override
        public String toString() {
            return display;
        }
    }
}
