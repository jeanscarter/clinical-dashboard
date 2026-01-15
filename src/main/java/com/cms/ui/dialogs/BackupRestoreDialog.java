package com.cms.ui.dialogs;

import com.cms.di.AppFactory;
import com.cms.infra.AppLogger;
import com.cms.service.BackupService;
import com.cms.service.BackupService.BackupInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BackupRestoreDialog extends JDialog {

    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final BackupService backupService;
    private JTable backupsTable;
    private DefaultTableModel tableModel;

    public BackupRestoreDialog(Frame parent) {
        super(parent, "Respaldo y Restauración", true);
        this.backupService = AppFactory.getInstance().getBackupService();

        setSize(700, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        loadBackups();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 41, 59));
        header.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Respaldo y Restauración");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Gestionar copias de seguridad del sistema");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(148, 163, 184));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        header.add(titlePanel, BorderLayout.WEST);

        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JButton createBackupBtn = new JButton("📦 Crear Respaldo");
        createBackupBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        createBackupBtn.setBackground(SUCCESS);
        createBackupBtn.setForeground(Color.WHITE);
        createBackupBtn.setFocusPainted(false);
        createBackupBtn.setBorderPainted(false);
        createBackupBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createBackupBtn.setPreferredSize(new Dimension(160, 40));
        createBackupBtn.addActionListener(e -> createBackup());

        JButton restoreExternalBtn = new JButton("📂 Restaurar Archivo...");
        restoreExternalBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        restoreExternalBtn.setBackground(PRIMARY);
        restoreExternalBtn.setForeground(Color.WHITE);
        restoreExternalBtn.setFocusPainted(false);
        restoreExternalBtn.setBorderPainted(false);
        restoreExternalBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        restoreExternalBtn.setPreferredSize(new Dimension(180, 40));
        restoreExternalBtn.addActionListener(e -> restoreFromFile());

        actionPanel.add(createBackupBtn);
        actionPanel.add(restoreExternalBtn);

        String[] columns = { "Nombre", "Tamaño", "Fecha de Creación", "Acciones" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        backupsTable = new JTable(tableModel);
        backupsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        backupsTable.setRowHeight(45);
        backupsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        backupsTable.getTableHeader().setBackground(new Color(248, 250, 252));
        backupsTable.setSelectionBackground(new Color(219, 234, 254));
        backupsTable.setGridColor(new Color(226, 232, 240));

        backupsTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        backupsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        backupsTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        backupsTable.getColumnModel().getColumn(3).setPreferredWidth(120);

        backupsTable.getColumnModel().getColumn(3)
                .setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
                    JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
                    panel.setOpaque(false);

                    JButton restoreBtn = new JButton("🔄");
                    restoreBtn.setToolTipText("Restaurar");
                    restoreBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
                    restoreBtn.setPreferredSize(new Dimension(35, 30));
                    restoreBtn.setBackground(new Color(219, 234, 254));
                    restoreBtn.setBorderPainted(false);

                    JButton deleteBtn = new JButton("🗑️");
                    deleteBtn.setToolTipText("Eliminar");
                    deleteBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
                    deleteBtn.setPreferredSize(new Dimension(35, 30));
                    deleteBtn.setBackground(new Color(254, 226, 226));
                    deleteBtn.setBorderPainted(false);

                    panel.add(restoreBtn);
                    panel.add(deleteBtn);

                    return panel;
                });

        backupsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int column = backupsTable.columnAtPoint(e.getPoint());
                int row = backupsTable.rowAtPoint(e.getPoint());
                if (column == 3 && row >= 0) {
                    Rectangle cellRect = backupsTable.getCellRect(row, column, true);
                    int xInCell = e.getPoint().x - cellRect.x;
                    int cellWidth = cellRect.width;

                    int buttonWidth = 35;
                    int buttonGap = 5;
                    int buttonsWidth = buttonWidth * 2 + buttonGap;
                    int startX = (cellWidth - buttonsWidth) / 2;
                    int restoreEnd = startX + buttonWidth;
                    int deleteStart = restoreEnd + buttonGap;
                    int deleteEnd = deleteStart + buttonWidth;

                    if (xInCell >= startX && xInCell < restoreEnd) {
                        restoreBackup(row);
                    } else if (xInCell >= deleteStart && xInCell < deleteEnd) {
                        deleteBackup(row);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(backupsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        content.add(actionPanel, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);

        return content;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(new Color(248, 250, 252));
        footer.setBorder(new EmptyBorder(10, 20, 10, 20));

        JButton closeBtn = new JButton("Cerrar");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        closeBtn.setPreferredSize(new Dimension(100, 35));
        closeBtn.addActionListener(e -> dispose());

        footer.add(closeBtn);

        return footer;
    }

    private void loadBackups() {
        tableModel.setRowCount(0);
        List<BackupInfo> backups = backupService.listBackups();
        for (BackupInfo backup : backups) {
            tableModel.addRow(new Object[] {
                    backup.name(),
                    backup.getFormattedSize(),
                    backup.createdAt().format(DATE_FORMAT),
                    ""
            });
        }
    }

    private void createBackup() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar ubicación para el respaldo");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            SwingWorker<File, Void> worker = new SwingWorker<>() {
                @Override
                protected File doInBackground() throws Exception {
                    return backupService.createBackup();
                }

                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    try {
                        File backupFile = get();
                        loadBackups();
                        JOptionPane.showMessageDialog(BackupRestoreDialog.this,
                                "Respaldo creado exitosamente:\n" + backupFile.getAbsolutePath(),
                                "Respaldo Exitoso",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        AppLogger.error("Error creating backup", ex);
                        JOptionPane.showMessageDialog(BackupRestoreDialog.this,
                                "Error al crear respaldo: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    private void restoreFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar archivo de respaldo para restaurar");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos ZIP", "zip"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            performRestore(selectedFile);
        }
    }

    private void restoreBackup(int row) {
        String backupName = (String) tableModel.getValueAt(row, 0);
        File backupFile = new File("backups", backupName);

        if (!backupFile.exists()) {
            JOptionPane.showMessageDialog(this,
                    "El archivo de respaldo no existe",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        performRestore(backupFile);
    }

    private void performRestore(File backupFile) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "⚠️ ADVERTENCIA: Restaurar un respaldo reemplazará todos los datos actuales.\n\n" +
                        "¿Está seguro de que desea continuar con la restauración?\n\n" +
                        "Archivo: " + backupFile.getName(),
                "Confirmar Restauración",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    backupService.restoreBackup(backupFile);
                    return null;
                }

                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    try {
                        get();
                        JOptionPane.showMessageDialog(BackupRestoreDialog.this,
                                "Restauración completada exitosamente.\n\n" +
                                        "Por favor, reinicie la aplicación para aplicar los cambios.",
                                "Restauración Exitosa",
                                JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } catch (Exception ex) {
                        AppLogger.error("Error restoring backup", ex);
                        JOptionPane.showMessageDialog(BackupRestoreDialog.this,
                                "Error al restaurar respaldo: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    private void deleteBackup(int row) {
        String backupName = (String) tableModel.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar el respaldo?\n\n" + backupName,
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                backupService.deleteBackup(backupName);
                loadBackups();
                JOptionPane.showMessageDialog(this,
                        "Respaldo eliminado exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                AppLogger.error("Error deleting backup", ex);
                JOptionPane.showMessageDialog(this,
                        "Error al eliminar respaldo: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
