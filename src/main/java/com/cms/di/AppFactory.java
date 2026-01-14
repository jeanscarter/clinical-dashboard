package com.cms.di;

import com.cms.infra.AppTheme;
import com.cms.infra.DatabaseConnection;
import com.cms.infra.EventBus;
import com.cms.repository.AttachmentRepository;
import com.cms.repository.ClinicalHistoryRepository;
import com.cms.repository.PatientRepository;
import com.cms.repository.sqlite.SQLiteAttachmentRepository;
import com.cms.repository.sqlite.SQLiteClinicalHistoryRepository;
import com.cms.repository.sqlite.SQLitePatientRepository;

public class AppFactory {

    private static AppFactory instance;
    private final DatabaseConnection dbConnection;
    private final EventBus eventBus;
    private final AppTheme appTheme;

    private PatientRepository patientRepository;
    private ClinicalHistoryRepository clinicalHistoryRepository;
    private AttachmentRepository attachmentRepository;

    private AppFactory() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.eventBus = EventBus.getInstance();
        this.appTheme = new AppTheme();
        this.appTheme.init();
    }

    public static synchronized AppFactory getInstance() {
        if (instance == null) {
            instance = new AppFactory();
        }
        return instance;
    }

    public DatabaseConnection getDatabaseConnection() {
        return dbConnection;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public AppTheme getAppTheme() {
        return appTheme;
    }

    public PatientRepository getPatientRepository() {
        if (patientRepository == null) {
            patientRepository = new SQLitePatientRepository(dbConnection);
        }
        return patientRepository;
    }

    public ClinicalHistoryRepository getClinicalHistoryRepository() {
        if (clinicalHistoryRepository == null) {
            clinicalHistoryRepository = new SQLiteClinicalHistoryRepository(dbConnection);
        }
        return clinicalHistoryRepository;
    }

    public AttachmentRepository getAttachmentRepository() {
        if (attachmentRepository == null) {
            attachmentRepository = new SQLiteAttachmentRepository(dbConnection);
        }
        return attachmentRepository;
    }
}
