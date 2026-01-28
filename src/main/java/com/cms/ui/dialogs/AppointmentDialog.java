package com.cms.ui.dialogs;

import com.cms.di.AppFactory;
import com.cms.domain.Patient;
import com.cms.repository.PatientRepository;
import com.cms.service.AppointmentService;
import com.cms.service.dto.AppointmentDTO;
import com.cms.ui.components.IconFactory;
import com.cms.ui.components.AutocompletePatientField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * Dialog para crear o editar citas médicas.
 * Diseño moderno con selector de paciente, fecha, hora y motivo.
 */
public class AppointmentDialog extends JDialog {

    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final Color PRIMARY_DARK = new Color(37, 99, 235);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color LABEL_COLOR = new Color(71, 85, 105);
    private static final Color BORDER_COLOR = new Color(203, 213, 225);
    private static final Color BG_COLOR = new Color(248, 250, 252);

    private final AppointmentService appointmentService;
    private final PatientRepository patientRepository;

    private AutocompletePatientField patientField;
    private JSpinner dateSpinner;
    private JSpinner hourSpinner;
    private JSpinner minuteSpinner;
    private JTextArea reasonArea;

    private Consumer<AppointmentDTO> onSaveCallback;
    private Integer preselectedPatientId;

    public AppointmentDialog(Frame parent) {
        this(parent, null);
    }

    public AppointmentDialog(Frame parent, Integer preselectedPatientId) {
        super(parent, "Nueva Cita", true);
        this.preselectedPatientId = preselectedPatientId;

        AppFactory factory = AppFactory.getInstance();
        this.appointmentService = factory.getAppointmentService();
        this.patientRepository = factory.getPatientRepository();

        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setSize(520, 520);
        setLocationRelativeTo(getParent());
        setBackground(BG_COLOR);
        getContentPane().setBackground(BG_COLOR);

        add(createHeader(), BorderLayout.NORTH);
        add(createForm(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        loadPatients();
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
        header.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 18));
        header.setPreferredSize(new Dimension(0, 70));

        JLabel icon = new JLabel(IconFactory.createCalendarIcon(28, Color.WHITE));

        JLabel title = new JLabel("Agendar Nueva Cita");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        header.add(icon);
        header.add(title);

        return header;
    }

    private JPanel createForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(BG_COLOR);
        form.setBorder(new EmptyBorder(25, 35, 20, 35));

        // Patient field
        form.add(createFieldLabel("Paciente *"));
        form.add(Box.createVerticalStrut(8));
        form.add(createPatientSelector());
        form.add(Box.createVerticalStrut(20));

        // Date and Time row
        JPanel dateTimeRow = new JPanel(new GridLayout(1, 2, 25, 0));
        dateTimeRow.setOpaque(false);
        dateTimeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
        dateTimeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateTimeRow.add(createDateField());
        dateTimeRow.add(createTimeField());
        form.add(dateTimeRow);
        form.add(Box.createVerticalStrut(20));

        // Reason field
        form.add(createFieldLabel("Motivo de la Consulta"));
        form.add(Box.createVerticalStrut(8));
        form.add(createReasonArea());

        return form;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private AutocompletePatientField createPatientSelector() {
        patientField = new AutocompletePatientField(patientRepository);
        patientField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        patientField.setAlignmentX(Component.LEFT_ALIGNMENT);
        return patientField;
    }

    private JPanel createDateField() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel label = new JLabel("Fecha *");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Date spinner
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateModel.setValue(java.util.Date.from(LocalDate.now().atStartOfDay()
                .atZone(java.time.ZoneId.systemDefault()).toInstant()));
        dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        dateSpinner.setPreferredSize(new Dimension(180, 42));
        dateSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(8));
        panel.add(dateSpinner);

        return panel;
    }

    private JPanel createTimeField() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel label = new JLabel("Hora *");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Time spinners in a row
        JPanel timeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        timeRow.setOpaque(false);
        timeRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        LocalTime now = LocalTime.now();

        hourSpinner = new JSpinner(new SpinnerNumberModel(now.getHour(), 0, 23, 1));
        hourSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        hourSpinner.setPreferredSize(new Dimension(72, 42));
        hourSpinner.setMinimumSize(new Dimension(72, 42));
        JComponent hourEditor = hourSpinner.getEditor();
        if (hourEditor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) hourEditor).getTextField();
            tf.setHorizontalAlignment(JTextField.CENTER);
            tf.setColumns(3);
        }

        JLabel separator = new JLabel(":");
        separator.setFont(new Font("Segoe UI", Font.BOLD, 20));
        separator.setForeground(TEXT_PRIMARY);

        minuteSpinner = new JSpinner(new SpinnerNumberModel(now.getMinute(), 0, 59, 5));
        minuteSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        minuteSpinner.setPreferredSize(new Dimension(72, 42));
        minuteSpinner.setMinimumSize(new Dimension(72, 42));
        JComponent minEditor = minuteSpinner.getEditor();
        if (minEditor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) minEditor).getTextField();
            tf.setHorizontalAlignment(JTextField.CENTER);
            tf.setColumns(3);
        }

        timeRow.add(hourSpinner);
        timeRow.add(separator);
        timeRow.add(minuteSpinner);

        panel.add(label);
        panel.add(Box.createVerticalStrut(8));
        panel.add(timeRow);

        return panel;
    }

    private JScrollPane createReasonArea() {
        reasonArea = new JTextArea(4, 40);
        reasonArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setMargin(new Insets(10, 12, 10, 12));

        JScrollPane scroll = new JScrollPane(reasonArea);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        scroll.setPreferredSize(new Dimension(400, 110));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        return scroll;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(new EmptyBorder(15, 35, 20, 35));
        footer.setBackground(BG_COLOR);

        // Button panel on the right
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);

        JButton cancelBtn = new JButton("Cancelar");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelBtn.setPreferredSize(new Dimension(110, 42));
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton("Guardar Cita");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setBackground(SUCCESS);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setPreferredSize(new Dimension(140, 42));
        saveBtn.setBorderPainted(false);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> saveAppointment());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        footer.add(buttonPanel, BorderLayout.EAST);
        return footer;
    }

    private void loadPatients() {
        // AutocompletePatientField loads patients automatically
        if (preselectedPatientId != null) {
            patientField.selectPatient(preselectedPatientId);
        }
    }

    private void saveAppointment() {
        // Validation
        Patient selectedPatient = patientField.getSelectedPatient();
        if (selectedPatient == null) {
            CustomAlertDialog.showWarning(this,
                    "Validación",
                    "Por favor seleccione un paciente.");
            return;
        }

        try {
            // Get date
            java.util.Date selectedDate = (java.util.Date) dateSpinner.getValue();
            LocalDate date = selectedDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();

            // Get time
            int hour = (Integer) hourSpinner.getValue();
            int minute = (Integer) minuteSpinner.getValue();
            LocalTime time = LocalTime.of(hour, minute);

            // Create DTO
            AppointmentDTO dto = new AppointmentDTO();
            dto.setPatientId(selectedPatient.getId());
            dto.setDate(date);
            dto.setTime(time);
            dto.setReason(reasonArea.getText().trim());

            // Save
            AppointmentDTO saved = appointmentService.createAppointment(dto);
            String patientName = selectedPatient.getNombreCompleto();
            String motivo = reasonArea.getText().trim();

            // Show success with custom dialog
            CustomAlertDialog.showSuccess(this,
                    "Cita Guardada",
                    "Cita agendada correctamente para:\n" +
                            date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                            " a las " + time.format(DateTimeFormatter.ofPattern("HH:mm")));

            // Ask if user wants to create clinical history immediately
            boolean wantsHistory = CustomAlertDialog.showConfirm(this,
                    "Historia Médica",
                    "¿Desea realizar ya mismo la Historia Médica para " + patientName + "?");

            if (wantsHistory) {
                // Open ClinicalHistoryDialog with pre-filled data
                dispose();
                ClinicalHistoryDialog historyDialog = new ClinicalHistoryDialog(
                        (Frame) getOwner(),
                        selectedPatient.getId(),
                        motivo,
                        "Hensy Sardi");
                historyDialog.setVisible(true);
            } else {
                if (onSaveCallback != null) {
                    onSaveCallback.accept(saved);
                }
                dispose();
            }

        } catch (Exception e) {
            CustomAlertDialog.showError(this,
                    "Error",
                    "Error al guardar la cita: " + e.getMessage());
        }
    }

    public void setOnSaveCallback(Consumer<AppointmentDTO> callback) {
        this.onSaveCallback = callback;
    }

}
