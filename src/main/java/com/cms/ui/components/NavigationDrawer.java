package com.cms.ui.components;

import com.cms.domain.User;
import com.cms.infra.SecurityContext;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class NavigationDrawer extends JPanel {

    private static final int DRAWER_WIDTH = 250;
    private static final Color DRAWER_BG = new Color(30, 41, 59);
    private static final Color ITEM_HOVER = new Color(51, 65, 85);
    private static final Color ITEM_ACTIVE = new Color(59, 130, 246);
    private static final Color TEXT_COLOR = new Color(226, 232, 240);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);

    private final Consumer<String> navigationHandler;
    private final List<NavItem> navItems;
    private String activeItem = "";

    public NavigationDrawer(Consumer<String> navigationHandler) {
        this.navigationHandler = navigationHandler;
        this.navItems = new ArrayList<>();

        setPreferredSize(new Dimension(DRAWER_WIDTH, 0));
        setBackground(DRAWER_BG);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createNavigationPanel(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(DRAWER_BG);
        header.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel logo = new JLabel("CMS");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logo.setForeground(ITEM_ACTIVE);

        JLabel subtitle = new JLabel("Clinical Management");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_MUTED);

        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBackground(DRAWER_BG);
        logoPanel.add(logo);
        logoPanel.add(Box.createVerticalStrut(4));
        logoPanel.add(subtitle);

        header.add(logoPanel, BorderLayout.CENTER);

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(51, 65, 85));
        header.add(separator, BorderLayout.SOUTH);

        return header;
    }

    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(DRAWER_BG);
        navPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        addNavItem(navPanel, "dashboard", "Dashboard",
                c -> IconFactory.createChartIcon(18, c));
        addNavItem(navPanel, "pacientes", "Pacientes",
                c -> IconFactory.createUsersIcon(18, c));
        addNavItem(navPanel, "agenda", "Agenda",
                c -> IconFactory.createCalendarIcon(18, c));
        addNavItem(navPanel, "historias", "Historias Clínicas",
                c -> IconFactory.createDocumentIcon(18, c));
        addNavItem(navPanel, "reportes", "Reportes",
                c -> IconFactory.createReportIcon(18, c));

        navPanel.add(Box.createVerticalStrut(20));

        // Configuración siempre visible
        addNavItem(navPanel, "settings", "Configuración",
                c -> IconFactory.createSettingsIcon(18, c));

        User currentUser = SecurityContext.getCurrentUser();
        if (currentUser != null && currentUser.getRole().canManageUsers()) {
            addNavItem(navPanel, "usuarios", "Usuarios",
                    c -> IconFactory.createUserIcon(18, c));
        }
        if (currentUser != null && currentUser.getRole().canManageBackups()) {
            addNavItem(navPanel, "respaldo", "Respaldo",
                    c -> IconFactory.createDocumentIcon(18, c));
        }

        navPanel.add(Box.createVerticalGlue());

        return navPanel;
    }

    private void addNavItem(JPanel parent, String id, String label,
            Function<Color, Icon> iconFactory) {
        NavItem item = new NavItem(id, label, iconFactory);
        navItems.add(item);
        parent.add(item);
        parent.add(Box.createVerticalStrut(5));
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(DRAWER_BG);
        footer.setBorder(new EmptyBorder(15, 15, 15, 15));

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(51, 65, 85));
        footer.add(separator, BorderLayout.NORTH);

        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(DRAWER_BG);
        userPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel userIcon = new JLabel(IconFactory.createUserIcon(20, TEXT_COLOR));
        userIcon.setBorder(new EmptyBorder(0, 0, 0, 10));

        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setBackground(DRAWER_BG);

        User currentUser = SecurityContext.getCurrentUser();
        String displayName = currentUser != null ? currentUser.getFullName() : "Usuario";
        JLabel userName = new JLabel(displayName);
        userName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userName.setForeground(TEXT_COLOR);

        String roleName = currentUser != null ? currentUser.getRole().getDisplayName() : "Sistema";
        JLabel userRole = new JLabel(roleName);
        userRole.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        userRole.setForeground(TEXT_MUTED);

        userInfo.add(userName);
        userInfo.add(userRole);

        userPanel.add(userIcon, BorderLayout.WEST);
        userPanel.add(userInfo, BorderLayout.CENTER);

        footer.add(userPanel, BorderLayout.CENTER);

        return footer;
    }

    public void setActiveItem(String itemId) {
        this.activeItem = itemId.toLowerCase();
        for (NavItem item : navItems) {
            item.updateState();
        }
    }

    public void refresh() {
        // Refresh footer with current user info
        removeAll();
        add(createHeader(), BorderLayout.NORTH);
        add(createNavigationPanel(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
        revalidate();
        repaint();
    }

    private class NavItem extends JPanel {
        private final String id;
        private final JLabel iconLabel;
        private final JLabel textLabel;
        private final Function<Color, Icon> iconFactory;
        private boolean isHovered = false;

        public NavItem(String id, String text, Function<Color, Icon> iconFactory) {
            this.id = id;
            this.iconFactory = iconFactory;

            setLayout(new BorderLayout());
            setBackground(DRAWER_BG);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
            setBorder(new EmptyBorder(10, 15, 10, 15));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            iconLabel = new JLabel(iconFactory.apply(TEXT_COLOR));
            iconLabel.setBorder(new EmptyBorder(0, 0, 0, 12));

            textLabel = new JLabel(text);
            textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            textLabel.setForeground(TEXT_COLOR);

            add(iconLabel, BorderLayout.WEST);
            add(textLabel, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    updateState();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    updateState();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    navigationHandler.accept(id);
                }
            });
        }

        public void updateState() {
            boolean isActive = id.equalsIgnoreCase(activeItem);
            Color iconColor;
            if (isActive) {
                setBackground(ITEM_ACTIVE);
                textLabel.setForeground(Color.WHITE);
                iconColor = Color.WHITE;
            } else if (isHovered) {
                setBackground(ITEM_HOVER);
                textLabel.setForeground(TEXT_COLOR);
                iconColor = TEXT_COLOR;
            } else {
                setBackground(DRAWER_BG);
                textLabel.setForeground(TEXT_COLOR);
                iconColor = TEXT_COLOR;
            }
            iconLabel.setIcon(iconFactory.apply(iconColor));
            repaint();
        }
    }
}
