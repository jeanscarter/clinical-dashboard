package com.cms.service;

import com.cms.domain.ClinicalHistory;
import com.cms.domain.Patient;
import com.cms.infra.AppLogger;
import com.cms.repository.ClinicalHistoryRepository;
import com.cms.repository.PatientRepository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ExportImportService {

    private final PatientRepository patientRepository;
    private final ClinicalHistoryRepository historyRepository;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ExportImportService(PatientRepository patientRepository,
            ClinicalHistoryRepository historyRepository) {
        this.patientRepository = patientRepository;
        this.historyRepository = historyRepository;
    }

    public void exportPatientsToCsv(File file) throws IOException {
        List<Patient> patients = patientRepository.findAll();

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            writer.write('\ufeff'); // BOM for Excel UTF-8
            writer.write("ID,Cédula,Nombre,Apellido,Teléfono,Email,Dirección,Sexo,Fecha Nacimiento,Fecha Registro\n");

            for (Patient p : patients) {
                writer.write(String.join(",",
                        String.valueOf(p.getId()),
                        escapeCsv(p.getCedula()),
                        escapeCsv(p.getNombre()),
                        escapeCsv(p.getApellido()),
                        escapeCsv(p.getTelefono()),
                        escapeCsv(p.getEmail()),
                        escapeCsv(p.getDireccion()),
                        escapeCsv(p.getSexo()),
                        p.getFechaNacimiento() != null ? p.getFechaNacimiento().format(DATE_FORMAT) : "",
                        p.getFechaRegistro() != null ? p.getFechaRegistro().format(DATE_FORMAT) : ""));
                writer.write("\n");
            }
        }

        AppLogger.info("Exported %d patients to %s", patients.size(), file.getName());
    }

    public void exportHistoriesToCsv(File file) throws IOException {
        List<ClinicalHistory> histories = historyRepository.findAll();

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            writer.write('\ufeff');
            writer.write(
                    "ID,ID Paciente,Fecha Consulta,Motivo,Antecedentes,Examen Físico,Diagnóstico,Conducta,Observaciones,Médico\n");

            for (ClinicalHistory h : histories) {
                writer.write(String.join(",",
                        String.valueOf(h.getId()),
                        String.valueOf(h.getPatientId()),
                        h.getFechaConsulta() != null ? h.getFechaConsulta().format(DATE_FORMAT) : "",
                        escapeCsv(h.getMotivoConsulta()),
                        escapeCsv(h.getAntecedentes()),
                        escapeCsv(h.getExamenFisico()),
                        escapeCsv(h.getDiagnostico()),
                        escapeCsv(h.getConducta()),
                        escapeCsv(h.getObservaciones()),
                        escapeCsv(h.getMedico())));
                writer.write("\n");
            }
        }

        AppLogger.info("Exported %d histories to %s", histories.size(), file.getName());
    }

    public ImportResult importPatientsFromCsv(File file) throws IOException {
        ImportResult result = new ImportResult();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String header = reader.readLine();
            if (header != null && header.startsWith("\ufeff")) {
                header = header.substring(1);
            }

            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                try {
                    String[] fields = parseCsvLine(line);
                    if (fields.length < 3) {
                        result.addError(lineNum, "Campos insuficientes");
                        continue;
                    }

                    Patient patient = new Patient();
                    patient.setCedula(fields.length > 1 ? fields[1] : "");
                    patient.setNombre(fields.length > 2 ? fields[2] : "");
                    patient.setApellido(fields.length > 3 ? fields[3] : "");
                    patient.setTelefono(fields.length > 4 ? fields[4] : "");
                    patient.setEmail(fields.length > 5 ? fields[5] : "");
                    patient.setDireccion(fields.length > 6 ? fields[6] : "");
                    patient.setSexo(fields.length > 7 ? fields[7] : "");
                    patient.setFechaRegistro(LocalDate.now());

                    if (patient.getCedula().isEmpty() || patient.getNombre().isEmpty()) {
                        result.addError(lineNum, "Cédula y Nombre son obligatorios");
                        continue;
                    }

                    patientRepository.save(patient);
                    result.incrementSuccess();

                } catch (Exception e) {
                    result.addError(lineNum, e.getMessage());
                }
            }
        }

        AppLogger.info("Import completed: %d success, %d errors", result.getSuccessCount(), result.getErrorCount());
        return result;
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().trim());

        return fields.toArray(new String[0]);
    }

    private String escapeCsv(String text) {
        if (text == null)
            return "";
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    public static class ImportResult {
        private int successCount = 0;
        private final List<String> errors = new ArrayList<>();

        public void incrementSuccess() {
            successCount++;
        }

        public void addError(int line, String message) {
            errors.add("Línea " + line + ": " + message);
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getErrorCount() {
            return errors.size();
        }

        public List<String> getErrors() {
            return errors;
        }

        public String getSummary() {
            return String.format("Importados: %d, Errores: %d", successCount, errors.size());
        }
    }
}
