package com.cms.ui.views;

import com.cms.di.AppFactory;
import com.cms.domain.Attachment;
import com.cms.domain.ClinicalHistory;
import com.cms.domain.Patient;
import com.cms.presenter.ClinicalHistoryContract;
import com.cms.repository.ClinicalHistoryRepository;
import com.cms.repository.PatientRepository;
import com.cms.service.ClinicalHistoryService;
import com.cms.ui.MainFrame;
import com.cms.ui.components.IconFactory;
import com.cms.ui.dialogs.ClinicalHistoryDialog;
import com.cms.ui.dialogs.ClinicalHistoryViewerDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClinicalHistoryView extends JPanel implements MainFrame.RefreshableView, ClinicalHistoryContract.View {

    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ClinicalHistoryContract.Presenter presenter;
    private final PatientRepository patientRepository;
    private final ClinicalHistoryRepository historyRepository;
    private final MainFrame mainFrame;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JComboBox<PatientItem> patientCombo;

    public ClinicalHistoryView(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        AppFactory factory = AppFactory.getInstance();
        this.patientRepository = factory.getPatientRepository();
        this.historyRepository = factory.getClinicalHistoryRepository();
        this.presenter = factory.getClinicalHistoryPresenter(this);

        setLayout(new BorderLayout());
        setBackground(new Color(241, 245, 249));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        // Auto-refresh when view becomes visible (tab switch)
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                presenter.loadAllHistories();
                refreshPatientCombo();
            }
        });

        presenter.loadAllHistories();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 25, 0));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Historias Clínicas");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(15, 23, 42));

        JLabel subtitle = new JLabel("Gestionar consultas y registros médicos");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 116, 139));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        JButton addButton = new JButton("Nueva Consulta",
                IconFactory.createPlusIcon(14, Color.WHITE));
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addButton.setIconTextGap(6);
        addButton.setBackground(PRIMARY);
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorderPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.setPreferredSize(new Dimension(160, 40));
        addButton.addActionListener(e -> showNewConsultationDialog());

        header.add(titlePanel, BorderLayout.WEST);
        header.add(addButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(20, 20, 20, 20)));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setOpaque(false);
        filterPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        filterPanel.add(new JLabel("Filtrar por paciente:"));
        patientCombo = new JComboBox<>();
        patientCombo.addItem(new PatientItem(null, "Todos los pacientes"));
        for (Patient p : patientRepository.findAll()) {
            patientCombo.addItem(new PatientItem(p.getId(), p.getNombreCompleto() + " - " + p.getCedula()));
        }
        patientCombo.setPreferredSize(new Dimension(300, 35));
        patientCombo.addActionListener(e -> filterByPatient());
        filterPanel.add(patientCombo);

        String[] columns = { "ID", "Paciente", "Edad", "Fecha", "Motivo", "Diagnóstico", "Médico", "Acciones" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        historyTable.setRowHeight(45);
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        historyTable.getTableHeader().setBackground(new Color(248, 250, 252));
        historyTable.setSelectionBackground(new Color(219, 234, 254));
        historyTable.setGridColor(new Color(226, 232, 240));

        historyTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(180);
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(180);
        historyTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(7).setPreferredWidth(250);
        historyTable.getColumnModel().getColumn(7).setMinWidth(250);

        historyTable.getColumnModel().getColumn(7)
                .setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
                    JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 5));
                    panel.setOpaque(true);
                    panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

                    JButton viewBtn = createTextIconButton("Ver",
                            IconFactory.createSearchIcon(12, new Color(59, 130, 246)),
                            new Color(219, 234, 254), new Color(59, 130, 246));
                    JButton editBtn = createTextIconButton("Editar",
                            IconFactory.createEditIcon(12, new Color(180, 140, 50)),
                            new Color(254, 243, 199), new Color(180, 140, 50));
                    JButton deleteBtn = createTextIconButton("Eliminar",
                            IconFactory.createTrashIcon(12, new Color(239, 68, 68)),
                            new Color(254, 226, 226), new Color(239, 68, 68));

                    panel.add(viewBtn);
                    panel.add(editBtn);
                    panel.add(deleteBtn);
                    return panel;
                });

        historyTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int column = historyTable.columnAtPoint(e.getPoint());
                int row = historyTable.rowAtPoint(e.getPoint());
                if (column == 7 && row >= 0) {
                    Rectangle cellRect = historyTable.getCellRect(row, column, true);
                    int xInCell = e.getPoint().x - cellRect.x;
                    int cellWidth = cellRect.width;
                    int id = (int) tableModel.getValueAt(row, 0);

                    // Divide cell into thirds: Ver | Editar | Eliminar
                    int thirdWidth = cellWidth / 3;
                    if (xInCell < thirdWidth) {
                        presenter.selectHistory(id);
                    } else if (xInCell < thirdWidth * 2) {
                        editHistory(row);
                    } else {
                        deleteHistory(id);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        content.add(filterPanel, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);

        return content;
    }

    private void filterByPatient() {
        PatientItem selected = (PatientItem) patientCombo.getSelectedItem();
        if (selected == null || selected.id == null) {
            presenter.loadAllHistories();
        } else {
            presenter.loadHistoriesByPatient(selected.id);
        }
    }

    public void showNewConsultationDialog() {
        ClinicalHistoryDialog dialog = new ClinicalHistoryDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setOnSaveCallback(saved -> {
            presenter.loadAllHistories();
            refreshPatientCombo();
        });
        dialog.setVisible(true);
    }

    public void selectPatient(Integer patientId) {
        for (int i = 0; i < patientCombo.getItemCount(); i++) {
            PatientItem item = patientCombo.getItemAt(i);
            if (item.id != null && item.id.equals(patientId)) {
                patientCombo.setSelectedIndex(i);
                filterByPatient();
                break;
            }
        }
    }

    private void editHistory(int row) {
        int id = (int) tableModel.getValueAt(row, 0);
        // Buscar la historia clínica
        historyRepository.findById(id).ifPresent(history -> {
            ClinicalHistoryDialog dialog = new ClinicalHistoryDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this), history);
            dialog.setOnSaveCallback(saved -> {
                presenter.loadAllHistories();
                showSuccess("Historia clínica actualizada correctamente");
            });
            dialog.setVisible(true);
        });
    }

    private void deleteHistory(int id) {
        int option = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de que desea ELIMINAR esta consulta?\n" +
                        "¡ADVERTENCIA: Esta acción es IRREVERSIBLE!\n" +
                        "Se eliminarán todos los datos y fotos adjuntas.",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            try {
                ClinicalHistoryService historyService = AppFactory.getInstance().getClinicalHistoryService();
                historyService.deleteHistory(id);

                JOptionPane.showMessageDialog(this,
                        "La consulta ha sido eliminada correctamente.",
                        "Eliminado",
                        JOptionPane.INFORMATION_MESSAGE);

                presenter.loadAllHistories();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error al eliminar la consulta: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshPatientCombo() {
        PatientItem selected = (PatientItem) patientCombo.getSelectedItem();
        patientCombo.removeAllItems();
        patientCombo.addItem(new PatientItem(null, "Todos los pacientes"));
        for (Patient p : patientRepository.findAll()) {
            patientCombo.addItem(new PatientItem(p.getId(), p.getNombreCompleto() + " - " + p.getCedula()));
        }
        if (selected != null && selected.id != null) {
            selectPatient(selected.id);
        }
    }

    // --- Implementación de ClinicalHistoryContract.View ---

    @Override
    public void showHistories(List<ClinicalHistory> histories) {
        tableModel.setRowCount(0);
        for (ClinicalHistory history : histories) {
            String patientName = "";
            String patientAge = "";
            if (history.getPatientId() != null) {
                var patientOpt = patientRepository.findById(history.getPatientId());
                patientName = patientOpt.map(Patient::getNombreCompleto).orElse("Desconocido");
                patientAge = patientOpt.map(p -> p.getAge() != null ? p.getAge() + " años" : "-").orElse("-");
            }

            tableModel.addRow(new Object[] {
                    history.getId(),
                    patientName,
                    patientAge,
                    history.getFechaConsulta() != null ? history.getFechaConsulta().format(DATE_FORMATTER) : "",
                    history.getMotivoConsulta(),
                    history.getDiagnostico(),
                    history.getMedico(),
                    ""
            });
        }
    }

    @Override
    public void showHistoryDetails(ClinicalHistory history) {
        ClinicalHistoryViewerDialog dialog = new ClinicalHistoryViewerDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), history);
        dialog.setVisible(true);
    }

    @Override
    public void showAttachments(List<Attachment> attachments) {
        // Implementation for showing attachments in a panel or dialog
        // This would typically update a list component in the UI
    }

    @Override
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void showLoading(boolean loading) {
        setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    @Override
    public void clearForm() {
        // Clear any form fields if applicable
    }

    @Override
    public void showImagePreview(BufferedImage image, String fileName) {
        // Show image preview in a dialog
        if (image != null) {
            ImageIcon icon = new ImageIcon(image.getScaledInstance(400, -1, Image.SCALE_SMOOTH));
            JLabel label = new JLabel(icon);
            JOptionPane.showMessageDialog(this, label, "Vista previa: " + fileName, JOptionPane.PLAIN_MESSAGE);
        }
    }

    @Override
    public void updateAttachmentsList() {
        // Refresh attachments list if displayed
    }

    @Override
    public void refresh() {
        presenter.loadAllHistories();
        refreshPatientCombo();
    }

    private record PatientItem(Integer id, String display) {
        @Override
        public String toString() {
            return display;
        }
    }

    private JButton createIconButton(Icon icon, Color bgColor, String tooltip) {
        JButton btn = new JButton(icon);
        btn.setToolTipText(tooltip);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setMargin(new Insets(4, 8, 4, 8));
        btn.setPreferredSize(new Dimension(36, 28));
        btn.setMaximumSize(new Dimension(36, 28));
        return btn;
    }

    private JButton createTextIconButton(String text, Icon icon, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text, icon);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setIconTextGap(4);
        btn.setMargin(new Insets(2, 6, 2, 6));
        btn.setPreferredSize(new Dimension(70, 28));
        btn.setMaximumSize(new Dimension(70, 28));
        return btn;
    }
}
