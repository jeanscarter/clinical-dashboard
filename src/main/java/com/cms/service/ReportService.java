package com.cms.service;

import com.cms.domain.ClinicalHistory;
import com.cms.domain.Patient;
import com.cms.infra.AppLogger;
import com.cms.repository.ClinicalHistoryRepository;
import com.cms.repository.PatientRepository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportService {

    private final PatientRepository patientRepository;
    private final ClinicalHistoryRepository historyRepository;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ReportService(PatientRepository patientRepository,
            ClinicalHistoryRepository historyRepository) {
        this.patientRepository = patientRepository;
        this.historyRepository = historyRepository;
    }

    public String generatePatientsListHtml() {
        List<Patient> patients = patientRepository.findAll();
        StringBuilder html = new StringBuilder();

        html.append(getHtmlHeader("Lista de Pacientes"));
        html.append("<h1>Lista de Pacientes Registrados</h1>\n");
        html.append("<p>Generado: ").append(LocalDateTime.now().format(DATE_FORMAT)).append("</p>\n");
        html.append("<p>Total: ").append(patients.size()).append(" pacientes</p>\n");

        html.append("<table>\n");
        html.append("<thead><tr>");
        html.append("<th>Cédula</th><th>Nombre</th><th>Teléfono</th><th>Email</th><th>Fecha Registro</th>");
        html.append("</tr></thead>\n<tbody>\n");

        for (Patient p : patients) {
            html.append("<tr>");
            html.append("<td>").append(escapeHtml(p.getCedula())).append("</td>");
            html.append("<td>").append(escapeHtml(p.getNombreCompleto())).append("</td>");
            html.append("<td>").append(escapeHtml(p.getTelefono())).append("</td>");
            html.append("<td>").append(escapeHtml(p.getEmail())).append("</td>");
            html.append("<td>").append(p.getFechaRegistro() != null ? p.getFechaRegistro().format(DATE_FORMAT) : "")
                    .append("</td>");
            html.append("</tr>\n");
        }

        html.append("</tbody></table>\n");
        html.append(getHtmlFooter());

        return html.toString();
    }

    public String generatePatientHistoryHtml(Integer patientId) {
        Patient patient = patientRepository.findById(patientId).orElse(null);
        if (patient == null)
            return "";

        List<ClinicalHistory> histories = historyRepository.findByPatientId(patientId);
        StringBuilder html = new StringBuilder();

        html.append(getHtmlHeader("Historia Clínica - " + patient.getNombreCompleto()));
        html.append("<h1>Historia Clínica</h1>\n");
        html.append("<div class='patient-info'>\n");
        html.append("<h2>").append(escapeHtml(patient.getNombreCompleto())).append("</h2>\n");
        html.append("<p><strong>Cédula:</strong> ").append(escapeHtml(patient.getCedula())).append("</p>\n");
        html.append("<p><strong>Teléfono:</strong> ").append(escapeHtml(patient.getTelefono())).append("</p>\n");
        html.append("<p><strong>Email:</strong> ").append(escapeHtml(patient.getEmail())).append("</p>\n");
        html.append("</div>\n");

        html.append("<h2>Consultas (").append(histories.size()).append(")</h2>\n");

        for (ClinicalHistory h : histories) {
            html.append("<div class='consultation'>\n");
            html.append("<h3>Consulta del ").append(
                    h.getFechaConsulta() != null ? h.getFechaConsulta().format(DATE_FORMAT) : "Fecha desconocida")
                    .append("</h3>\n");

            appendField(html, "Motivo", h.getMotivoConsulta());
            appendField(html, "Antecedentes", h.getAntecedentes());
            appendField(html, "Examen Físico", h.getExamenFisico());
            appendField(html, "Diagnóstico", h.getDiagnostico());
            appendField(html, "Conducta", h.getConducta());
            appendField(html, "Observaciones", h.getObservaciones());
            appendField(html, "Médico", h.getMedico());

            html.append("</div>\n");
        }

        html.append(getHtmlFooter());
        return html.toString();
    }

    public String generateConsultationsCsv(LocalDateTime from, LocalDateTime to) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Paciente,Fecha,Motivo,Diagnóstico,Médico\n");

        List<ClinicalHistory> histories = historyRepository.findAll().stream()
                .filter(h -> isInRange(h.getFechaConsulta(), from, to))
                .toList();

        for (ClinicalHistory h : histories) {
            String patientName = patientRepository.findById(h.getPatientId())
                    .map(Patient::getNombreCompleto)
                    .orElse("Desconocido");

            csv.append(h.getId()).append(",");
            csv.append(escapeCsv(patientName)).append(",");
            csv.append(h.getFechaConsulta() != null ? h.getFechaConsulta().format(DATE_FORMAT) : "").append(",");
            csv.append(escapeCsv(h.getMotivoConsulta())).append(",");
            csv.append(escapeCsv(h.getDiagnostico())).append(",");
            csv.append(escapeCsv(h.getMedico())).append("\n");
        }

        return csv.toString();
    }

    public void exportToFile(String content, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(content);
        }
        AppLogger.info("Report exported to: " + file.getAbsolutePath());
    }

    private void appendField(StringBuilder html, String label, String value) {
        if (value != null && !value.isEmpty()) {
            html.append("<p><strong>").append(label).append(":</strong> ");
            html.append(escapeHtml(value)).append("</p>\n");
        }
    }

    private String getHtmlHeader(String title) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>%s</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; line-height: 1.6; }
                        h1 { color: #1e3a5f; border-bottom: 2px solid #3b82f6; padding-bottom: 10px; }
                        h2 { color: #2d4a6f; margin-top: 30px; }
                        h3 { color: #3b82f6; }
                        table { width: 100%%; border-collapse: collapse; margin: 20px 0; }
                        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
                        th { background-color: #3b82f6; color: white; }
                        tr:nth-child(even) { background-color: #f9fafb; }
                        .patient-info { background: #f0f9ff; padding: 15px; border-radius: 8px; margin: 20px 0; }
                        .consultation { background: #fafafa; padding: 15px; border-left: 4px solid #3b82f6; margin: 15px 0; }
                        @media print { body { margin: 0; } }
                    </style>
                </head>
                <body>
                """
                .formatted(title);
    }

    private String getHtmlFooter() {
        return """
                <footer style="margin-top: 40px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; font-size: 12px;">
                    <p>Clinical Management System - Reporte generado automáticamente</p>
                </footer>
                </body>
                </html>
                """;
    }

    private String escapeHtml(String text) {
        if (text == null)
            return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String escapeCsv(String text) {
        if (text == null)
            return "";
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private boolean isInRange(LocalDateTime date, LocalDateTime from, LocalDateTime to) {
        if (date == null)
            return false;
        if (from != null && date.isBefore(from))
            return false;
        if (to != null && date.isAfter(to))
            return false;
        return true;
    }
}
