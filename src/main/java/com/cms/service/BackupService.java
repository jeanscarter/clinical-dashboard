package com.cms.service;

import com.cms.infra.AppLogger;
import com.cms.infra.DatabaseConnection;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.*;

public class BackupService {

    private static final String BACKUP_DIR = "backups";
    private static final String ATTACHMENTS_DIR = "attachments";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final int MAX_BACKUPS = 10;

    private final DatabaseConnection dbConnection;

    public BackupService(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
        createBackupDirectory();
    }

    private void createBackupDirectory() {
        try {
            Files.createDirectories(Paths.get(BACKUP_DIR));
        } catch (IOException e) {
            AppLogger.error("Failed to create backup directory", e);
        }
    }

    public File createBackup() throws IOException {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String backupName = "cms_backup_" + timestamp + ".zip";
        File backupFile = new File(BACKUP_DIR, backupName);

        AppLogger.info("Creating backup: " + backupName);

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(backupFile))) {
            // Backup database
            File dbFile = new File(dbConnection.getDatabasePath());
            if (dbFile.exists()) {
                addFileToZip(zos, dbFile, "database/");
            }

            // Backup attachments
            Path attachmentsPath = Paths.get(ATTACHMENTS_DIR);
            if (Files.exists(attachmentsPath)) {
                Files.walk(attachmentsPath)
                        .filter(Files::isRegularFile)
                        .forEach(file -> {
                            try {
                                String entryName = "attachments/" + attachmentsPath.relativize(file).toString();
                                addFileToZip(zos, file.toFile(), entryName);
                            } catch (IOException e) {
                                AppLogger.error("Error adding file to backup: " + file, e);
                            }
                        });
            }

            // Add metadata
            String metadata = createMetadata();
            ZipEntry metaEntry = new ZipEntry("metadata.txt");
            zos.putNextEntry(metaEntry);
            zos.write(metadata.getBytes());
            zos.closeEntry();
        }

        AppLogger.info("Backup created successfully: " + backupFile.getAbsolutePath());
        cleanOldBackups();
        return backupFile;
    }

    private void addFileToZip(ZipOutputStream zos, File file, String basePath) throws IOException {
        String entryName = basePath.endsWith("/") ? basePath + file.getName() : basePath;
        ZipEntry entry = new ZipEntry(entryName.replace("\\", "/"));
        zos.putNextEntry(entry);

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
        }
        zos.closeEntry();
    }

    public void restoreBackup(File backupFile) throws IOException {
        AppLogger.info("Restoring backup from: " + backupFile.getName());

        Path tempDir = Files.createTempDirectory("cms_restore_");

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(backupFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path targetPath = tempDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.createDirectories(targetPath.getParent());
                    Files.copy(zis, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }

        // Restore database
        Path restoredDb = tempDir.resolve("database").resolve(new File(dbConnection.getDatabasePath()).getName());
        if (Files.exists(restoredDb)) {
            Files.copy(restoredDb, Paths.get(dbConnection.getDatabasePath()), StandardCopyOption.REPLACE_EXISTING);
        }

        // Restore attachments
        Path restoredAttachments = tempDir.resolve("attachments");
        if (Files.exists(restoredAttachments)) {
            copyDirectory(restoredAttachments, Paths.get(ATTACHMENTS_DIR));
        }

        // Cleanup temp directory
        deleteDirectory(tempDir);

        AppLogger.info("Backup restored successfully");
    }

    public List<BackupInfo> listBackups() {
        List<BackupInfo> backups = new ArrayList<>();
        File backupDir = new File(BACKUP_DIR);

        if (backupDir.exists() && backupDir.isDirectory()) {
            File[] files = backupDir.listFiles((dir, name) -> name.endsWith(".zip"));
            if (files != null) {
                for (File file : files) {
                    backups.add(new BackupInfo(
                            file.getName(),
                            file.getAbsolutePath(),
                            file.length(),
                            LocalDateTime.ofInstant(
                                    java.time.Instant.ofEpochMilli(file.lastModified()),
                                    java.time.ZoneId.systemDefault())));
                }
            }
        }

        return backups.stream()
                .sorted(Comparator.comparing(BackupInfo::createdAt).reversed())
                .collect(Collectors.toList());
    }

    public void deleteBackup(String backupName) throws IOException {
        Path backupPath = Paths.get(BACKUP_DIR, backupName);
        if (Files.exists(backupPath)) {
            Files.delete(backupPath);
            AppLogger.info("Backup deleted: " + backupName);
        }
    }

    private void cleanOldBackups() {
        List<BackupInfo> backups = listBackups();
        if (backups.size() > MAX_BACKUPS) {
            List<BackupInfo> toDelete = backups.subList(MAX_BACKUPS, backups.size());
            for (BackupInfo backup : toDelete) {
                try {
                    deleteBackup(backup.name());
                } catch (IOException e) {
                    AppLogger.error("Failed to delete old backup: " + backup.name(), e);
                }
            }
        }
    }

    private String createMetadata() {
        return """
                Clinical Management System Backup
                ==================================
                Created: %s
                Database: %s
                Version: 1.0
                """.formatted(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                dbConnection.getDatabasePath());
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(s -> {
            try {
                Path t = target.resolve(source.relativize(s));
                if (Files.isDirectory(s)) {
                    Files.createDirectories(t);
                } else {
                    Files.copy(s, t, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                AppLogger.error("Error copying file", e);
            }
        });
    }

    private void deleteDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walk(directory)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            AppLogger.error("Error deleting temp file", e);
                        }
                    });
        }
    }

    public record BackupInfo(String name, String path, long size, LocalDateTime createdAt) {
        public String getFormattedSize() {
            if (size < 1024)
                return size + " B";
            if (size < 1024 * 1024)
                return String.format("%.1f KB", size / 1024.0);
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }
    }
}
