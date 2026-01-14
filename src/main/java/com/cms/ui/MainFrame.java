package com.cms.ui;

import com.cms.di.AppFactory;
import com.cms.ui.components.NavigationDrawer;
import com.cms.ui.components.ContentPanel;
import com.cms.ui.views.DashboardView;
import com.cms.ui.views.PatientsView;
import com.cms.ui.views.ClinicalHistoryView;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final NavigationDrawer navigationDrawer;
    private final ContentPanel contentPanel;
    private final AppFactory appFactory;

    public MainFrame() {
        this.appFactory = AppFactory.getInstance();

        setTitle("Clinical Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 600));
        setLocationRelativeTo(null);

        initializeLayout();

        navigationDrawer = new NavigationDrawer(this::navigateTo);
        contentPanel = new ContentPanel();

        add(navigationDrawer, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        navigateTo("dashboard");
    }

    private void initializeLayout() {
        setLayout(new BorderLayout());
    }

    public void navigateTo(String viewName) {
        JPanel view = switch (viewName.toLowerCase()) {
            case "dashboard" -> new DashboardView();
            case "patients", "pacientes" -> new PatientsView();
            case "history", "historias" -> new ClinicalHistoryView();
            default -> createPlaceholderView(viewName);
        };
        contentPanel.setContent(view);
        navigationDrawer.setActiveItem(viewName);
    }

    private JPanel createPlaceholderView(String name) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Vista: " + name, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    public AppFactory getAppFactory() {
        return appFactory;
    }
}
