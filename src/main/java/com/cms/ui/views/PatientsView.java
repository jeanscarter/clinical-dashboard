package com.cms.ui.views;

import com.cms.di.AppFactory;
import com.cms.domain.Patient;
import com.cms.presenter.PatientContract;
import com.cms.repository.PatientRepository;
import com.cms.ui.MainFrame;
import com.cms.ui.components.IconFactory;
import com.cms.ui.dialogs.AppointmentDialog;
import com.cms.ui.dialogs.CustomAlertDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class PatientsView extends JPanel implements MainFrame.RefreshableView, PatientContract.View {

    private static final Color PRIMARY = new Color(59, 130, 246);
    private final PatientContract.Presenter presenter;
    private final MainFrame mainFrame;
    private JTable patientsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public PatientsView(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        // Se inicializa el presenter DESDE la factory
        this.presenter = AppFactory.getInstance().getPatientPresenter(this);

        setLayout(new BorderLayout());
        setBackground(new Color(241, 245, 249));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        presenter.loadPatients();
    }

    // --- Métodos Requeridos por MainFrame ---

    public void showNewPatientDialog() {
        showPatientDialog(null);
    }

    public void focusSearchField() {
        SwingUtilities.invokeLater(() -> {
            searchField.requestFocusInWindow();
            searchField.selectAll();
        });
    }

    // --- Implementación de PatientContract.View ---

    @Override
    public void showPatients(List<Patient> patients) {
        tableModel.setRowCount(0);
        for (Patient patient : patients) {
            tableModel.addRow(new Object[] {
                    patient.getId(),
                    patient.getCedula(),
                    patient.getNombre(),
                    patient.getApellido(),
                    patient.getTelefono(),
                    patient.getEmail(),
                    ""
            });
        }
    }

    @Override
    public void showLoading(boolean loading) {
        setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    @Override
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void showSuccess(String message) {
        // Suppressed: We now show CustomAlertDialog directly in save/update actions
        // The presenter still calls this for consistency, but the UI is handled
        // separately
    }

    @Override
    public void showPatientDetails(Patient patient) {
        showPatientDialog(patient);
    }

    @Override
    public void clearForm() {
    }

    @Override
    public void navigateToHistory(Patient patient) {
        // Lógica de navegación a través del MainFrame
        mainFrame.navigateToWithAction("history", "select_patient", patient.getId());
    }

    @Override
    public void refresh() {
        presenter.loadPatients();
    }

    // --- Componentes UI ---

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 25, 0));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Gestión de Pacientes");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(15, 23, 42));

        JLabel subtitle = new JLabel("Administrar registros de pacientes");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 116, 139));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        JButton addButton = new JButton("Nuevo Paciente",
                IconFactory.createPlusIcon(14, Color.WHITE));
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addButton.setIconTextGap(6);
        addButton.setBackground(PRIMARY);
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorderPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.setPreferredSize(new Dimension(160, 40));
        addButton.addActionListener(e -> showNewPatientDialog());

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

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setOpaque(false);
        searchPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Panel con icono de búsqueda y campo de texto
        JPanel searchContainer = new JPanel(new BorderLayout());
        searchContainer.setBackground(Color.WHITE);
        searchContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(0, 10, 0, 0)));
        searchContainer.setPreferredSize(new Dimension(350, 40));

        JLabel searchIcon = new JLabel(IconFactory.createSearchIcon(16, new Color(148, 163, 184)));
        searchIcon.setBorder(new EmptyBorder(0, 0, 0, 8));

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createEmptyBorder());
        searchField.putClientProperty("JTextField.placeholderText", "Buscar por nombre, apellido o cédula...");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                presenter.searchPatients(searchField.getText());
            }
        });

        searchContainer.add(searchIcon, BorderLayout.WEST);
        searchContainer.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchContainer, BorderLayout.WEST);

        String[] columns = { "ID", "Cédula", "Nombre", "Apellido", "Teléfono", "Email", "Acciones" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        patientsTable = new JTable(tableModel);
        patientsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        patientsTable.setRowHeight(45);
        patientsTable.getTableHeader().setBackground(new Color(248, 250, 252));

        patientsTable.getColumnModel().getColumn(6).setMinWidth(180);
        patientsTable.getColumnModel().getColumn(6).setPreferredWidth(180);
        patientsTable.getColumnModel().getColumn(6)
                .setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
                    JPanel panel = new JPanel();
                    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
                    panel.setOpaque(true);
                    panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                    panel.add(Box.createHorizontalGlue());
                    JButton editBtn = createTextIconButton("Editar",
                            IconFactory.createEditIcon(12, new Color(59, 130, 246)),
                            new Color(219, 234, 254), new Color(59, 130, 246));
                    JButton deleteBtn = createTextIconButton("Eliminar",
                            IconFactory.createDeleteIcon(12, new Color(220, 38, 38)),
                            new Color(254, 226, 226), new Color(220, 38, 38));
                    panel.add(editBtn);
                    panel.add(Box.createHorizontalStrut(4));
                    panel.add(deleteBtn);
                    panel.add(Box.createHorizontalGlue());
                    return panel;
                });

        patientsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int column = patientsTable.columnAtPoint(e.getPoint());
                int row = patientsTable.rowAtPoint(e.getPoint());
                if (column == 6 && row >= 0) {
                    int id = (int) tableModel.getValueAt(row, 0);
                    Rectangle cellRect = patientsTable.getCellRect(row, column, true);
                    int xInCell = e.getPoint().x - cellRect.x;
                    if (xInCell < cellRect.width / 2) {
                        presenter.selectPatient(id);
                    } else {
                        confirmDelete(id, tableModel.getValueAt(row, 2) + " " + tableModel.getValueAt(row, 3));
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(patientsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        content.add(searchPanel, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);

        return content;
    }

    private void confirmDelete(int id, String name) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar al paciente " + name + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            presenter.deletePatient(id);
        }
    }

    private void showPatientDialog(Patient patient) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                patient == null ? "Nuevo Paciente" : "Editar Paciente", true);
        dialog.setSize(500, 550);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        JTextField cedulaField = new JTextField(patient != null ? patient.getCedula() : "");
        JTextField nombreField = new JTextField(patient != null ? patient.getNombre() : "");
        JTextField apellidoField = new JTextField(patient != null ? patient.getApellido() : "");
        JTextField telefonoField = new JTextField(patient != null ? patient.getTelefono() : "");
        JTextField emailField = new JTextField(patient != null ? patient.getEmail() : "");
        JTextField direccionField = new JTextField(patient != null ? patient.getDireccion() : "");

        int row = 0;
        addFormField(form, gbc, row++, "Cédula: *", cedulaField);
        addFormField(form, gbc, row++, "Nombre: *", nombreField);
        addFormField(form, gbc, row++, "Apellido:", apellidoField);
        addFormField(form, gbc, row++, "Teléfono:", telefonoField);
        addFormField(form, gbc, row++, "Email:", emailField);
        addFormField(form, gbc, row++, "Dirección:", direccionField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(new Color(248, 250, 252));
        buttonPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JButton cancelBtn = new JButton("Cancelar");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton saveBtn = new JButton("Guardar");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setBackground(PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setPreferredSize(new Dimension(100, 35));
        saveBtn.setBorderPainted(false);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            Patient p = patient != null ? patient : new Patient();
            p.setCedula(cedulaField.getText().trim());
            p.setNombre(nombreField.getText().trim());
            p.setApellido(apellidoField.getText().trim());
            p.setTelefono(telefonoField.getText().trim());
            p.setEmail(emailField.getText().trim());
            p.setDireccion(direccionField.getText().trim());

            boolean isNewPatient = (patient == null);
            String patientFullName = p.getNombreCompleto();

            if (isNewPatient) {
                presenter.savePatient(p);
            } else {
                presenter.updatePatient(p);
            }
            dialog.dispose();

            // Prompt for appointment after new patient registration
            if (isNewPatient) {
                // Step 1: Show success message
                CustomAlertDialog.showSuccess(
                        PatientsView.this,
                        "Registro Exitoso",
                        "Paciente Guardado Exitosamente");

                // Step 2: Ask if user wants to schedule an appointment with personalized
                // message
                boolean wantsAppointment = CustomAlertDialog.showConfirm(
                        PatientsView.this,
                        "Agendar Cita",
                        "¿Desea agendar una cita para " + patientFullName + "?");

                if (wantsAppointment) {
                    // Find the saved patient to get the ID
                    PatientRepository patientRepo = AppFactory.getInstance().getPatientRepository();
                    patientRepo.findByCedula(p.getCedula()).ifPresent(savedPatient -> {
                        Frame owner = (Frame) SwingUtilities.getWindowAncestor(PatientsView.this);
                        AppointmentDialog appointmentDialog = new AppointmentDialog(owner, savedPatient.getId());
                        appointmentDialog.setOnSaveCallback(saved -> {
                            // Navigate to agenda after saving appointment
                            mainFrame.navigateTo("agenda");
                        });
                        appointmentDialog.setVisible(true);
                    });
                }
            }
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        dialog.setLayout(new BorderLayout());
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addFormField(JPanel form, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        form.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        field.setPreferredSize(new Dimension(250, 35));
        form.add(field, gbc);
    }

    private JButton createIconButton(Icon icon, Color bgColor, String tooltip) {
        JButton btn = new JButton(icon);
        btn.setToolTipText(tooltip);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setMargin(new Insets(4, 8, 4, 8));
        btn.setPreferredSize(new Dimension(36, 28));
        btn.setMaximumSize(new Dimension(36, 28));
        return btn;
    }

    private JButton createTextIconButton(String text, Icon icon, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text, icon);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setIconTextGap(4);
        btn.setMargin(new Insets(2, 6, 2, 6));
        btn.setPreferredSize(new Dimension(80, 28));
        btn.setMaximumSize(new Dimension(80, 28));
        return btn;
    }
}