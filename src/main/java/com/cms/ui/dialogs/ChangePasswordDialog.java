package com.cms.ui.dialogs;

import com.cms.service.AuthenticationService;
import com.cms.service.exception.BusinessException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal dialog for forced password change.
 * Cannot be closed until password is successfully updated.
 */
public class ChangePasswordDialog extends JDialog {

    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final Color ERROR_COLOR = new Color(239, 68, 68);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);

    private final AuthenticationService authService;
    private final String username;
    private final Runnable onSuccess;

    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel messageLabel;
    private JButton saveButton;

    private boolean passwordChanged = false;

    public ChangePasswordDialog(Frame parent, AuthenticationService authService,
            String username, Runnable onSuccess) {
        super(parent, "Cambio de Contraseña Obligatorio", true);
        this.authService = authService;
        this.username = username;
        this.onSuccess = onSuccess;

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(450, 380);
        setLocationRelativeTo(parent);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        mainPanel.add(createHeader(), BorderLayout.NORTH);
        mainPanel.add(createForm(), BorderLayout.CENTER);
        mainPanel.add(createFooter(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(new Color(254, 243, 199));
        header.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel warningIcon = new JLabel("⚠️");
        warningIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        warningIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Cambio de Contraseña Requerido");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(146, 64, 14));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "<html><center>Por seguridad, debe cambiar la contraseña por defecto antes de continuar.</center></html>");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(180, 83, 9));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(warningIcon);
        header.add(Box.createVerticalStrut(10));
        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);

        return header;
    }

    private JPanel createForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(25, 30, 15, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        currentPasswordField = new JPasswordField();
        newPasswordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();

        configureField(currentPasswordField);
        configureField(newPasswordField);
        configureField(confirmPasswordField);

        int row = 0;
        addFormRow(form, gbc, row++, "Contraseña actual:", currentPasswordField);
        addFormRow(form, gbc, row++, "Nueva contraseña:", newPasswordField);
        addFormRow(form, gbc, row++, "Confirmar contraseña:", confirmPasswordField);

        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        form.add(messageLabel, gbc);

        return form;
    }

    private void configureField(JPasswordField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(200, 35));
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        form.add(field, gbc);
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(10, 20, 20, 20));

        saveButton = new JButton("Cambiar Contraseña");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setBackground(PRIMARY);
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.setPreferredSize(new Dimension(180, 40));
        saveButton.addActionListener(e -> changePassword());

        footer.add(saveButton);
        return footer;
    }

    private void changePassword() {
        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("Todos los campos son obligatorios");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Las contraseñas nuevas no coinciden");
            return;
        }

        if (newPassword.length() < 6) {
            showError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        if (newPassword.equals("admin123")) {
            showError("No puede usar la contraseña por defecto");
            return;
        }

        saveButton.setEnabled(false);
        saveButton.setText("Guardando...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                authService.changePassword(username, currentPassword, newPassword);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    passwordChanged = true;
                    showSuccess("Contraseña actualizada correctamente");

                    Timer timer = new Timer(1500, evt -> {
                        if (onSuccess != null) {
                            onSuccess.run();
                        }
                        dispose();
                    });
                    timer.setRepeats(false);
                    timer.start();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause();
                    String message = cause instanceof BusinessException
                            ? cause.getMessage()
                            : "Error al cambiar la contraseña";
                    showError(message);
                    saveButton.setEnabled(true);
                    saveButton.setText("Cambiar Contraseña");
                }
            }
        };
        worker.execute();
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setForeground(ERROR_COLOR);
    }

    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setForeground(SUCCESS_COLOR);
    }

    public boolean isPasswordChanged() {
        return passwordChanged;
    }
}
