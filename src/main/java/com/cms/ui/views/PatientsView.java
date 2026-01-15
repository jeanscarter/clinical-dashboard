package com.cms.ui.views;

import com.cms.di.AppFactory;
import com.cms.domain.Patient;
import com.cms.repository.PatientRepository;
import com.cms.ui.MainFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class PatientsView extends JPanel implements MainFrame.RefreshableView {

    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color DANGER = new Color(239, 68, 68);

    private final PatientRepository patientRepository;
    private final MainFrame mainFrame;
    private JTable patientsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public PatientsView(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.patientRepository = AppFactory.getInstance().getPatientRepository();

        setLayout(new BorderLayout());
        setBackground(new Color(241, 245, 249));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        loadPatients();
    }

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

        JButton addButton = new JButton("➕ Nuevo Paciente");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addButton.setBackground(PRIMARY);
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorderPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.setPreferredSize(new Dimension(160, 40));
        addButton.addActionListener(e -> showPatientDialog(null));

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

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.putClientProperty("JTextField.placeholderText", "🔍 Buscar por nombre, apellido o cédula...");
        searchField.setPreferredSize(new Dimension(300, 40));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterTable(searchField.getText());
            }
        });

        searchPanel.add(searchField, BorderLayout.WEST);

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
        patientsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        patientsTable.getTableHeader().setBackground(new Color(248, 250, 252));
        patientsTable.setSelectionBackground(new Color(219, 234, 254));
        patientsTable.setGridColor(new Color(226, 232, 240));

        patientsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        patientsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        patientsTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        patientsTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        patientsTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        patientsTable.getColumnModel().getColumn(5).setPreferredWidth(180);
        patientsTable.getColumnModel().getColumn(6).setPreferredWidth(150);

        patientsTable.getColumnModel().getColumn(6)
                .setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
                    JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
                    panel.setOpaque(false);

                    JButton editBtn = new JButton("✏️");
                    editBtn.setToolTipText("Editar");
                    editBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
                    editBtn.setPreferredSize(new Dimension(35, 30));
                    editBtn.setBackground(new Color(219, 234, 254));
                    editBtn.setBorderPainted(false);

                    JButton deleteBtn = new JButton("🗑️");
                    deleteBtn.setToolTipText("Eliminar");
                    deleteBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
                    deleteBtn.setPreferredSize(new Dimension(35, 30));
                    deleteBtn.setBackground(new Color(254, 226, 226));
                    deleteBtn.setBorderPainted(false);

                    panel.add(editBtn);
                    panel.add(deleteBtn);

                    return panel;
                });

        patientsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int column = patientsTable.columnAtPoint(e.getPoint());
                int row = patientsTable.rowAtPoint(e.getPoint());
                if (column == 6 && row >= 0) {
                    Rectangle cellRect = patientsTable.getCellRect(row, column, true);
                    int xInCell = e.getPoint().x - cellRect.x;
                    int cellWidth = cellRect.width;

                    // Button layout: FlowLayout.CENTER with 5px horizontal gap
                    // Each button is 35px wide
                    // Total buttons width = 35 + 5 + 35 = 75px
                    int buttonWidth = 35;
                    int buttonGap = 5;
                    int buttonsWidth = buttonWidth + buttonGap + buttonWidth;
                    int startX = (cellWidth - buttonsWidth) / 2;
                    int editEnd = startX + buttonWidth;
                    int deleteStart = editEnd + buttonGap;
                    int deleteEnd = deleteStart + buttonWidth;

                    if (xInCell >= startX && xInCell < editEnd) {
                        editPatient(row);
                    } else if (xInCell >= deleteStart && xInCell < deleteEnd) {
                        deletePatient(row);
                    }
                    // Clicks outside button areas do nothing
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(patientsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        content.add(searchPanel, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);

        return content;
    }

    private void loadPatients() {
        tableModel.setRowCount(0);
        List<Patient> patients = patientRepository.findAll();
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

    private void filterTable(String query) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        patientsTable.setRowSorter(sorter);
        if (query.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
        }
    }

    public void showNewPatientDialog() {
        showPatientDialog(null);
    }

    public void focusSearchField() {
        SwingUtilities.invokeLater(() -> {
            searchField.requestFocusInWindow();
            searchField.selectAll();
        });
    }

    private void showPatientDialog(Patient patient) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                patient == null ? "Nuevo Paciente" : "Editar Paciente", true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
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
        addFormField(form, gbc, row++, "Cédula *", cedulaField);
        addFormField(form, gbc, row++, "Nombre *", nombreField);
        addFormField(form, gbc, row++, "Apellido", apellidoField);
        addFormField(form, gbc, row++, "Teléfono", telefonoField);
        addFormField(form, gbc, row++, "Email", emailField);
        addFormField(form, gbc, row++, "Dirección", direccionField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("Cancelar");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = new JButton("Guardar");
        saveBtn.setBackground(PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            if (cedulaField.getText().trim().isEmpty() || nombreField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Cédula y Nombre son obligatorios", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Patient p = patient != null ? patient : new Patient();
            p.setCedula(cedulaField.getText().trim());
            p.setNombre(nombreField.getText().trim());
            p.setApellido(apellidoField.getText().trim());
            p.setTelefono(telefonoField.getText().trim());
            p.setEmail(emailField.getText().trim());
            p.setDireccion(direccionField.getText().trim());

            patientRepository.save(p);
            loadPatients();
            dialog.dispose();
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        form.add(buttonPanel, gbc);

        dialog.add(form);
        dialog.setVisible(true);
    }

    private void addFormField(JPanel form, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        form.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        field.setPreferredSize(new Dimension(250, 35));
        form.add(field, gbc);
    }

    private void editPatient(int row) {
        int id = (int) tableModel.getValueAt(row, 0);
        patientRepository.findById(id).ifPresent(this::showPatientDialog);
    }

    private void deletePatient(int row) {
        int id = (int) tableModel.getValueAt(row, 0);
        String nombre = tableModel.getValueAt(row, 2) + " " + tableModel.getValueAt(row, 3);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar al paciente " + nombre + "?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            patientRepository.delete(id);
            loadPatients();
        }
    }

    @Override
    public void refresh() {
        loadPatients();
    }
}
