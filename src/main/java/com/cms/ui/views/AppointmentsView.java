package com.cms.ui.views;

import com.cms.di.AppFactory;
import com.cms.domain.AppointmentStatus;
import com.cms.service.AppointmentService;
import com.cms.service.dto.AppointmentDTO;
import com.cms.ui.MainFrame;
import com.cms.ui.components.IconFactory;
import com.cms.ui.dialogs.AppointmentDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Vista principal para la gestión de citas (Agenda).
 * Muestra las citas del día seleccionado con acciones disponibles.
 */
public class AppointmentsView extends JPanel implements MainFrame.RefreshableView {

    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final Color PRIMARY_DARK = new Color(37, 99, 235);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color WARNING = new Color(245, 158, 11);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color BG_COLOR = new Color(241, 245, 249);

    private static final DateTimeFormatter DATE_DISPLAY = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final AppointmentService appointmentService;
    private final MainFrame mainFrame;

    private LocalDate selectedDate = LocalDate.now();
    private JLabel dateLabel;
    private JPanel appointmentsContainer;
    private JLabel countLabel;

    public AppointmentsView(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.appointmentService = AppFactory.getInstance().getAppointmentService();

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        loadAppointments();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gradient = new GradientPaint(0, 0, PRIMARY, getWidth(), 0, PRIMARY_DARK);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Left side - Title and date
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel icon = new JLabel(IconFactory.createCalendarIcon(32, Color.WHITE));
        JLabel title = new JLabel("Agenda de Citas");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        titleRow.add(icon);
        titleRow.add(title);

        // Date display
        dateLabel = new JLabel(selectedDate.format(DATE_DISPLAY));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        dateLabel.setForeground(new Color(191, 219, 254));
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateLabel.setBorder(new EmptyBorder(5, 5, 0, 0));

        titlePanel.add(titleRow);
        titlePanel.add(dateLabel);

        // Right side - Date navigation and Add button
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        // Previous day button
        JButton prevBtn = new JButton(IconFactory.createChevronLeftIcon(16, PRIMARY));
        prevBtn.setBackground(Color.WHITE);
        prevBtn.setPreferredSize(new Dimension(45, 40));
        prevBtn.setBorderPainted(false);
        prevBtn.setFocusPainted(false);
        prevBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        prevBtn.setToolTipText("Día anterior");
        prevBtn.addActionListener(e -> navigateDate(-1));

        // Today button
        JButton todayBtn = new JButton("Hoy");
        todayBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        todayBtn.setBackground(Color.WHITE);
        todayBtn.setForeground(PRIMARY);
        todayBtn.setPreferredSize(new Dimension(70, 40));
        todayBtn.setBorderPainted(false);
        todayBtn.setFocusPainted(false);
        todayBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        todayBtn.addActionListener(e -> {
            selectedDate = LocalDate.now();
            loadAppointments();
        });

        // Next day button
        JButton nextBtn = new JButton(IconFactory.createChevronRightIcon(16, PRIMARY));
        nextBtn.setBackground(Color.WHITE);
        nextBtn.setPreferredSize(new Dimension(45, 40));
        nextBtn.setBorderPainted(false);
        nextBtn.setFocusPainted(false);
        nextBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nextBtn.setToolTipText("Día siguiente");
        nextBtn.addActionListener(e -> navigateDate(1));

        // Add appointment button
        JButton addBtn = new JButton("+ Nueva Cita");
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addBtn.setBackground(SUCCESS);
        addBtn.setForeground(Color.WHITE);
        addBtn.setPreferredSize(new Dimension(140, 45));
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> showNewAppointmentDialog());

        actionPanel.add(prevBtn);
        actionPanel.add(todayBtn);
        actionPanel.add(nextBtn);
        actionPanel.add(Box.createHorizontalStrut(15));
        actionPanel.add(addBtn);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(actionPanel, BorderLayout.EAST);

        return header;
    }

    private JButton createNavButton(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(new Color(255, 255, 255, 50));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(45, 40));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(tooltip);
        return btn;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(BG_COLOR);
        content.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Count header
        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        countPanel.setOpaque(false);
        countPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        countLabel = new JLabel("Cargando...");
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        countLabel.setForeground(TEXT_PRIMARY);
        countPanel.add(countLabel);

        // Appointments list
        appointmentsContainer = new JPanel();
        appointmentsContainer.setLayout(new BoxLayout(appointmentsContainer, BoxLayout.Y_AXIS));
        appointmentsContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(appointmentsContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        content.add(countPanel, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);

        return content;
    }

    private void navigateDate(int days) {
        selectedDate = selectedDate.plusDays(days);
        loadAppointments();
    }

    private void loadAppointments() {
        dateLabel.setText(selectedDate.format(DATE_DISPLAY));
        appointmentsContainer.removeAll();

        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByDate(selectedDate);

        if (appointments.isEmpty()) {
            countLabel.setText("No hay citas para este día");
            appointmentsContainer.add(createEmptyState());
        } else {
            countLabel.setText(appointments.size() + " cita(s) programada(s)");
            for (AppointmentDTO appt : appointments) {
                appointmentsContainer.add(createAppointmentCard(appt));
                appointmentsContainer.add(Box.createVerticalStrut(12));
            }
        }

        appointmentsContainer.revalidate();
        appointmentsContainer.repaint();
    }

    private JPanel createEmptyState() {
        JPanel empty = new JPanel();
        empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
        empty.setOpaque(false);
        empty.setBorder(new EmptyBorder(60, 0, 60, 0));

        JLabel icon = new JLabel("📅");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel message = new JLabel("Sin citas programadas");
        message.setFont(new Font("Segoe UI", Font.BOLD, 18));
        message.setForeground(TEXT_SECONDARY);
        message.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hint = new JLabel("Haz clic en \"+ Nueva Cita\" para agendar");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        hint.setForeground(TEXT_SECONDARY);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        empty.add(icon);
        empty.add(Box.createVerticalStrut(20));
        empty.add(message);
        empty.add(Box.createVerticalStrut(8));
        empty.add(hint);

        return empty;
    }

    private JPanel createAppointmentCard(AppointmentDTO appt) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(18, 20, 18, 20)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Left: Time
        JPanel timePanel = new JPanel();
        timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.Y_AXIS));
        timePanel.setOpaque(false);
        timePanel.setPreferredSize(new Dimension(80, 60));

        JLabel timeLabel = new JLabel(appt.getTime().format(TIME_FORMAT));
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        timeLabel.setForeground(PRIMARY);
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        timePanel.add(Box.createVerticalGlue());
        timePanel.add(timeLabel);
        timePanel.add(Box.createVerticalGlue());

        // Center: Patient info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel patientLabel = new JLabel(appt.getPatientName() != null ? appt.getPatientName() : "Paciente");
        patientLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        patientLabel.setForeground(TEXT_PRIMARY);

        String reasonText = appt.getReason() != null && !appt.getReason().isEmpty()
                ? appt.getReason()
                : "Sin motivo especificado";
        JLabel reasonLabel = new JLabel(reasonText);
        reasonLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        reasonLabel.setForeground(TEXT_SECONDARY);

        infoPanel.add(patientLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(reasonLabel);

        // Right: Status and actions
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setOpaque(false);

        // Status badge
        JLabel statusBadge = createStatusBadge(appt.getStatus());
        actionsPanel.add(statusBadge);

        // Action buttons based on status
        if (appt.getStatus() == AppointmentStatus.PENDING) {
            JButton attendBtn = createActionButton("Atender", SUCCESS, e -> attendAppointment(appt));
            JButton cancelBtn = createActionButton("Cancelar", DANGER, e -> cancelAppointment(appt));
            actionsPanel.add(attendBtn);
            actionsPanel.add(cancelBtn);
        } else if (appt.getStatus() == AppointmentStatus.IN_PROGRESS) {
            JButton completeBtn = createActionButton("Completar", SUCCESS, e -> completeAppointment(appt));
            actionsPanel.add(completeBtn);
        }

        card.add(timePanel, BorderLayout.WEST);
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(actionsPanel, BorderLayout.EAST);

        return card;
    }

    private JLabel createStatusBadge(AppointmentStatus status) {
        String text;
        Color bgColor;

        switch (status) {
            case PENDING:
                text = "⏳ Pendiente";
                bgColor = WARNING;
                break;
            case IN_PROGRESS:
                text = "🔄 En curso";
                bgColor = PRIMARY;
                break;
            case COMPLETED:
                text = "✓ Completada";
                bgColor = SUCCESS;
                break;
            case CANCELLED:
                text = "✕ Cancelada";
                bgColor = DANGER;
                break;
            default:
                text = status.name();
                bgColor = TEXT_SECONDARY;
        }

        JLabel badge = new JLabel(text);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(bgColor);
        badge.setBorder(new EmptyBorder(5, 10, 5, 10));

        return badge;
    }

    private JButton createActionButton(String text, Color bgColor, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(90, 32));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        return btn;
    }

    private void showNewAppointmentDialog() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        AppointmentDialog dialog = new AppointmentDialog(owner);
        dialog.setOnSaveCallback(saved -> loadAppointments());
        dialog.setVisible(true);
    }

    private void attendAppointment(AppointmentDTO appt) {
        appointmentService.startAppointment(appt.getId());

        // Navigate to clinical history to create consultation
        if (mainFrame != null) {
            mainFrame.navigateToWithAction("history", "new_consultation_for_appointment",
                    appt.getPatientId() + "|" + appt.getReason());
        }

        loadAppointments();
    }

    private void cancelAppointment(AppointmentDTO appt) {
        int option = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de cancelar esta cita?",
                "Confirmar Cancelación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            appointmentService.cancelAppointment(appt.getId());
            loadAppointments();
        }
    }

    private void completeAppointment(AppointmentDTO appt) {
        appointmentService.completeAppointment(appt.getId());
        loadAppointments();
    }

    public void refresh() {
        loadAppointments();
    }
}
