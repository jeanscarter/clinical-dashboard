package com.cms.ui.views;

import com.cms.di.AppFactory;
import com.cms.domain.Role;
import com.cms.domain.User;
import com.cms.service.UserService;
import com.cms.service.exception.BusinessException;
import com.cms.service.exception.ValidationException;
import com.cms.ui.MainFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UsersView extends JPanel implements MainFrame.RefreshableView {

    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UserService userService;
    private JTable usersTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public UsersView(MainFrame mainFrame) {
        this.userService = AppFactory.getInstance().getUserService();

        setLayout(new BorderLayout());
        setBackground(new Color(241, 245, 249));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        loadUsers();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 25, 0));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Gestión de Usuarios");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(15, 23, 42));

        JLabel subtitle = new JLabel("Administrar usuarios del sistema");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 116, 139));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        JButton addButton = new JButton("➕ Nuevo Usuario");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addButton.setBackground(PRIMARY);
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorderPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.setPreferredSize(new Dimension(160, 40));
        addButton.addActionListener(e -> showUserDialog(null));

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
        searchField.putClientProperty("JTextField.placeholderText", "🔍 Buscar por nombre o usuario...");
        searchField.setPreferredSize(new Dimension(300, 40));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterTable(searchField.getText());
            }
        });

        searchPanel.add(searchField, BorderLayout.WEST);

        String[] columns = { "ID", "Usuario", "Nombre Completo", "Rol", "Estado", "Último Acceso", "Acciones" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        usersTable = new JTable(tableModel);
        usersTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        usersTable.setRowHeight(45);
        usersTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        usersTable.getTableHeader().setBackground(new Color(248, 250, 252));
        usersTable.setSelectionBackground(new Color(219, 234, 254));
        usersTable.setGridColor(new Color(226, 232, 240));

        usersTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        usersTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        usersTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        usersTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        usersTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        usersTable.getColumnModel().getColumn(5).setPreferredWidth(140);
        usersTable.getColumnModel().getColumn(6).setPreferredWidth(180);

        usersTable.getColumnModel().getColumn(4).setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            JLabel label = new JLabel(value.toString(), SwingConstants.CENTER);
            label.setOpaque(true);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            if ("Activo".equals(value)) {
                label.setBackground(new Color(220, 252, 231));
                label.setForeground(new Color(22, 163, 74));
            } else {
                label.setBackground(new Color(254, 226, 226));
                label.setForeground(new Color(220, 38, 38));
            }
            return label;
        });

        usersTable.getColumnModel().getColumn(6)
                .setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
                    JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
                    panel.setOpaque(false);

                    JButton editBtn = new JButton("✏️");
                    editBtn.setToolTipText("Editar");
                    editBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
                    editBtn.setPreferredSize(new Dimension(35, 30));
                    editBtn.setBackground(new Color(219, 234, 254));
                    editBtn.setBorderPainted(false);

                    JButton resetBtn = new JButton("🔑");
                    resetBtn.setToolTipText("Restablecer Contraseña");
                    resetBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
                    resetBtn.setPreferredSize(new Dimension(35, 30));
                    resetBtn.setBackground(new Color(254, 243, 199));
                    resetBtn.setBorderPainted(false);

                    JButton toggleBtn = new JButton("⏻");
                    toggleBtn.setToolTipText("Activar/Desactivar");
                    toggleBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
                    toggleBtn.setPreferredSize(new Dimension(35, 30));
                    toggleBtn.setBackground(new Color(254, 226, 226));
                    toggleBtn.setBorderPainted(false);

                    panel.add(editBtn);
                    panel.add(resetBtn);
                    panel.add(toggleBtn);

                    return panel;
                });

        usersTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int column = usersTable.columnAtPoint(e.getPoint());
                int row = usersTable.rowAtPoint(e.getPoint());
                if (column == 6 && row >= 0) {
                    Rectangle cellRect = usersTable.getCellRect(row, column, true);
                    int xInCell = e.getPoint().x - cellRect.x;
                    int cellWidth = cellRect.width;

                    int buttonWidth = 35;
                    int buttonGap = 5;
                    int buttonsWidth = buttonWidth * 3 + buttonGap * 2;
                    int startX = (cellWidth - buttonsWidth) / 2;
                    int editEnd = startX + buttonWidth;
                    int resetStart = editEnd + buttonGap;
                    int resetEnd = resetStart + buttonWidth;
                    int toggleStart = resetEnd + buttonGap;
                    int toggleEnd = toggleStart + buttonWidth;

                    if (xInCell >= startX && xInCell < editEnd) {
                        editUser(row);
                    } else if (xInCell >= resetStart && xInCell < resetEnd) {
                        resetPassword(row);
                    } else if (xInCell >= toggleStart && xInCell < toggleEnd) {
                        toggleUserStatus(row);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(usersTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        content.add(searchPanel, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);

        return content;
    }

    private void loadUsers() {
        tableModel.setRowCount(0);
        List<User> users = userService.getAllUsers();
        for (User user : users) {
            String lastLogin = user.getLastLogin() != null
                    ? user.getLastLogin().format(DATE_FORMAT)
                    : "Nunca";
            tableModel.addRow(new Object[] {
                    user.getId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getRole().getDisplayName(),
                    user.isActive() ? "Activo" : "Inactivo",
                    lastLogin,
                    ""
            });
        }
    }

    private void filterTable(String query) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        usersTable.setRowSorter(sorter);
        if (query.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
        }
    }

    private void showUserDialog(User user) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                user == null ? "Nuevo Usuario" : "Editar Usuario", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        JTextField usernameField = new JTextField(user != null ? user.getUsername() : "");
        usernameField.setEnabled(user == null);
        JPasswordField passwordField = new JPasswordField();
        JTextField fullNameField = new JTextField(user != null ? user.getFullName() : "");
        JComboBox<Role> roleCombo = new JComboBox<>(Role.values());
        if (user != null) {
            roleCombo.setSelectedItem(user.getRole());
        }
        JCheckBox activeCheckbox = new JCheckBox("Usuario activo");
        activeCheckbox.setSelected(user == null || user.isActive());

        int row = 0;
        addFormField(form, gbc, row++, "Usuario *", usernameField);
        if (user == null) {
            addFormField(form, gbc, row++, "Contraseña *", passwordField);
        }
        addFormField(form, gbc, row++, "Nombre Completo *", fullNameField);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        form.add(new JLabel("Rol *"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        roleCombo.setPreferredSize(new Dimension(250, 35));
        form.add(roleCombo, gbc);
        row++;

        gbc.gridx = 1;
        gbc.gridy = row++;
        form.add(activeCheckbox, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("Cancelar");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = new JButton("Guardar");
        saveBtn.setBackground(PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            try {
                if (user == null) {
                    String password = new String(passwordField.getPassword());
                    userService.createUser(
                            usernameField.getText().trim(),
                            password,
                            fullNameField.getText().trim(),
                            (Role) roleCombo.getSelectedItem());
                    JOptionPane.showMessageDialog(dialog,
                            "Usuario creado exitosamente",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    userService.updateUser(
                            user.getId(),
                            fullNameField.getText().trim(),
                            (Role) roleCombo.getSelectedItem(),
                            activeCheckbox.isSelected());
                    JOptionPane.showMessageDialog(dialog,
                            "Usuario actualizado exitosamente",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                loadUsers();
                dialog.dispose();
            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(dialog,
                        String.join("\n", ex.getErrors()),
                        "Error de Validación",
                        JOptionPane.ERROR_MESSAGE);
            } catch (BusinessException ex) {
                JOptionPane.showMessageDialog(dialog,
                        ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
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

    private void addFormField(JPanel form, GridBagConstraints gbc, int row, String label, JComponent field) {
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

    private void editUser(int row) {
        int modelRow = usersTable.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        try {
            User user = userService.getUser(id);
            showUserDialog(user);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar usuario: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetPassword(int row) {
        int modelRow = usersTable.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        String username = (String) tableModel.getValueAt(modelRow, 1);

        JPasswordField newPasswordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Nueva Contraseña:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        newPasswordField.setPreferredSize(new Dimension(200, 30));
        panel.add(newPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Confirmar Contraseña:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        confirmPasswordField.setPreferredSize(new Dimension(200, 30));
        panel.add(confirmPasswordField, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Restablecer Contraseña - " + username,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this,
                        "Las contraseñas no coinciden",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                userService.resetPassword(id, newPassword);
                JOptionPane.showMessageDialog(this,
                        "Contraseña restablecida exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(this,
                        String.join("\n", ex.getErrors()),
                        "Error de Validación",
                        JOptionPane.ERROR_MESSAGE);
            } catch (BusinessException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void toggleUserStatus(int row) {
        int modelRow = usersTable.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        String username = (String) tableModel.getValueAt(modelRow, 1);
        String currentStatus = (String) tableModel.getValueAt(modelRow, 4);
        boolean isActive = "Activo".equals(currentStatus);

        String action = isActive ? "desactivar" : "activar";
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de " + action + " al usuario " + username + "?",
                "Confirmar " + action,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (isActive) {
                    userService.deactivateUser(id);
                } else {
                    User user = userService.getUser(id);
                    userService.updateUser(id, user.getFullName(), user.getRole(), true);
                }
                loadUsers();
                JOptionPane.showMessageDialog(this,
                        "Usuario " + (isActive ? "desactivado" : "activado") + " exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (BusinessException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void refresh() {
        loadUsers();
    }
}
