package com.cms.presenter;

import com.cms.domain.ClinicalHistory;
import com.cms.domain.Attachment;

import java.awt.image.BufferedImage;
import java.util.List;

public interface ClinicalHistoryContract {

    interface View {
        void showHistories(List<ClinicalHistory> histories);

        void showHistoryDetails(ClinicalHistory history);

        void showAttachments(List<Attachment> attachments);

        void showError(String message);

        void showSuccess(String message);

        void showLoading(boolean loading);

        void clearForm();

        void showImagePreview(BufferedImage image, String fileName);

        void updateAttachmentsList();
    }

    interface Presenter {
        void loadHistoriesByPatient(Integer patientId);

        void loadAllHistories();

        void saveHistory(ClinicalHistory history);

        void updateHistory(ClinicalHistory history);

        void deleteHistory(Integer historyId);

        void selectHistory(Integer historyId);

        void addAttachmentFromClipboard();

        void addAttachmentFromFile(String filePath);

        void removeAttachment(Integer attachmentId);

        void loadAttachments(Integer historyId);
    }
}
