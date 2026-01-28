package com.cms.di;

import com.cms.infra.AppTheme;
import com.cms.infra.DatabaseConnection;
import com.cms.infra.EventBus;
import com.cms.presenter.ClinicalHistoryContract;
import com.cms.presenter.ClinicalHistoryPresenter;
import com.cms.presenter.PatientContract;
import com.cms.presenter.PatientPresenter;
import com.cms.repository.AppointmentRepository;
import com.cms.repository.AttachmentRepository;
import com.cms.repository.ClinicalHistoryRepository;
import com.cms.repository.PatientRepository;
import com.cms.repository.UserRepository;
import com.cms.repository.sqlite.SQLiteAppointmentRepository;
import com.cms.repository.sqlite.SQLiteAttachmentRepository;
import com.cms.repository.sqlite.SQLiteClinicalHistoryRepository;
import com.cms.repository.sqlite.SQLitePatientRepository;
import com.cms.repository.sqlite.SQLiteUserRepository;
import com.cms.service.*;

public class AppFactory {

    private static AppFactory instance;
    private final DatabaseConnection dbConnection;
    private final EventBus eventBus;
    private final AppTheme appTheme;

    // Repositories
    private PatientRepository patientRepository;
    private ClinicalHistoryRepository clinicalHistoryRepository;
    private AttachmentRepository attachmentRepository;
    private UserRepository userRepository;
    private AppointmentRepository appointmentRepository;

    // Services
    private PatientService patientService;
    private ClinicalHistoryService clinicalHistoryService;
    private AttachmentService attachmentService;
    private AuthenticationService authenticationService;
    private UserService userService;
    private StatisticsService statisticsService;
    private ReportService reportService;
    private BackupService backupService;
    private ExportImportService exportImportService;
    private AppointmentService appointmentService;

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

    // Repositories
    public PatientRepository getPatientRepository() {
        if (patientRepository == null) {
            patientRepository = new SQLitePatientRepository(dbConnection);
        }
        return patientRepository;
    }

    public PatientPresenter getPatientPresenter(PatientContract.View view) {
        return new PatientPresenter(view, getPatientRepository());
    }

    public ClinicalHistoryPresenter getClinicalHistoryPresenter(ClinicalHistoryContract.View view) {
        return new ClinicalHistoryPresenter(
                view,
                getClinicalHistoryRepository(),
                getAttachmentRepository());
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

    public UserRepository getUserRepository() {
        if (userRepository == null) {
            userRepository = new SQLiteUserRepository(dbConnection);
        }
        return userRepository;
    }

    public AppointmentRepository getAppointmentRepository() {
        if (appointmentRepository == null) {
            appointmentRepository = new SQLiteAppointmentRepository(dbConnection);
        }
        return appointmentRepository;
    }

    // Services
    public PatientService getPatientService() {
        if (patientService == null) {
            patientService = new PatientService(
                    getPatientRepository(),
                    getClinicalHistoryRepository());
        }
        return patientService;
    }

    public ClinicalHistoryService getClinicalHistoryService() {
        if (clinicalHistoryService == null) {
            clinicalHistoryService = new ClinicalHistoryService(
                    getClinicalHistoryRepository(),
                    getAttachmentRepository(),
                    getPatientRepository());
        }
        return clinicalHistoryService;
    }

    public AttachmentService getAttachmentService() {
        if (attachmentService == null) {
            attachmentService = new AttachmentService(getAttachmentRepository());
        }
        return attachmentService;
    }

    public AuthenticationService getAuthenticationService() {
        if (authenticationService == null) {
            authenticationService = new AuthenticationService(getUserRepository());
        }
        return authenticationService;
    }

    public UserService getUserService() {
        if (userService == null) {
            userService = new UserService(getUserRepository());
        }
        return userService;
    }

    public StatisticsService getStatisticsService() {
        if (statisticsService == null) {
            statisticsService = new StatisticsService(
                    getPatientRepository(),
                    getClinicalHistoryRepository());
        }
        return statisticsService;
    }

    public ReportService getReportService() {
        if (reportService == null) {
            reportService = new ReportService(
                    getPatientRepository(),
                    getClinicalHistoryRepository());
        }
        return reportService;
    }

    public BackupService getBackupService() {
        if (backupService == null) {
            backupService = new BackupService(dbConnection);
        }
        return backupService;
    }

    public ExportImportService getExportImportService() {
        if (exportImportService == null) {
            exportImportService = new ExportImportService(
                    getPatientRepository(),
                    getClinicalHistoryRepository());
        }
        return exportImportService;
    }

    public AppointmentService getAppointmentService() {
        if (appointmentService == null) {
            appointmentService = new AppointmentService(
                    getAppointmentRepository(),
                    getPatientRepository(),
                    getUserRepository());
        }
        return appointmentService;
    }
}
