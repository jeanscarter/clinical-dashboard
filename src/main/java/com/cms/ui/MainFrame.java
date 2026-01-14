package com.cms.ui;

import com.cms.di.AppFactory;
import com.cms.ui.components.NavigationDrawer;
import com.cms.ui.components.ContentPanel;
import com.cms.ui.views.DashboardView;
import com.cms.ui.views.PatientsView;
import com.cms.ui.views.ClinicalHistoryView;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame {

    private final NavigationDrawer navigationDrawer;
    private final ContentPanel contentPanel;
    private final AppFactory appFactory;
    private final Map<String, JPanel> viewCache;
    private String currentViewName;

    public MainFrame() {
        this.appFactory = AppFactory.getInstance();
        this.viewCache = new HashMap<>();

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
        navigateToWithAction(viewName, null, null);
    }

    public void navigateToWithAction(String viewName, String action, Object data) {
        String normalizedName = viewName.toLowerCase();
        JPanel view = getOrCreateView(normalizedName);

        if (view != null) {
            contentPanel.setContent(view);
            navigationDrawer.setActiveItem(viewName);
            currentViewName = normalizedName;

            if (action != null) {
                executeViewAction(view, action, data);
            }
        }
    }

    private JPanel getOrCreateView(String viewName) {
        if (viewCache.containsKey(viewName)) {
            JPanel cached = viewCache.get(viewName);
            if (cached instanceof RefreshableView) {
                ((RefreshableView) cached).refresh();
            }
            return cached;
        }

        JPanel view = switch (viewName) {
            case "dashboard" -> new DashboardView(this);
            case "patients", "pacientes" -> new PatientsView(this);
            case "history", "historias" -> new ClinicalHistoryView(this);
            default -> createPlaceholderView(viewName);
        };

        viewCache.put(viewName, view);
        return view;
    }

    private void executeViewAction(JPanel view, String action, Object data) {
        SwingUtilities.invokeLater(() -> {
            switch (action) {
                case "new_patient" -> {
                    if (view instanceof PatientsView pv) {
                        pv.showNewPatientDialog();
                    }
                }
                case "search_patient" -> {
                    if (view instanceof PatientsView pv) {
                        pv.focusSearchField();
                    }
                }
                case "new_consultation" -> {
                    if (view instanceof ClinicalHistoryView chv) {
                        chv.showNewConsultationDialog();
                    }
                }
                case "select_patient" -> {
                    if (view instanceof ClinicalHistoryView chv && data instanceof Integer patientId) {
                        chv.selectPatient(patientId);
                    }
                }
            }
        });
    }

    public void refreshCurrentView() {
        if (currentViewName != null && viewCache.containsKey(currentViewName)) {
            JPanel view = viewCache.get(currentViewName);
            if (view instanceof RefreshableView) {
                ((RefreshableView) view).refresh();
            }
        }
    }

    public void clearViewCache() {
        viewCache.clear();
    }

    public void clearViewCache(String viewName) {
        viewCache.remove(viewName.toLowerCase());
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

    public interface RefreshableView {
        void refresh();
    }
}
