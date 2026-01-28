package com.cms.ui.components;

import com.cms.domain.AppointmentStatus;
import com.cms.service.dto.AppointmentDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * UI component for displaying an appointment in a timeline card format.
 * Shows: Time | Patient Name + Age + Reason | Status + Action Button
 */
public class AppointmentCard extends JPanel {

    private static final Color PENDING_BG = new Color(254, 243, 199);
    private static final Color PENDING_BORDER = new Color(234, 179, 8);
    private static final Color IN_PROGRESS_BG = new Color(219, 234, 254);
    private static final Color IN_PROGRESS_BORDER = new Color(59, 130, 246);
    private static final Color COMPLETED_BG = new Color(220, 252, 231);
    private static final Color COMPLETED_BORDER = new Color(34, 197, 94);
    private static final Color CANCELLED_BG = new Color(254, 226, 226);
    private static final Color CANCELLED_BORDER = new Color(239, 68, 68);

    private final AppointmentDTO appointment;
    private Consumer<AppointmentDTO> onAttendClick;

    public AppointmentCard(AppointmentDTO appointment) {
        this.appointment = appointment;
        initUI();
    }

    public void setOnAttendClick(Consumer<AppointmentDTO> handler) {
        this.onAttendClick = handler;
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 0));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getBorderColor(), 1),
                new EmptyBorder(12, 16, 12, 16)));
        setBackground(getBackgroundColor());
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // Left: Time
        JPanel timePanel = new JPanel(new BorderLayout());
        timePanel.setOpaque(false);
        timePanel.setPreferredSize(new Dimension(70, 60));

        JLabel timeLabel = new JLabel(appointment.getFormattedTime());
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        timeLabel.setForeground(new Color(15, 23, 42));
        timePanel.add(timeLabel, BorderLayout.CENTER);

        // Center: Patient Info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        String patientText = appointment.getPatientName() != null ? appointment.getPatientName()
                : "Paciente desconocido";
        if (appointment.getPatientAge() != null) {
            patientText += " (" + appointment.getPatientAge() + " años)";
        }

        JLabel patientLabel = new JLabel(patientText);
        patientLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        patientLabel.setForeground(new Color(15, 23, 42));

        JLabel reasonLabel = new JLabel(
                appointment.getReason() != null ? appointment.getReason() : "Sin motivo especificado");
        reasonLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        reasonLabel.setForeground(new Color(100, 116, 139));

        infoPanel.add(patientLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(reasonLabel);

        // Right: Status and Action
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        // Status badge
        JLabel statusBadge = createStatusBadge();
        actionPanel.add(statusBadge);

        // "Atender" button only for PENDING appointments
        if (appointment.getStatus() == AppointmentStatus.PENDING) {
            JButton attendBtn = new JButton("Atender");
            attendBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            attendBtn.setBackground(new Color(34, 197, 94));
            attendBtn.setForeground(Color.WHITE);
            attendBtn.setFocusPainted(false);
            attendBtn.setBorderPainted(false);
            attendBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            attendBtn.setPreferredSize(new Dimension(80, 30));
            attendBtn.addActionListener(e -> {
                if (onAttendClick != null) {
                    onAttendClick.accept(appointment);
                }
            });
            actionPanel.add(attendBtn);
        }

        add(timePanel, BorderLayout.WEST);
        add(infoPanel, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.EAST);
    }

    private JLabel createStatusBadge() {
        JLabel badge = new JLabel(appointment.getStatusDisplayName());
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));

        switch (appointment.getStatus()) {
            case PENDING -> {
                badge.setBackground(PENDING_BG);
                badge.setForeground(new Color(161, 98, 7));
            }
            case IN_PROGRESS -> {
                badge.setBackground(IN_PROGRESS_BG);
                badge.setForeground(new Color(30, 64, 175));
            }
            case COMPLETED -> {
                badge.setBackground(COMPLETED_BG);
                badge.setForeground(new Color(21, 128, 61));
            }
            case CANCELLED -> {
                badge.setBackground(CANCELLED_BG);
                badge.setForeground(new Color(185, 28, 28));
            }
        }

        return badge;
    }

    private Color getBackgroundColor() {
        return switch (appointment.getStatus()) {
            case PENDING -> new Color(255, 251, 235);
            case IN_PROGRESS -> new Color(239, 246, 255);
            case COMPLETED -> new Color(240, 253, 244);
            case CANCELLED -> new Color(254, 242, 242);
        };
    }

    private Color getBorderColor() {
        return switch (appointment.getStatus()) {
            case PENDING -> PENDING_BORDER;
            case IN_PROGRESS -> IN_PROGRESS_BORDER;
            case COMPLETED -> COMPLETED_BORDER;
            case CANCELLED -> CANCELLED_BORDER;
        };
    }
}
