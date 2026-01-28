package com.cms.repository.sqlite;

import com.cms.domain.Appointment;
import com.cms.domain.AppointmentStatus;
import com.cms.infra.DatabaseConnection;
import com.cms.repository.AppointmentRepository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite implementation of AppointmentRepository.
 */
public class SQLiteAppointmentRepository implements AppointmentRepository {

    private final DatabaseConnection dbConnection;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public SQLiteAppointmentRepository(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
        initializeTable();
    }

    private void initializeTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS appointments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    patient_id INTEGER NOT NULL,
                    doctor_id INTEGER,
                    date TEXT NOT NULL,
                    time TEXT NOT NULL,
                    reason TEXT,
                    status TEXT DEFAULT 'PENDING',
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY(patient_id) REFERENCES patients(id) ON DELETE CASCADE,
                    FOREIGN KEY(doctor_id) REFERENCES users(id)
                )
                """;
        try (Statement stmt = dbConnection.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing appointments table", e);
        }
    }

    @Override
    public Appointment save(Appointment appointment) {
        if (appointment.getId() == null) {
            return insert(appointment);
        } else {
            return update(appointment);
        }
    }

    private Appointment insert(Appointment appointment) {
        String sql = """
                INSERT INTO appointments (patient_id, doctor_id, date, time, reason, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            setAppointmentParameters(pstmt, appointment);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    appointment.setId(generatedKeys.getInt(1));
                }
            }
            return appointment;
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting appointment", e);
        }
    }

    private Appointment update(Appointment appointment) {
        String sql = """
                UPDATE appointments SET patient_id = ?, doctor_id = ?, date = ?, time = ?,
                reason = ?, status = ?, created_at = ?
                WHERE id = ?
                """;
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            setAppointmentParameters(pstmt, appointment);
            pstmt.setInt(8, appointment.getId());
            pstmt.executeUpdate();
            return appointment;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating appointment", e);
        }
    }

    private void setAppointmentParameters(PreparedStatement pstmt, Appointment appointment) throws SQLException {
        pstmt.setInt(1, appointment.getPatientId());
        if (appointment.getDoctorId() != null) {
            pstmt.setInt(2, appointment.getDoctorId());
        } else {
            pstmt.setNull(2, Types.INTEGER);
        }
        pstmt.setString(3, appointment.getDate() != null ? appointment.getDate().format(DATE_FORMATTER) : null);
        pstmt.setString(4, appointment.getTime() != null ? appointment.getTime().format(TIME_FORMATTER) : null);
        pstmt.setString(5, appointment.getReason());
        pstmt.setString(6,
                appointment.getStatus() != null ? appointment.getStatus().name() : AppointmentStatus.PENDING.name());
        pstmt.setString(7, appointment.getCreatedAt() != null ? appointment.getCreatedAt().format(DATETIME_FORMATTER)
                : LocalDateTime.now().format(DATETIME_FORMATTER));
    }

    @Override
    public Optional<Appointment> findById(Integer id) {
        String sql = "SELECT * FROM appointments WHERE id = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding appointment by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Appointment> findAll() {
        String sql = "SELECT * FROM appointments ORDER BY date DESC, time DESC";
        List<Appointment> appointments = new ArrayList<>();
        try (Statement stmt = dbConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all appointments", e);
        }
        return appointments;
    }

    @Override
    public List<Appointment> findByDate(LocalDate date) {
        String sql = "SELECT * FROM appointments WHERE date = ? ORDER BY time ASC";
        List<Appointment> appointments = new ArrayList<>();
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, date.format(DATE_FORMATTER));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding appointments by date", e);
        }
        return appointments;
    }

    @Override
    public List<Appointment> findByPatientId(Integer patientId) {
        String sql = "SELECT * FROM appointments WHERE patient_id = ? ORDER BY date DESC, time DESC";
        List<Appointment> appointments = new ArrayList<>();
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding appointments by patient id", e);
        }
        return appointments;
    }

    @Override
    public List<Appointment> findByStatus(AppointmentStatus status) {
        String sql = "SELECT * FROM appointments WHERE status = ? ORDER BY date ASC, time ASC";
        List<Appointment> appointments = new ArrayList<>();
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding appointments by status", e);
        }
        return appointments;
    }

    @Override
    public List<Appointment> findTodayAppointments() {
        return findByDate(LocalDate.now());
    }

    @Override
    public List<Appointment> findByDateBetween(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM appointments WHERE date BETWEEN ? AND ? ORDER BY date ASC, time ASC";
        List<Appointment> appointments = new ArrayList<>();
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, startDate.format(DATE_FORMATTER));
            pstmt.setString(2, endDate.format(DATE_FORMATTER));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding appointments by date range", e);
        }
        return appointments;
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM appointments WHERE id = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting appointment", e);
        }
    }

    @Override
    public long countByDate(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE date = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, date.format(DATE_FORMATTER));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting appointments by date", e);
        }
        return 0;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM appointments";
        try (Statement stmt = dbConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting appointments", e);
        }
        return 0;
    }

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        Appointment appointment = new Appointment();
        appointment.setId(rs.getInt("id"));
        appointment.setPatientId(rs.getInt("patient_id"));

        int doctorId = rs.getInt("doctor_id");
        if (!rs.wasNull()) {
            appointment.setDoctorId(doctorId);
        }

        String dateStr = rs.getString("date");
        if (dateStr != null && !dateStr.isEmpty()) {
            appointment.setDate(LocalDate.parse(dateStr, DATE_FORMATTER));
        }

        String timeStr = rs.getString("time");
        if (timeStr != null && !timeStr.isEmpty()) {
            appointment.setTime(LocalTime.parse(timeStr, TIME_FORMATTER));
        }

        appointment.setReason(rs.getString("reason"));
        appointment.setStatus(AppointmentStatus.fromString(rs.getString("status")));

        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null && !createdAtStr.isEmpty()) {
            try {
                appointment.setCreatedAt(LocalDateTime.parse(createdAtStr, DATETIME_FORMATTER));
            } catch (Exception e) {
                // Handle alternative date format from SQLite default
                appointment.setCreatedAt(LocalDateTime.now());
            }
        }

        return appointment;
    }
}
