package com.cms.presenter;

import com.cms.core.validation.ClinicalHistoryValidator;
import com.cms.core.validation.ValidationResult;
import com.cms.domain.Attachment;
import com.cms.domain.ClinicalHistory;
import com.cms.infra.EventBus;
import com.cms.presenter.events.HistorySavedEvent;
import com.cms.repository.AttachmentRepository;
import com.cms.repository.ClinicalHistoryRepository;
import com.cms.util.ClipboardImageHandler;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class ClinicalHistoryPresenter implements ClinicalHistoryContract.Presenter {

    private final ClinicalHistoryContract.View view;
    private final ClinicalHistoryRepository historyRepository;
    private final AttachmentRepository attachmentRepository;
    private final ClinicalHistoryValidator validator;
    private final ClipboardImageHandler clipboardHandler;
    private final EventBus eventBus;

    private Integer currentHistoryId;

    public ClinicalHistoryPresenter(
            ClinicalHistoryContract.View view,
            ClinicalHistoryRepository historyRepository,
            AttachmentRepository attachmentRepository) {
        this.view = view;
        this.historyRepository = historyRepository;
        this.attachmentRepository = attachmentRepository;
        this.validator = new ClinicalHistoryValidator();
        this.clipboardHandler = new ClipboardImageHandler();
        this.eventBus = EventBus.getInstance();
    }

    @Override
    public void loadHistoriesByPatient(Integer patientId) {
        view.showLoading(true);
        SwingWorker<List<ClinicalHistory>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ClinicalHistory> doInBackground() {
                return historyRepository.findByPatientId(patientId);
            }

            @Override
            protected void done() {
                try {
                    List<ClinicalHistory> histories = get();
                    view.showHistories(histories);
                } catch (Exception e) {
                    view.showError("Error al cargar historias: " + e.getMessage());
                } finally {
                    view.showLoading(false);
                }
            }
        };
        worker.execute();
    }

    @Override
    public void loadAllHistories() {
        view.showLoading(true);
        SwingWorker<List<ClinicalHistory>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ClinicalHistory> doInBackground() {
                return historyRepository.findAll();
            }

            @Override
            protected void done() {
                try {
                    List<ClinicalHistory> histories = get();
                    view.showHistories(histories);
                } catch (Exception e) {
                    view.showError("Error al cargar historias: " + e.getMessage());
                } finally {
                    view.showLoading(false);
                }
            }
        };
        worker.execute();
    }

    @Override
    public void saveHistory(ClinicalHistory history) {
        ValidationResult validation = validator.validate(history);
        if (!validation.isValid()) {
            view.showError(validation.getErrorsAsString());
            return;
        }

        view.showLoading(true);
        SwingWorker<ClinicalHistory, Void> worker = new SwingWorker<>() {
            @Override
            protected ClinicalHistory doInBackground() {
                return historyRepository.save(history);
            }

            @Override
            protected void done() {
                try {
                    ClinicalHistory saved = get();
                    currentHistoryId = saved.getId();
                    view.showSuccess("Historia clínica guardada exitosamente");
                    view.clearForm();
                    eventBus.publish(new HistorySavedEvent(saved));
                    loadAllHistories();
                } catch (Exception e) {
                    view.showError("Error al guardar historia: " + e.getMessage());
                } finally {
                    view.showLoading(false);
                }
            }
        };
        worker.execute();
    }

    @Override
    public void updateHistory(ClinicalHistory history) {
        ValidationResult validation = validator.validateForUpdate(history);
        if (!validation.isValid()) {
            view.showError(validation.getErrorsAsString());
            return;
        }

        view.showLoading(true);
        SwingWorker<ClinicalHistory, Void> worker = new SwingWorker<>() {
            @Override
            protected ClinicalHistory doInBackground() {
                return historyRepository.save(history);
            }

            @Override
            protected void done() {
                try {
                    ClinicalHistory updated = get();
                    view.showSuccess("Historia clínica actualizada exitosamente");
                    eventBus.publish(new HistorySavedEvent(updated));
                    loadAllHistories();
                } catch (Exception e) {
                    view.showError("Error al actualizar historia: " + e.getMessage());
                } finally {
                    view.showLoading(false);
                }
            }
        };
        worker.execute();
    }

    @Override
    public void deleteHistory(Integer historyId) {
        view.showLoading(true);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                attachmentRepository.deleteByClinicalHistoryId(historyId);
                historyRepository.delete(historyId);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    view.showSuccess("Historia clínica eliminada exitosamente");
                    loadAllHistories();
                } catch (Exception e) {
                    view.showError("Error al eliminar historia: " + e.getMessage());
                } finally {
                    view.showLoading(false);
                }
            }
        };
        worker.execute();
    }

    @Override
    public void selectHistory(Integer historyId) {
        this.currentHistoryId = historyId;
        historyRepository.findById(historyId).ifPresentOrElse(
                history -> {
                    view.showHistoryDetails(history);
                    loadAttachments(historyId);
                },
                () -> view.showError("Historia clínica no encontrada"));
    }

    @Override
    public void addAttachmentFromClipboard() {
        if (currentHistoryId == null) {
            view.showError("Debe guardar la historia antes de agregar adjuntos");
            return;
        }

        if (!clipboardHandler.hasImageInClipboard()) {
            view.showError("No hay imagen en el portapapeles");
            return;
        }

        clipboardHandler.getImageFromClipboard().ifPresentOrElse(
                image -> {
                    clipboardHandler.saveImageToFile(image, currentHistoryId).ifPresentOrElse(
                            file -> saveAttachment(file, image),
                            () -> view.showError("Error al guardar imagen del portapapeles"));
                },
                () -> view.showError("No se pudo obtener la imagen del portapapeles"));
    }

    @Override
    public void addAttachmentFromFile(String filePath) {
        if (currentHistoryId == null) {
            view.showError("Debe guardar la historia antes de agregar adjuntos");
            return;
        }

        clipboardHandler.copyFileToAttachments(filePath, currentHistoryId).ifPresentOrElse(
                file -> saveAttachment(file, null),
                () -> view.showError("Error al copiar archivo"));
    }

    private void saveAttachment(File file, BufferedImage previewImage) {
        Attachment attachment = new Attachment();
        attachment.setClinicalHistoryId(currentHistoryId);
        attachment.setNombre(file.getName());
        attachment.setRutaArchivo(file.getAbsolutePath());
        attachment.setTipo(clipboardHandler.getMimeType(file.getAbsolutePath()));
        attachment.setTamanoBytes(clipboardHandler.getFileSize(file.getAbsolutePath()));

        SwingWorker<Attachment, Void> worker = new SwingWorker<>() {
            @Override
            protected Attachment doInBackground() {
                return attachmentRepository.save(attachment);
            }

            @Override
            protected void done() {
                try {
                    Attachment saved = get();
                    view.showSuccess("Adjunto guardado: " + saved.getNombre());
                    if (previewImage != null) {
                        view.showImagePreview(previewImage, saved.getNombre());
                    }
                    view.updateAttachmentsList();
                    loadAttachments(currentHistoryId);
                } catch (Exception e) {
                    view.showError("Error al guardar adjunto: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    @Override
    public void removeAttachment(Integer attachmentId) {
        attachmentRepository.findById(attachmentId).ifPresent(attachment -> {
            clipboardHandler.deleteAttachmentFile(attachment.getRutaArchivo());
            attachmentRepository.delete(attachmentId);
            view.showSuccess("Adjunto eliminado");
            loadAttachments(currentHistoryId);
        });
    }

    @Override
    public void loadAttachments(Integer historyId) {
        if (historyId == null)
            return;

        SwingWorker<List<Attachment>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Attachment> doInBackground() {
                return attachmentRepository.findByClinicalHistoryId(historyId);
            }

            @Override
            protected void done() {
                try {
                    List<Attachment> attachments = get();
                    view.showAttachments(attachments);
                } catch (Exception e) {
                    view.showError("Error al cargar adjuntos: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    public void setCurrentHistoryId(Integer historyId) {
        this.currentHistoryId = historyId;
    }
}
