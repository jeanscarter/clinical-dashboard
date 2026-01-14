package com.cms.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

public class ClipboardImageHandler {

    private static final String ATTACHMENTS_DIR = "attachments";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public Optional<BufferedImage> getImageFromClipboard() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable transferable = clipboard.getContents(null);

            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                Image image = (Image) transferable.getTransferData(DataFlavor.imageFlavor);
                return Optional.of(toBufferedImage(image));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean hasImageInClipboard() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable transferable = clipboard.getContents(null);
            return transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor);
        } catch (Exception e) {
            return false;
        }
    }

    public Optional<File> saveImageToFile(BufferedImage image, Integer historyId) {
        try {
            Path attachmentsPath = Paths.get(ATTACHMENTS_DIR, String.valueOf(historyId));
            Files.createDirectories(attachmentsPath);

            String fileName = "clipboard_" + LocalDateTime.now().format(DATE_FORMAT) + "_" +
                    UUID.randomUUID().toString().substring(0, 8) + ".png";
            File outputFile = attachmentsPath.resolve(fileName).toFile();

            ImageIO.write(image, "PNG", outputFile);
            return Optional.of(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<File> copyFileToAttachments(String sourcePath, Integer historyId) {
        try {
            Path source = Paths.get(sourcePath);
            if (!Files.exists(source)) {
                return Optional.empty();
            }

            Path attachmentsPath = Paths.get(ATTACHMENTS_DIR, String.valueOf(historyId));
            Files.createDirectories(attachmentsPath);

            String originalName = source.getFileName().toString();
            String extension = getExtension(originalName);
            String newName = originalName.replace(extension, "") + "_" +
                    UUID.randomUUID().toString().substring(0, 8) + extension;

            Path destination = attachmentsPath.resolve(newName);
            Files.copy(source, destination);

            return Optional.of(destination.toFile());
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public boolean deleteAttachmentFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public long getFileSize(String filePath) {
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            return 0;
        }
    }

    public String getMimeType(String filePath) {
        String extension = getExtension(filePath).toLowerCase();
        return switch (extension) {
            case ".png" -> "image/png";
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".gif" -> "image/gif";
            case ".bmp" -> "image/bmp";
            case ".pdf" -> "application/pdf";
            case ".doc" -> "application/msword";
            case ".docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default -> "application/octet-stream";
        };
    }

    private BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        BufferedImage bufferedImage = new BufferedImage(
                img.getWidth(null),
                img.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return bufferedImage;
    }

    private String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : "";
    }
}
