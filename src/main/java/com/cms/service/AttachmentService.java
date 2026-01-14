package com.cms.service;

import com.cms.domain.Attachment;
import com.cms.repository.AttachmentRepository;
import com.cms.service.exception.BusinessException;
import com.cms.service.exception.NotFoundException;
import com.cms.util.ClipboardImageHandler;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class AttachmentService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_TOTAL_SIZE = 50 * 1024 * 1024; // 50MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".png", ".jpg", ".jpeg", ".gif", ".pdf", ".doc", ".docx");

    private final AttachmentRepository attachmentRepository;
    private final ClipboardImageHandler clipboardHandler;

    public AttachmentService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
        this.clipboardHandler = new ClipboardImageHandler();
    }

    public Attachment addAttachment(Integer historyId, File file, String description) {
        validateFile(file);
        validateTotalSize(historyId, file.length());

        File destFile = clipboardHandler.copyFileToAttachments(
                file.getAbsolutePath(), historyId).orElseThrow(() -> new BusinessException("Error al copiar archivo"));

        Attachment attachment = new Attachment();
        attachment.setClinicalHistoryId(historyId);
        attachment.setNombre(file.getName());
        attachment.setRutaArchivo(destFile.getAbsolutePath());
        attachment.setTipo(clipboardHandler.getMimeType(file.getAbsolutePath()));
        attachment.setTamanoBytes(destFile.length());
        attachment.setFechaCarga(LocalDateTime.now());

        return attachmentRepository.save(attachment);
    }

    public Attachment addAttachmentFromClipboard(Integer historyId) {
        if (!clipboardHandler.hasImageInClipboard()) {
            throw new BusinessException("No hay imagen en el portapapeles");
        }

        BufferedImage image = clipboardHandler.getImageFromClipboard()
                .orElseThrow(() -> new BusinessException("No se pudo obtener la imagen del portapapeles"));

        File savedFile = clipboardHandler.saveImageToFile(image, historyId)
                .orElseThrow(() -> new BusinessException("Error al guardar imagen"));

        validateTotalSize(historyId, savedFile.length());

        Attachment attachment = new Attachment();
        attachment.setClinicalHistoryId(historyId);
        attachment.setNombre(savedFile.getName());
        attachment.setRutaArchivo(savedFile.getAbsolutePath());
        attachment.setTipo("image/png");
        attachment.setTamanoBytes(savedFile.length());
        attachment.setFechaCarga(LocalDateTime.now());

        return attachmentRepository.save(attachment);
    }

    public void deleteAttachment(Integer attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException("Adjunto", attachmentId));

        clipboardHandler.deleteAttachmentFile(attachment.getRutaArchivo());
        attachmentRepository.delete(attachmentId);
    }

    public List<Attachment> getAttachmentsByHistory(Integer historyId) {
        return attachmentRepository.findByClinicalHistoryId(historyId);
    }

    public File getAttachmentFile(Integer attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException("Adjunto", attachmentId));

        File file = new File(attachment.getRutaArchivo());
        if (!file.exists()) {
            throw new NotFoundException("Archivo de adjunto no encontrado");
        }
        return file;
    }

    public BufferedImage getAttachmentImage(Integer attachmentId) {
        File file = getAttachmentFile(attachmentId);
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            throw new BusinessException("Error al leer imagen: " + e.getMessage());
        }
    }

    public void validateFile(File file) {
        if (file == null || !file.exists()) {
            throw new BusinessException("El archivo no existe");
        }

        if (file.length() > MAX_FILE_SIZE) {
            throw new BusinessException("El archivo excede el tamaño máximo de 10MB");
        }

        String fileName = file.getName().toLowerCase();
        boolean validExtension = ALLOWED_EXTENSIONS.stream()
                .anyMatch(fileName::endsWith);

        if (!validExtension) {
            throw new BusinessException("Tipo de archivo no permitido. Extensiones permitidas: " +
                    String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    private void validateTotalSize(Integer historyId, long additionalSize) {
        List<Attachment> existing = attachmentRepository.findByClinicalHistoryId(historyId);
        long totalSize = existing.stream()
                .mapToLong(Attachment::getTamanoBytes)
                .sum();

        if (totalSize + additionalSize > MAX_TOTAL_SIZE) {
            throw new BusinessException("Se ha excedido el límite total de 50MB para adjuntos en esta consulta");
        }
    }
}
