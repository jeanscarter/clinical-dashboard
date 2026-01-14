package com.cms.ui.components;

import com.cms.core.validation.ClinicalHistoryValidator;
import com.cms.core.validation.ValidationResult;
import com.cms.domain.ClinicalHistory;
import com.cms.domain.Patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

public class ClinicalHistoryFormPanel extends JPanel {

    private static final Color LABEL_COLOR = new Color(71, 85, 105);
    private static final Color ERROR_COLOR = new Color(239, 68, 68);
    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private JComboBox<PatientItem> patientCombo;
    private JTextField fechaField;
    private JTextField motivoField;
    private JTextArea antecedentesArea;
    private JTextArea examenArea;
    private JTextArea diagnosticoArea;
    private JTextArea conductaArea;
    private JTextArea observacionesArea;
    private JTextField medicoField;
    private JLabel errorLabel;

    private ClinicalHistory currentHistory;
    private final ClinicalHistoryValidator validator;

    private Consumer<ClinicalHistory> onSave;
    private Runnable onCancel;

    public ClinicalHistoryFormPanel() {
        this.validator = new ClinicalHistoryValidator();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(new JScrollPane(createForm()), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.NORTHWEST;

        patientCombo = new JComboBox<>();
        patientCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        patientCombo.setPreferredSize(new Dimension(350, 38));

        fechaField = createTextField();
        fechaField.setText(LocalDateTime.now().format(DATE_FORMAT));

        motivoField = createTextField();
        antecedentesArea = createTextArea(3);
        examenArea = createTextArea(3);
        diagnosticoArea = createTextArea(3);
        conductaArea = createTextArea(3);
        observacionesArea = createTextArea(2);
        medicoField = createTextField();

        int row = 0;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        form.add(createLabel("Paciente *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        form.add(patientCombo, gbc);
        row++;

        addFormRow(form, gbc, row++, "Fecha *", fechaField);
        addFormRow(form, gbc, row++, "Motivo Consulta *", motivoField);
        addTextAreaRow(form, gbc, row++, "Antecedentes", antecedentesArea);
        addTextAreaRow(form, gbc, row++, "Examen Físico", examenArea);
        addTextAreaRow(form, gbc, row++, "Diagnóstico", diagnosticoArea);
        addTextAreaRow(form, gbc, row++, "Conducta", conductaArea);
        addTextAreaRow(form, gbc, row++, "Observaciones", observacionesArea);
        addFormRow(form, gbc, row++, "Médico", medicoField);

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(ERROR_COLOR);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        form.add(errorLabel, gbc);

        return form;
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        gbc.weighty = 0;
        form.add(createLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        form.add(field, gbc);
    }

    private void addTextAreaRow(JPanel form, GridBagConstraints gbc, int row, String label, JTextArea area) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        gbc.weighty = 0;
        form.add(createLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setPreferredSize(new Dimension(350, 70));
        form.add(scrollPane, gbc);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(LABEL_COLOR);
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(350, 38));
        return field;
    }

    private JTextArea createTextArea(int rows) {
        JTextArea area = new JTextArea(rows, 30);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton cancelBtn = new JButton("Cancelar");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelBtn.setPreferredSize(new Dimension(100, 40));
        cancelBtn.addActionListener(e -> {
            if (onCancel != null) {
                onCancel.run();
            }
        });

        JButton saveBtn = new JButton("Guardar");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setBackground(PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setPreferredSize(new Dimension(100, 40));
        saveBtn.addActionListener(e -> saveHistory());

        panel.add(cancelBtn);
        panel.add(saveBtn);

        return panel;
    }

    public void setPatients(List<Patient> patients) {
        patientCombo.removeAllItems();
        for (Patient p : patients) {
            patientCombo.addItem(new PatientItem(p.getId(), p.getNombreCompleto() + " - " + p.getCedula()));
        }
    }

    public void selectPatient(Integer patientId) {
        for (int i = 0; i < patientCombo.getItemCount(); i++) {
            PatientItem item = patientCombo.getItemAt(i);
            if (item.id().equals(patientId)) {
                patientCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    public void setHistory(ClinicalHistory history) {
        this.currentHistory = history;
        if (history != null) {
            if (history.getPatientId() != null) {
                selectPatient(history.getPatientId());
            }
            fechaField
                    .setText(history.getFechaConsulta() != null ? history.getFechaConsulta().format(DATE_FORMAT) : "");
            motivoField.setText(history.getMotivoConsulta() != null ? history.getMotivoConsulta() : "");
            antecedentesArea.setText(history.getAntecedentes() != null ? history.getAntecedentes() : "");
            examenArea.setText(history.getExamenFisico() != null ? history.getExamenFisico() : "");
            diagnosticoArea.setText(history.getDiagnostico() != null ? history.getDiagnostico() : "");
            conductaArea.setText(history.getConducta() != null ? history.getConducta() : "");
            observacionesArea.setText(history.getObservaciones() != null ? history.getObservaciones() : "");
            medicoField.setText(history.getMedico() != null ? history.getMedico() : "");
        } else {
            clearForm();
        }
        clearError();
    }

    public ClinicalHistory getHistory() {
        ClinicalHistory history = currentHistory != null ? currentHistory : new ClinicalHistory();

        PatientItem selectedPatient = (PatientItem) patientCombo.getSelectedItem();
        if (selectedPatient != null) {
            history.setPatientId(selectedPatient.id());
        }

        String fechaStr = fechaField.getText().trim();
        if (!fechaStr.isEmpty()) {
            try {
                history.setFechaConsulta(LocalDateTime.parse(fechaStr, DATE_FORMAT));
            } catch (Exception e) {
                history.setFechaConsulta(LocalDateTime.now());
            }
        }

        history.setMotivoConsulta(motivoField.getText().trim());
        history.setAntecedentes(antecedentesArea.getText().trim());
        history.setExamenFisico(examenArea.getText().trim());
        history.setDiagnostico(diagnosticoArea.getText().trim());
        history.setConducta(conductaArea.getText().trim());
        history.setObservaciones(observacionesArea.getText().trim());
        history.setMedico(medicoField.getText().trim());

        return history;
    }

    public void clearForm() {
        currentHistory = null;
        if (patientCombo.getItemCount() > 0) {
            patientCombo.setSelectedIndex(0);
        }
        fechaField.setText(LocalDateTime.now().format(DATE_FORMAT));
        motivoField.setText("");
        antecedentesArea.setText("");
        examenArea.setText("");
        diagnosticoArea.setText("");
        conductaArea.setText("");
        observacionesArea.setText("");
        medicoField.setText("");
        clearError();
    }

    private void saveHistory() {
        ClinicalHistory history = getHistory();
        ValidationResult validation = validator.validate(history);

        if (!validation.isValid()) {
            showError(validation.getErrorsAsString());
            return;
        }

        if (onSave != null) {
            onSave.accept(history);
        }
    }

    public void showError(String message) {
        errorLabel.setText("<html>" + message.replace("\n", "<br>") + "</html>");
    }

    public void clearError() {
        errorLabel.setText(" ");
    }

    public void setOnSave(Consumer<ClinicalHistory> handler) {
        this.onSave = handler;
    }

    public void setOnCancel(Runnable handler) {
        this.onCancel = handler;
    }

    private record PatientItem(Integer id, String display) {
        @Override
        public String toString() {
            return display;
        }
    }
}
