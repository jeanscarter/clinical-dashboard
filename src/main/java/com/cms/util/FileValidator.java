package com.cms.util;

import com.cms.service.exception.BusinessException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileValidator {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_TOTAL_SIZE = 50 * 1024 * 1024; // 50MB

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".png", ".jpg", ".jpeg", ".gif", ".pdf", ".doc", ".docx");

    private static final Map<String, byte[]> MAGIC_BYTES = new HashMap<>();

    static {
        MAGIC_BYTES.put(".png", new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47 });
        MAGIC_BYTES.put(".jpg", new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF });
        MAGIC_BYTES.put(".jpeg", new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF });
        MAGIC_BYTES.put(".gif", new byte[] { 0x47, 0x49, 0x46, 0x38 });
        MAGIC_BYTES.put(".pdf", new byte[] { 0x25, 0x50, 0x44, 0x46 });
        MAGIC_BYTES.put(".doc", new byte[] { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0 });
        MAGIC_BYTES.put(".docx", new byte[] { 0x50, 0x4B, 0x03, 0x04 });
    }

    public void validateFile(File file) {
        if (file == null) {
            throw new BusinessException("El archivo es nulo");
        }

        if (!file.exists()) {
            throw new BusinessException("El archivo no existe");
        }

        if (!file.isFile()) {
            throw new BusinessException("La ruta no es un archivo válido");
        }

        validateSize(file);
        validateExtension(file);
        validateMagicBytes(file);
        validateFileName(file.getName());
    }

    public void validateSize(File file) {
        if (file.length() > MAX_FILE_SIZE) {
            throw new BusinessException(String.format(
                    "El archivo excede el tamaño máximo de %.1f MB",
                    MAX_FILE_SIZE / (1024.0 * 1024.0)));
        }
    }

    public void validateTotalSize(long currentTotal, long newFileSize) {
        if (currentTotal + newFileSize > MAX_TOTAL_SIZE) {
            throw new BusinessException(String.format(
                    "Se excedería el límite total de %.1f MB para adjuntos",
                    MAX_TOTAL_SIZE / (1024.0 * 1024.0)));
        }
    }

    public void validateExtension(File file) {
        String fileName = file.getName().toLowerCase();
        boolean validExtension = ALLOWED_EXTENSIONS.stream()
                .anyMatch(fileName::endsWith);

        if (!validExtension) {
            throw new BusinessException(
                    "Tipo de archivo no permitido. Extensiones permitidas: " +
                            String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    public void validateMagicBytes(File file) {
        String extension = getExtension(file.getName()).toLowerCase();
        byte[] expectedMagic = MAGIC_BYTES.get(extension);

        if (expectedMagic == null) {
            return;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] fileHeader = new byte[expectedMagic.length];
            int bytesRead = fis.read(fileHeader);

            if (bytesRead < expectedMagic.length) {
                throw new BusinessException("Archivo demasiado pequeño o corrupto");
            }

            for (int i = 0; i < expectedMagic.length; i++) {
                if (fileHeader[i] != expectedMagic[i]) {
                    throw new BusinessException(
                            "El contenido del archivo no coincide con su extensión");
                }
            }
        } catch (IOException e) {
            throw new BusinessException("Error al validar archivo: " + e.getMessage());
        }
    }

    public void validateFileName(String fileName) {
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new BusinessException("Nombre de archivo contiene caracteres no permitidos");
        }

        if (fileName.length() > 255) {
            throw new BusinessException("Nombre de archivo demasiado largo");
        }

        String[] dangerousPatterns = { "<", ">", ":", "\"", "|", "?", "*" };
        for (String pattern : dangerousPatterns) {
            if (fileName.contains(pattern)) {
                throw new BusinessException("Nombre de archivo contiene caracteres no permitidos: " + pattern);
            }
        }
    }

    public String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "unnamed_file";
        }

        String sanitized = fileName.replaceAll("[<>:\"/\\\\|?*]", "_");
        sanitized = sanitized.replaceAll("\\.\\.", "_");
        sanitized = sanitized.replaceAll("\\s+", "_");

        if (sanitized.length() > 200) {
            String extension = getExtension(sanitized);
            String baseName = sanitized.substring(0, 200 - extension.length());
            sanitized = baseName + extension;
        }

        return sanitized;
    }

    private String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : "";
    }

    public boolean isImage(File file) {
        String extension = getExtension(file.getName()).toLowerCase();
        return Arrays.asList(".png", ".jpg", ".jpeg", ".gif").contains(extension);
    }

    public boolean isPdf(File file) {
        return getExtension(file.getName()).toLowerCase().equals(".pdf");
    }

    public boolean isDocument(File file) {
        String extension = getExtension(file.getName()).toLowerCase();
        return Arrays.asList(".doc", ".docx", ".pdf").contains(extension);
    }
}
