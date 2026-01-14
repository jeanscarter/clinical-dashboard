package com.cms.repository.sqlite;

import com.cms.domain.ClinicalHistory;
import com.cms.infra.DatabaseConnection;
import com.cms.repository.ClinicalHistoryRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteClinicalHistoryRepository implements ClinicalHistoryRepository {

    private final DatabaseConnection dbConnection;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public SQLiteClinicalHistoryRepository(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
        initializeTable();
    }

    private void initializeTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS clinical_histories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    patient_id INTEGER NOT NULL,
                    fecha_consulta TEXT NOT NULL,
                    motivo_consulta TEXT,
                    antecedentes TEXT,
                    examen_fisico TEXT,
                    diagnostico TEXT,
                    conducta TEXT,
                    observaciones TEXT,
                    medico TEXT,
                    FOREIGN KEY(patient_id) REFERENCES patients(id) ON DELETE CASCADE
                )
                """;
        try (Statement stmt = dbConnection.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing clinical_histories table", e);
        }
    }

    @Override
    public ClinicalHistory save(ClinicalHistory history) {
        if (history.getId() == null) {
            return insert(history);
        } else {
            return update(history);
        }
    }

    private ClinicalHistory insert(ClinicalHistory history) {
        String sql = """
                INSERT INTO clinical_histories (patient_id, fecha_consulta, motivo_consulta, antecedentes,
                examen_fisico, diagnostico, conducta, observaciones, medico)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            setHistoryParameters(pstmt, history);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    history.setId(generatedKeys.getInt(1));
                }
            }
            return history;
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting clinical history", e);
        }
    }

    private ClinicalHistory update(ClinicalHistory history) {
        String sql = """
                UPDATE clinical_histories SET patient_id = ?, fecha_consulta = ?, motivo_consulta = ?,
                antecedentes = ?, examen_fisico = ?, diagnostico = ?, conducta = ?, observaciones = ?, medico = ?
                WHERE id = ?
                """;
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            setHistoryParameters(pstmt, history);
            pstmt.setInt(10, history.getId());
            pstmt.executeUpdate();
            return history;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating clinical history", e);
        }
    }

    private void setHistoryParameters(PreparedStatement pstmt, ClinicalHistory history) throws SQLException {
        pstmt.setInt(1, history.getPatientId());
        pstmt.setString(2,
                history.getFechaConsulta() != null ? history.getFechaConsulta().format(DATETIME_FORMATTER) : null);
        pstmt.setString(3, history.getMotivoConsulta());
        pstmt.setString(4, history.getAntecedentes());
        pstmt.setString(5, history.getExamenFisico());
        pstmt.setString(6, history.getDiagnostico());
        pstmt.setString(7, history.getConducta());
        pstmt.setString(8, history.getObservaciones());
        pstmt.setString(9, history.getMedico());
    }

    @Override
    public Optional<ClinicalHistory> findById(Integer id) {
        String sql = "SELECT * FROM clinical_histories WHERE id = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToHistory(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding clinical history by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<ClinicalHistory> findAll() {
        String sql = "SELECT * FROM clinical_histories ORDER BY fecha_consulta DESC";
        List<ClinicalHistory> histories = new ArrayList<>();
        try (Statement stmt = dbConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                histories.add(mapResultSetToHistory(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all clinical histories", e);
        }
        return histories;
    }

    @Override
    public List<ClinicalHistory> findByPatientId(Integer patientId) {
        String sql = "SELECT * FROM clinical_histories WHERE patient_id = ? ORDER BY fecha_consulta DESC";
        List<ClinicalHistory> histories = new ArrayList<>();
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    histories.add(mapResultSetToHistory(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding clinical histories by patient id", e);
        }
        return histories;
    }

    @Override
    public List<ClinicalHistory> findByFechaConsultaBetween(LocalDateTime inicio, LocalDateTime fin) {
        String sql = "SELECT * FROM clinical_histories WHERE fecha_consulta BETWEEN ? AND ? ORDER BY fecha_consulta DESC";
        List<ClinicalHistory> histories = new ArrayList<>();
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, inicio.format(DATETIME_FORMATTER));
            pstmt.setString(2, fin.format(DATETIME_FORMATTER));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    histories.add(mapResultSetToHistory(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding clinical histories by date range", e);
        }
        return histories;
    }

    @Override
    public List<ClinicalHistory> findByMedico(String medico) {
        String sql = "SELECT * FROM clinical_histories WHERE medico = ? ORDER BY fecha_consulta DESC";
        List<ClinicalHistory> histories = new ArrayList<>();
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, medico);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    histories.add(mapResultSetToHistory(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding clinical histories by medico", e);
        }
        return histories;
    }

    @Override
    public List<ClinicalHistory> findByDiagnosticoContaining(String diagnostico) {
        String sql = "SELECT * FROM clinical_histories WHERE diagnostico LIKE ? ORDER BY fecha_consulta DESC";
        List<ClinicalHistory> histories = new ArrayList<>();
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, "%" + diagnostico + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    histories.add(mapResultSetToHistory(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding clinical histories by diagnostico", e);
        }
        return histories;
    }

    @Override
    public List<ClinicalHistory> findRecentByPatientId(Integer patientId, int limit) {
        String sql = "SELECT * FROM clinical_histories WHERE patient_id = ? ORDER BY fecha_consulta DESC LIMIT ?";
        List<ClinicalHistory> histories = new ArrayList<>();
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            pstmt.setInt(2, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    histories.add(mapResultSetToHistory(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding recent clinical histories", e);
        }
        return histories;
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM clinical_histories WHERE id = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting clinical history", e);
        }
    }

    @Override
    public long countByPatientId(Integer patientId) {
        String sql = "SELECT COUNT(*) FROM clinical_histories WHERE patient_id = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting clinical histories by patient", e);
        }
        return 0;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM clinical_histories";
        try (Statement stmt = dbConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting clinical histories", e);
        }
        return 0;
    }

    private ClinicalHistory mapResultSetToHistory(ResultSet rs) throws SQLException {
        ClinicalHistory history = new ClinicalHistory();
        history.setId(rs.getInt("id"));
        history.setPatientId(rs.getInt("patient_id"));

        String fechaConsulta = rs.getString("fecha_consulta");
        if (fechaConsulta != null && !fechaConsulta.isEmpty()) {
            history.setFechaConsulta(LocalDateTime.parse(fechaConsulta, DATETIME_FORMATTER));
        }

        history.setMotivoConsulta(rs.getString("motivo_consulta"));
        history.setAntecedentes(rs.getString("antecedentes"));
        history.setExamenFisico(rs.getString("examen_fisico"));
        history.setDiagnostico(rs.getString("diagnostico"));
        history.setConducta(rs.getString("conducta"));
        history.setObservaciones(rs.getString("observaciones"));
        history.setMedico(rs.getString("medico"));

        return history;
    }
}
