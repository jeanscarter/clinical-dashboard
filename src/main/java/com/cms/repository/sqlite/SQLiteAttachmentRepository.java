package com.cms.repository.sqlite;

import com.cms.domain.Attachment;
import com.cms.infra.DatabaseConnection;
import com.cms.repository.AttachmentRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteAttachmentRepository implements AttachmentRepository {

    private final DatabaseConnection dbConnection;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public SQLiteAttachmentRepository(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
        initializeTable();
    }

    private void initializeTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS attachments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    clinical_history_id INTEGER NOT NULL,
                    nombre TEXT NOT NULL,
                    tipo TEXT,
                    ruta_archivo TEXT NOT NULL,
                    tamano_bytes INTEGER,
                    fecha_carga TEXT,
                    descripcion TEXT,
                    FOREIGN KEY(clinical_history_id) REFERENCES clinical_histories(id) ON DELETE CASCADE
                )
                """;
        try (Statement stmt = dbConnection.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing attachments table", e);
        }
    }

    @Override
    public Attachment save(Attachment attachment) {
        if (attachment.getId() == null) {
            return insert(attachment);
        } else {
            return update(attachment);
        }
    }

    private Attachment insert(Attachment attachment) {
        String sql = """
                INSERT INTO attachments (clinical_history_id, nombre, tipo, ruta_archivo, tamano_bytes, fecha_carga, descripcion)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            setAttachmentParameters(pstmt, attachment);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    attachment.setId(generatedKeys.getInt(1));
                }
            }
            return attachment;
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting attachment", e);
        }
    }

    private Attachment update(Attachment attachment) {
        String sql = """
                UPDATE attachments SET clinical_history_id = ?, nombre = ?, tipo = ?,
                ruta_archivo = ?, tamano_bytes = ?, fecha_carga = ?, descripcion = ?
                WHERE id = ?
                """;
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            setAttachmentParameters(pstmt, attachment);
            pstmt.setInt(8, attachment.getId());
            pstmt.executeUpdate();
            return attachment;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating attachment", e);
        }
    }

    private void setAttachmentParameters(PreparedStatement pstmt, Attachment attachment) throws SQLException {
        pstmt.setInt(1, attachment.getClinicalHistoryId());
        pstmt.setString(2, attachment.getNombre());
        pstmt.setString(3, attachment.getTipo());
        pstmt.setString(4, attachment.getRutaArchivo());
        pstmt.setObject(5, attachment.getTamanoBytes());
        pstmt.setString(6,
                attachment.getFechaCarga() != null ? attachment.getFechaCarga().format(DATETIME_FORMATTER) : null);
        pstmt.setString(7, attachment.getDescripcion());
    }

    @Override
    public Optional<Attachment> findById(Integer id) {
        String sql = "SELECT * FROM attachments WHERE id = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAttachment(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding attachment by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Attachment> findAll() {
        String sql = "SELECT * FROM attachments ORDER BY fecha_carga DESC";
        List<Attachment> attachments = new ArrayList<>();
        try (Statement stmt = dbConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                attachments.add(mapResultSetToAttachment(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all attachments", e);
        }
        return attachments;
    }

    @Override
    public List<Attachment> findByClinicalHistoryId(Integer clinicalHistoryId) {
        String sql = "SELECT * FROM attachments WHERE clinical_history_id = ? ORDER BY fecha_carga DESC";
        List<Attachment> attachments = new ArrayList<>();
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, clinicalHistoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    attachments.add(mapResultSetToAttachment(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding attachments by clinical history id", e);
        }
        return attachments;
    }

    @Override
    public List<Attachment> findByTipo(String tipo) {
        String sql = "SELECT * FROM attachments WHERE tipo = ? ORDER BY fecha_carga DESC";
        List<Attachment> attachments = new ArrayList<>();
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, tipo);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    attachments.add(mapResultSetToAttachment(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding attachments by tipo", e);
        }
        return attachments;
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM attachments WHERE id = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting attachment", e);
        }
    }

    @Override
    public void deleteByClinicalHistoryId(Integer clinicalHistoryId) {
        String sql = "DELETE FROM attachments WHERE clinical_history_id = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, clinicalHistoryId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting attachments by clinical history id", e);
        }
    }

    @Override
    public long countByClinicalHistoryId(Integer clinicalHistoryId) {
        String sql = "SELECT COUNT(*) FROM attachments WHERE clinical_history_id = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, clinicalHistoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting attachments by clinical history", e);
        }
        return 0;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM attachments";
        try (Statement stmt = dbConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting attachments", e);
        }
        return 0;
    }

    private Attachment mapResultSetToAttachment(ResultSet rs) throws SQLException {
        Attachment attachment = new Attachment();
        attachment.setId(rs.getInt("id"));
        attachment.setClinicalHistoryId(rs.getInt("clinical_history_id"));
        attachment.setNombre(rs.getString("nombre"));
        attachment.setTipo(rs.getString("tipo"));
        attachment.setRutaArchivo(rs.getString("ruta_archivo"));

        long tamano = rs.getLong("tamano_bytes");
        if (!rs.wasNull()) {
            attachment.setTamanoBytes(tamano);
        }

        String fechaCarga = rs.getString("fecha_carga");
        if (fechaCarga != null && !fechaCarga.isEmpty()) {
            attachment.setFechaCarga(LocalDateTime.parse(fechaCarga, DATETIME_FORMATTER));
        }

        attachment.setDescripcion(rs.getString("descripcion"));

        return attachment;
    }
}
