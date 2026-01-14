package com.cms.util;

import com.cms.service.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileValidatorTest {

    private FileValidator validator;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        validator = new FileValidator();
    }

    @Test
    void validateFile_shouldThrow_whenFileIsNull() {
        assertThrows(BusinessException.class, () -> validator.validateFile(null));
    }

    @Test
    void validateFile_shouldThrow_whenFileDoesNotExist() {
        File nonExistent = new File("non_existent_file.png");
        assertThrows(BusinessException.class, () -> validator.validateFile(nonExistent));
    }

    @Test
    void validateExtension_shouldNotThrow_forAllowedExtensions() throws IOException {
        File pngFile = tempDir.resolve("test.png").toFile();
        pngFile.createNewFile();
        assertDoesNotThrow(() -> validator.validateExtension(pngFile));

        File jpgFile = tempDir.resolve("test.jpg").toFile();
        jpgFile.createNewFile();
        assertDoesNotThrow(() -> validator.validateExtension(jpgFile));

        File pdfFile = tempDir.resolve("test.pdf").toFile();
        pdfFile.createNewFile();
        assertDoesNotThrow(() -> validator.validateExtension(pdfFile));
    }

    @Test
    void validateExtension_shouldThrow_forDisallowedExtensions() throws IOException {
        File exeFile = tempDir.resolve("malware.exe").toFile();
        exeFile.createNewFile();

        assertThrows(BusinessException.class, () -> validator.validateExtension(exeFile));
    }

    @Test
    void validateSize_shouldNotThrow_forSmallFile() throws IOException {
        File smallFile = tempDir.resolve("small.txt").toFile();
        try (FileOutputStream fos = new FileOutputStream(smallFile)) {
            fos.write(new byte[1024]); // 1KB
        }

        assertDoesNotThrow(() -> validator.validateSize(smallFile));
    }

    @Test
    void validateFileName_shouldThrow_forPathTraversal() {
        assertThrows(BusinessException.class,
                () -> validator.validateFileName("../../../etc/passwd"));
        assertThrows(BusinessException.class,
                () -> validator.validateFileName("..\\..\\windows\\system32"));
    }

    @Test
    void validateFileName_shouldThrow_forDangerousCharacters() {
        assertThrows(BusinessException.class,
                () -> validator.validateFileName("file<script>.txt"));
        assertThrows(BusinessException.class,
                () -> validator.validateFileName("file|command.txt"));
    }

    @Test
    void sanitizeFileName_shouldRemoveDangerousCharacters() {
        String sanitized = validator.sanitizeFileName("file<>:\"/\\|?*.txt");
        assertFalse(sanitized.contains("<"));
        assertFalse(sanitized.contains(">"));
        assertFalse(sanitized.contains(":"));
        assertFalse(sanitized.contains("\""));
        assertFalse(sanitized.contains("\\"));
        assertFalse(sanitized.contains("|"));
        assertFalse(sanitized.contains("?"));
        assertFalse(sanitized.contains("*"));
    }

    @Test
    void sanitizeFileName_shouldRemovePathTraversal() {
        String sanitized = validator.sanitizeFileName("..file.txt");
        assertFalse(sanitized.contains(".."));
    }

    @Test
    void isImage_shouldReturnTrue_forImageExtensions() throws IOException {
        File pngFile = tempDir.resolve("test.png").toFile();
        pngFile.createNewFile();
        assertTrue(validator.isImage(pngFile));

        File jpgFile = tempDir.resolve("test.jpg").toFile();
        jpgFile.createNewFile();
        assertTrue(validator.isImage(jpgFile));
    }

    @Test
    void isImage_shouldReturnFalse_forNonImageExtensions() throws IOException {
        File pdfFile = tempDir.resolve("test.pdf").toFile();
        pdfFile.createNewFile();
        assertFalse(validator.isImage(pdfFile));
    }

    @Test
    void isPdf_shouldReturnTrue_forPdfExtension() throws IOException {
        File pdfFile = tempDir.resolve("document.pdf").toFile();
        pdfFile.createNewFile();
        assertTrue(validator.isPdf(pdfFile));
    }
}
