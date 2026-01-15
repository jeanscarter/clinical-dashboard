package com.cms.ui.views;

import com.cms.di.AppFactory;
import com.cms.domain.User;
import com.cms.infra.SecurityContext;
import com.cms.service.AuthenticationService;
import com.cms.ui.MainFrame;
import com.cms.ui.dialogs.BackupRestoreDialog;
import com.cms.ui.dialogs.ChangePasswordDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingsView extends JPanel implements MainFrame.RefreshableView {

    private final MainFrame mainFrame;
    private final AuthenticationService authService;

    public SettingsView(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.authService = AppFactory.getInstance().getAuthenticationService();
        
        setLayout(new BorderLayout());
        setBackground(new Color(241, 245, 249));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        add(createHeader(), BorderLayout.NORTH);
        add(createCardsPanel(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JLabel title = new JLabel("Configuración del Sistema");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        header.add(title, BorderLayout.NORTH);
        
        JLabel subtitle = new JLabel("Gestione su cuenta, usuarios y herramientas de seguridad.");
        subtitle.setForeground(Color.GRAY);
        header.add(subtitle, BorderLayout.SOUTH);
        
        return header;
    }

    private JPanel createCardsPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 20, 20));
        panel.setOpaque(false);

        // Tarjeta 1: Mi Seguridad
        panel.add(createCard("Mi Seguridad", 
            "Cambie su contraseña personal actual.", 
            "🔑 Cambiar mi Clave", 
            e -> openChangePassword()));

        // Tarjeta 2: Gestión de Usuarios (Crear/Modificar Admin y otros)
        if (SecurityContext.canManageUsers()) {
            panel.add(createCard("Gestión de Usuarios", 
                "Cree nuevos administradores o modifique el usuario 'admin' existente.", 
                "👤 Abrir Usuarios", 
                e -> mainFrame.navigateTo("usuarios")));
        }

        // Tarjeta 3: Respaldo y Restauración
        if (SecurityContext.canManageBackups()) {
            panel.add(createCard("Base de Datos", 
                "Realice copias de seguridad o restaure el sistema.", 
                "💾 Backup / Restore", 
                e -> new BackupRestoreDialog(mainFrame).setVisible(true)));
        }

        return panel;
    }

    private JPanel createCard(String title, String desc, String btnText, java.awt.event.ActionListener action) {
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(20, 20, 20, 20)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        JTextArea lblDesc = new JTextArea(desc);
        lblDesc.setWrapStyleWord(true);
        lblDesc.setLineWrap(true);
        lblDesc.setEditable(false);
        lblDesc.setBackground(Color.WHITE);

        JButton btn = new JButton(btnText);
        btn.setPreferredSize(new Dimension(150, 40));
        btn.setBackground(new Color(59, 130, 246));
        btn.setForeground(Color.WHITE);
        btn.addActionListener(action);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblDesc, BorderLayout.CENTER);
        card.add(btn, BorderLayout.SOUTH);
        
        return card;
    }

    private void openChangePassword() {
        User user = SecurityContext.getCurrentUser();
        if (user != null) {
            new ChangePasswordDialog(mainFrame, authService, user.getUsername(), () -> {
                JOptionPane.showMessageDialog(this, "Contraseña cambiada.");
            }).setVisible(true);
        }
    }

    @Override
    public void refresh() {}
}