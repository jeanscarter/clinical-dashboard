package com.cms.ui.components;

import com.cms.core.validation.PatientValidator;
import com.cms.core.validation.ValidationResult;
import com.cms.domain.Patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class PatientFormPanel extends JPanel {

    private static final Color LABEL_COLOR = new Color(71, 85, 105);
    private static final Color ERROR_COLOR = new Color(239, 68, 68);
    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JTextField cedulaField;
    private JTextField nombreField;
    private JTextField apellidoField;
    private JTextField fechaNacimientoField;
    private JComboBox<String> sexoCombo;
    private JTextField direccionField;
    private JTextField telefonoField;
    private JTextField emailField;
    private JLabel errorLabel;

    private Patient currentPatient;
    private final PatientValidator validator;

    private Consumer<Patient> onSave;
    private Runnable onCancel;

    public PatientFormPanel() {
        this.validator = new PatientValidator();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createForm(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        cedulaField = createTextField();
        nombreField = createTextField();
        apellidoField = createTextField();
        fechaNacimientoField = createTextField();
        fechaNacimientoField.setToolTipText("Formato: dd/MM/yyyy");

        sexoCombo = new JComboBox<>(new String[] { "", "Masculino", "Femenino", "Otro" });
        sexoCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sexoCombo.setPreferredSize(new Dimension(250, 38));

        direccionField = createTextField();
        telefonoField = createTextField();
        emailField = createTextField();

        int row = 0;
        addFormRow(form, gbc, row++, "Cédula *", cedulaField, "Ej: V-12345678");
        addFormRow(form, gbc, row++, "Nombre *", nombreField, null);
        addFormRow(form, gbc, row++, "Apellido", apellidoField, null);
        addFormRow(form, gbc, row++, "Fecha Nacimiento", fechaNacimientoField, "dd/MM/yyyy");

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        form.add(createLabel("Sexo"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        form.add(sexoCombo, gbc);
        row++;

        addFormRow(form, gbc, row++, "Dirección", direccionField, null);
        addFormRow(form, gbc, row++, "Teléfono", telefonoField, "Ej: 0412-1234567");
        addFormRow(form, gbc, row++, "Email", emailField, "Ej: correo@ejemplo.com");

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(ERROR_COLOR);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        form.add(errorLabel, gbc);

        return form;
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, JTextField field,
            String placeholder) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        form.add(createLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        if (placeholder != null) {
            field.putClientProperty("JTextField.placeholderText", placeholder);
        }
        form.add(field, gbc);
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
        field.setPreferredSize(new Dimension(250, 38));
        return field;
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
        saveBtn.addActionListener(e -> savePatient());

        panel.add(cancelBtn);
        panel.add(saveBtn);

        return panel;
    }

    public void setPatient(Patient patient) {
        this.currentPatient = patient;
        if (patient != null) {
            cedulaField.setText(patient.getCedula() != null ? patient.getCedula() : "");
            nombreField.setText(patient.getNombre() != null ? patient.getNombre() : "");
            apellidoField.setText(patient.getApellido() != null ? patient.getApellido() : "");

            if (patient.getFechaNacimiento() != null) {
                fechaNacimientoField.setText(patient.getFechaNacimiento().format(DATE_FORMAT));
            } else {
                fechaNacimientoField.setText("");
            }

            sexoCombo.setSelectedItem(patient.getSexo() != null ? patient.getSexo() : "");
            direccionField.setText(patient.getDireccion() != null ? patient.getDireccion() : "");
            telefonoField.setText(patient.getTelefono() != null ? patient.getTelefono() : "");
            emailField.setText(patient.getEmail() != null ? patient.getEmail() : "");
        } else {
            clearForm();
        }
        clearError();
    }

    public Patient getPatient() {
        Patient patient = currentPatient != null ? currentPatient : new Patient();
        patient.setCedula(cedulaField.getText().trim());
        patient.setNombre(nombreField.getText().trim());
        patient.setApellido(apellidoField.getText().trim());

        String fechaStr = fechaNacimientoField.getText().trim();
        if (!fechaStr.isEmpty()) {
            try {
                patient.setFechaNacimiento(LocalDate.parse(fechaStr, DATE_FORMAT));
            } catch (Exception e) {
                // Invalid date format, will be caught by validation
            }
        }

        patient.setSexo((String) sexoCombo.getSelectedItem());
        patient.setDireccion(direccionField.getText().trim());
        patient.setTelefono(telefonoField.getText().trim());
        patient.setEmail(emailField.getText().trim());

        return patient;
    }

    public void clearForm() {
        currentPatient = null;
        cedulaField.setText("");
        nombreField.setText("");
        apellidoField.setText("");
        fechaNacimientoField.setText("");
        sexoCombo.setSelectedIndex(0);
        direccionField.setText("");
        telefonoField.setText("");
        emailField.setText("");
        clearError();
    }

    private void savePatient() {
        Patient patient = getPatient();
        ValidationResult validation = validator.validate(patient);

        if (!validation.isValid()) {
            showError(validation.getErrorsAsString());
            return;
        }

        if (onSave != null) {
            onSave.accept(patient);
        }
    }

    public void showError(String message) {
        errorLabel.setText("<html>" + message.replace("\n", "<br>") + "</html>");
    }

    public void clearError() {
        errorLabel.setText(" ");
    }

    public void setOnSave(Consumer<Patient> handler) {
        this.onSave = handler;
    }

    public void setOnCancel(Runnable handler) {
        this.onCancel = handler;
    }

    public boolean validateForm() {
        Patient patient = getPatient();
        ValidationResult validation = validator.validate(patient);
        if (!validation.isValid()) {
            showError(validation.getErrorsAsString());
            return false;
        }
        return true;
    }
}
