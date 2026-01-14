package com.cms.ui.dialogs;

import com.cms.core.validation.ClinicalHistoryValidator;
import com.cms.core.validation.ValidationResult;
import com.cms.di.AppFactory;
import com.cms.domain.Attachment;
import com.cms.domain.ClinicalHistory;
import com.cms.domain.Patient;
import com.cms.repository.AttachmentRepository;
import com.cms.repository.ClinicalHistoryRepository;
import com.cms.repository.PatientRepository;
import com.cms.ui.components.AttachmentPanel;
import com.cms.ui.dialogs.ImageViewerDialog;
import com.cms.util.ClipboardImageHandler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClinicalHistoryDialog extends JDialog {

    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final Color LABEL_COLOR = new Color(71, 85, 105);
    private static final Color ERROR_COLOR = new Color(239, 68, 68);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PatientRepository patientRepository;
    private final ClinicalHistoryRepository historyRepository;
    private final AttachmentRepository attachmentRepository;
    private final ClinicalHistoryValidator validator;
    private final ClipboardImageHandler clipboardHandler;

    private JComboBox<PatientItem> patientCombo;
    private JTextField fechaField;
    private JTextField motivoField;
    private JTextArea antecedentesArea;
    private JTextArea examenArea;
    private JTextArea diagnosticoArea;
    private JTextArea conductaArea;
    private JTextArea observacionesArea;
    private JTextField medicoField;
    private JLabel errorLabel;
    private AttachmentPanel attachmentPanel;

    private ClinicalHistory currentHistory;
    private final List<TempAttachment> tempAttachments;
    private Consumer<ClinicalHistory> onSaveCallback;

    public ClinicalHistoryDialog(Frame parent, ClinicalHistory history) {
        super(parent, history == null ? "Nueva Consulta" : "Editar Consulta", true);

        AppFactory factory = AppFactory.getInstance();
        this.patientRepository = factory.getPatientRepository();
        this.historyRepository = factory.getClinicalHistoryRepository();
        this.attachmentRepository = factory.getAttachmentRepository();
        this.validator = new ClinicalHistoryValidator();
        this.clipboardHandler = new ClipboardImageHandler();
        this.currentHistory = history;
        this.tempAttachments = new ArrayList<>();

        setSize(750, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        loadPatients();
        if (history != null) {
            populateForm(history);
        }
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        String icon = currentHistory == null ? "📝" : "✏️";
        String title = currentHistory == null ? "Nueva Consulta" : "Editar Consulta";

        JLabel titleLabel = new JLabel(icon + " " + title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        header.add(titleLabel, BorderLayout.WEST);
        return header;
    }

    private JScrollPane createFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        form.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.NORTHWEST;

        patientCombo = new JComboBox<>();
        patientCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        fechaField = createTextField();
        fechaField.setText(LocalDateTime.now().format(DATE_FORMAT));

        motivoField = createTextField();
        antecedentesArea = createTextArea(2);
        examenArea = createTextArea(2);
        diagnosticoArea = createTextArea(2);
        conductaArea = createTextArea(2);
        observacionesArea = createTextArea(2);
        medicoField = createTextField();

        int row = 0;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.25;
        form.add(createLabel("Paciente *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.75;
        gbc.gridwidth = 2;
        patientCombo.setPreferredSize(new Dimension(400, 35));
        form.add(patientCombo, gbc);
        gbc.gridwidth = 1;
        row++;

        addFormRow(form, gbc, row++, "Fecha *", fechaField);
        addFormRow(form, gbc, row++, "Motivo Consulta *", motivoField);
        addTextAreaRow(form, gbc, row++, "Antecedentes", antecedentesArea);
        addTextAreaRow(form, gbc, row++, "Examen Físico", examenArea);
        addTextAreaRow(form, gbc, row++, "Diagnóstico", diagnosticoArea);
        addTextAreaRow(form, gbc, row++, "Conducta", conductaArea);
        addTextAreaRow(form, gbc, row++, "Observaciones", observacionesArea);
        addFormRow(form, gbc, row++, "Médico", medicoField);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.3;

        attachmentPanel = new AttachmentPanel();
        attachmentPanel.setPreferredSize(new Dimension(0, 180));
        setupAttachmentHandlers();
        form.add(attachmentPanel, gbc);
        row++;

        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(ERROR_COLOR);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 3;
        form.add(errorLabel, gbc);

        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private void setupAttachmentHandlers() {
        attachmentPanel.setOnPasteFromClipboard(v -> pasteFromClipboard());
        attachmentPanel.setOnAddFromFile(this::addFromFile);
        attachmentPanel.setOnRemoveAttachment(this::removeAttachment);
        attachmentPanel.setOnViewAttachment(this::viewAttachment);
    }

    private void pasteFromClipboard() {
        if (!clipboardHandler.hasImageInClipboard()) {
            showError("No hay imagen en el portapapeles");
            return;
        }

        clipboardHandler.getImageFromClipboard().ifPresentOrElse(
                image -> {
                    String tempFileName = "clipboard_" + System.currentTimeMillis() + ".png";
                    File tempDir = new File(System.getProperty("java.io.tmpdir"), "cms_temp");
                    tempDir.mkdirs();
                    File tempFile = new File(tempDir, tempFileName);

                    clipboardHandler.saveImageToTempFile(image, tempFile).ifPresentOrElse(
                            file -> {
                                TempAttachment temp = new TempAttachment(file, tempFileName, "image/png",
                                        file.length());
                                tempAttachments.add(temp);
                                updateAttachmentDisplay();
                                clearError();
                            },
                            () -> showError("Error al guardar imagen del portapapeles"));
                },
                () -> showError("No se pudo obtener la imagen del portapapeles"));
    }

    private void addFromFile(String filePath) {
        File sourceFile = new File(filePath);
        if (!sourceFile.exists()) {
            showError("El archivo no existe");
            return;
        }

        long maxSize = 10 * 1024 * 1024; // 10MB
        if (sourceFile.length() > maxSize) {
            showError("El archivo excede el tamaño máximo de 10MB");
            return;
        }

        long totalSize = tempAttachments.stream().mapToLong(t -> t.size).sum();
        if (totalSize + sourceFile.length() > 50 * 1024 * 1024) { // 50MB total
            showError("Se ha excedido el límite total de 50MB para adjuntos");
            return;
        }

        String mimeType = clipboardHandler.getMimeType(filePath);
        TempAttachment temp = new TempAttachment(sourceFile, sourceFile.getName(), mimeType, sourceFile.length());
        tempAttachments.add(temp);
        updateAttachmentDisplay();
        clearError();
    }

    private void removeAttachment(Integer index) {
        if (index != null && index >= 0 && index < tempAttachments.size()) {
            TempAttachment removed = tempAttachments.remove((int) index);
            if (removed.file.getPath().contains("cms_temp")) {
                removed.file.delete();
            }
            updateAttachmentDisplay();
        }
    }

    private void viewAttachment(Attachment attachment) {
        int index = attachment.getId();
        if (index >= 0 && index < tempAttachments.size()) {
            TempAttachment temp = tempAttachments.get(index);
            if (temp.mimeType.startsWith("image/")) {
                ImageViewerDialog viewer = new ImageViewerDialog(
                        SwingUtilities.getWindowAncestor(this),
                        temp.name,
                        temp.file.getAbsolutePath());
                viewer.setVisible(true);
            } else {
                try {
                    Desktop.getDesktop().open(temp.file);
                } catch (Exception e) {
                    showError("No se puede abrir el archivo");
                }
            }
        }
    }

    private void updateAttachmentDisplay() {
        List<Attachment> displayList = new ArrayList<>();
        for (int i = 0; i < tempAttachments.size(); i++) {
            TempAttachment temp = tempAttachments.get(i);
            Attachment display = new Attachment();
            display.setId(i);
            display.setNombre(temp.name);
            display.setRutaArchivo(temp.file.getAbsolutePath());
            display.setTipo(temp.mimeType);
            display.setTamanoBytes(temp.size);
            displayList.add(display);
        }
        attachmentPanel.setAttachments(displayList);
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.25;
        form.add(createLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.75;
        gbc.gridwidth = 2;
        field.setPreferredSize(new Dimension(400, 35));
        form.add(field, gbc);
        gbc.gridwidth = 1;
    }

    private void addTextAreaRow(JPanel form, GridBagConstraints gbc, int row, String label, JTextArea area) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.25;
        form.add(createLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.75;
        gbc.gridwidth = 2;
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setPreferredSize(new Dimension(400, 55));
        form.add(scrollPane, gbc);
        gbc.gridwidth = 1;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(LABEL_COLOR);
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return field;
    }

    private JTextArea createTextArea(int rows) {
        JTextArea area = new JTextArea(rows, 30);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBorder(new EmptyBorder(10, 20, 10, 20));
        footer.setBackground(new Color(248, 250, 252));

        JButton cancelBtn = new JButton("Cancelar");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelBtn.setPreferredSize(new Dimension(100, 38));
        cancelBtn.addActionListener(e -> {
            cleanupTempFiles();
            dispose();
        });

        JButton saveBtn = new JButton("Guardar");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setBackground(PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setPreferredSize(new Dimension(100, 38));
        saveBtn.addActionListener(e -> saveHistory());

        footer.add(cancelBtn);
        footer.add(saveBtn);
        return footer;
    }

    private void loadPatients() {
        patientCombo.removeAllItems();
        for (Patient p : patientRepository.findAll()) {
            patientCombo.addItem(new PatientItem(p.getId(), p.getNombreCompleto() + " - " + p.getCedula()));
        }
    }

    public void selectPatient(Integer patientId) {
        for (int i = 0; i < patientCombo.getItemCount(); i++) {
            PatientItem item = patientCombo.getItemAt(i);
            if (item.id().equals(patientId)) {
                patientCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void populateForm(ClinicalHistory history) {
        if (history.getPatientId() != null) {
            selectPatient(history.getPatientId());
        }
        fechaField.setText(history.getFechaConsulta() != null ? history.getFechaConsulta().format(DATE_FORMAT) : "");
        motivoField.setText(history.getMotivoConsulta() != null ? history.getMotivoConsulta() : "");
        antecedentesArea.setText(history.getAntecedentes() != null ? history.getAntecedentes() : "");
        examenArea.setText(history.getExamenFisico() != null ? history.getExamenFisico() : "");
        diagnosticoArea.setText(history.getDiagnostico() != null ? history.getDiagnostico() : "");
        conductaArea.setText(history.getConducta() != null ? history.getConducta() : "");
        observacionesArea.setText(history.getObservaciones() != null ? history.getObservaciones() : "");
        medicoField.setText(history.getMedico() != null ? history.getMedico() : "");

        List<Attachment> existingAttachments = attachmentRepository.findByClinicalHistoryId(history.getId());
        for (Attachment att : existingAttachments) {
            File file = new File(att.getRutaArchivo());
            if (file.exists()) {
                TempAttachment temp = new TempAttachment(file, att.getNombre(), att.getTipo(), att.getTamanoBytes());
                temp.existingId = att.getId();
                tempAttachments.add(temp);
            }
        }
        updateAttachmentDisplay();
    }

    private ClinicalHistory buildHistory() {
        ClinicalHistory history = currentHistory != null ? currentHistory : new ClinicalHistory();

        PatientItem selectedPatient = (PatientItem) patientCombo.getSelectedItem();
        if (selectedPatient != null) {
            history.setPatientId(selectedPatient.id());
        }

        String fechaStr = fechaField.getText().trim();
        if (!fechaStr.isEmpty()) {
            try {
                history.setFechaConsulta(LocalDateTime.parse(fechaStr, DATE_FORMAT));
            } catch (Exception e) {
                history.setFechaConsulta(LocalDateTime.now());
            }
        } else {
            history.setFechaConsulta(LocalDateTime.now());
        }

        history.setMotivoConsulta(motivoField.getText().trim());
        history.setAntecedentes(antecedentesArea.getText().trim());
        history.setExamenFisico(examenArea.getText().trim());
        history.setDiagnostico(diagnosticoArea.getText().trim());
        history.setConducta(conductaArea.getText().trim());
        history.setObservaciones(observacionesArea.getText().trim());
        history.setMedico(medicoField.getText().trim());

        return history;
    }

    private void saveHistory() {
        ClinicalHistory history = buildHistory();
        ValidationResult validation = validator.validate(history);

        if (!validation.isValid()) {
            showError(validation.getErrorsAsString());
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<ClinicalHistory, Void> worker = new SwingWorker<>() {
            @Override
            protected ClinicalHistory doInBackground() {
                ClinicalHistory saved = historyRepository.save(history);

                for (TempAttachment temp : tempAttachments) {
                    if (temp.existingId == null) {
                        File destFile = clipboardHandler.copyFileToAttachments(
                                temp.file.getAbsolutePath(), saved.getId()).orElse(null);

                        if (destFile != null) {
                            Attachment attachment = new Attachment();
                            attachment.setClinicalHistoryId(saved.getId());
                            attachment.setNombre(temp.name);
                            attachment.setRutaArchivo(destFile.getAbsolutePath());
                            attachment.setTipo(temp.mimeType);
                            attachment.setTamanoBytes(destFile.length());
                            attachmentRepository.save(attachment);
                        }
                    }
                }

                return saved;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    ClinicalHistory saved = get();
                    cleanupTempFiles();
                    if (onSaveCallback != null) {
                        onSaveCallback.accept(saved);
                    }
                    dispose();
                } catch (Exception e) {
                    showError("Error al guardar: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void cleanupTempFiles() {
        for (TempAttachment temp : tempAttachments) {
            if (temp.file.getPath().contains("cms_temp") && temp.existingId == null) {
                temp.file.delete();
            }
        }
    }

    private void showError(String message) {
        errorLabel.setText("<html>" + message.replace("\n", "<br>") + "</html>");
    }

    private void clearError() {
        errorLabel.setText(" ");
    }

    public void setOnSaveCallback(Consumer<ClinicalHistory> callback) {
        this.onSaveCallback = callback;
    }

    private record PatientItem(Integer id, String display) {
        @Override
        public String toString() {
            return display;
        }
    }

    private static class TempAttachment {
        File file;
        String name;
        String mimeType;
        long size;
        Integer existingId;

        TempAttachment(File file, String name, String mimeType, long size) {
            this.file = file;
            this.name = name;
            this.mimeType = mimeType;
            this.size = size;
        }
    }
}
