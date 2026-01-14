package com.cms;

import com.cms.di.AppFactory;
import com.cms.infra.AppLogger;
import com.cms.infra.DatabaseMigration;
import com.cms.service.AuthenticationService;
import com.cms.ui.KeyboardShortcutManager;
import com.cms.ui.LoginFrame;
import com.cms.ui.MainFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        AppLogger.info("Starting Clinical Management System...");

        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize factory and run migrations
                AppFactory factory = AppFactory.getInstance();

                DatabaseMigration migration = new DatabaseMigration(factory.getDatabaseConnection());
                migration.runMigrations();

                // Initialize authentication service
                AuthenticationService authService = factory.getAuthenticationService();

                // Show login frame
                LoginFrame loginFrame = new LoginFrame(authService);
                loginFrame.setOnLoginSuccess(() -> {
                    MainFrame mainFrame = new MainFrame();
                    KeyboardShortcutManager.initialize(mainFrame);
                    mainFrame.setVisible(true);
                    AppLogger.info("Main application started");
                });
                loginFrame.setVisible(true);

            } catch (Exception e) {
                AppLogger.error("Application startup failed", e);
                JOptionPane.showMessageDialog(null,
                        "Error al iniciar la aplicación: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
