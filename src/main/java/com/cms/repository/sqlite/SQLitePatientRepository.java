package com.cms.repository.sqlite;

import com.cms.domain.Patient;
import com.cms.infra.DatabaseConnection;
import com.cms.repository.PatientRepository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLitePatientRepository implements PatientRepository {

    private final DatabaseConnection dbConnection;

    public SQLitePatientRepository(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
        initializeTable();
    }

    private void initializeTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS patients (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    cedula TEXT UNIQUE NOT NULL,
                    nombre TEXT NOT NULL,
                    apellido TEXT,
                    fecha_nacimiento TEXT,
                    sexo TEXT,
                    direccion TEXT,
                    telefono TEXT,
                    email TEXT,
                    fecha_registro TEXT
                )
                """;
        try (Statement stmt = dbConnection.getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing patients table", e);
        }
    }

    @Override
    public Patient save(Patient patient) {
        if (patient.getId() == null) {
            return insert(patient);
        } else {
            return update(patient);
        }
    }

    private Patient insert(Patient patient) {
        String sql = """
                INSERT INTO patients (cedula, nombre, apellido, fecha_nacimiento, sexo, direccion, telefono, email, fecha_registro)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            setPatientParameters(pstmt, patient);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    patient.setId(generatedKeys.getInt(1));
                }
            }
            return patient;
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting patient", e);
        }
    }

    private Patient update(Patient patient) {
        String sql = """
                UPDATE patients SET cedula = ?, nombre = ?, apellido = ?, fecha_nacimiento = ?,
                sexo = ?, direccion = ?, telefono = ?, email = ?, fecha_registro = ?
                WHERE id = ?
                """;
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            setPatientParameters(pstmt, patient);
            pstmt.setInt(10, patient.getId());
            pstmt.executeUpdate();
            return patient;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating patient", e);
        }
    }

    private void setPatientParameters(PreparedStatement pstmt, Patient patient) throws SQLException {
        pstmt.setString(1, patient.getCedula());
        pstmt.setString(2, patient.getNombre());
        pstmt.setString(3, patient.getApellido());
        pstmt.setString(4, patient.getFechaNacimiento() != null ? patient.getFechaNacimiento().toString() : null);
        pstmt.setString(5, patient.getSexo());
        pstmt.setString(6, patient.getDireccion());
        pstmt.setString(7, patient.getTelefono());
        pstmt.setString(8, patient.getEmail());
        pstmt.setString(9, patient.getFechaRegistro() != null ? patient.getFechaRegistro().toString() : null);
    }

    @Override
    public Optional<Patient> findById(Integer id) {
        String sql = "SELECT * FROM patients WHERE id = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding patient by id", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Patient> findByCedula(String cedula) {
        String sql = "SELECT * FROM patients WHERE cedula = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, cedula);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding patient by cedula", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Patient> findAll() {
        String sql = "SELECT * FROM patients ORDER BY apellido, nombre";
        List<Patient> patients = new ArrayList<>();
        try (Statement stmt = dbConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                patients.add(mapResultSetToPatient(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all patients", e);
        }
        return patients;
    }

    @Override
    public List<Patient> findByNombreContaining(String nombre) {
        String sql = "SELECT * FROM patients WHERE nombre LIKE ? ORDER BY apellido, nombre";
        return executeSearchQuery(sql, "%" + nombre + "%");
    }

    @Override
    public List<Patient> findByApellidoContaining(String apellido) {
        String sql = "SELECT * FROM patients WHERE apellido LIKE ? ORDER BY apellido, nombre";
        return executeSearchQuery(sql, "%" + apellido + "%");
    }

    @Override
    public List<Patient> search(String query) {
        String sql = """
                SELECT * FROM patients
                WHERE nombre LIKE ? OR apellido LIKE ? OR cedula LIKE ?
                ORDER BY apellido, nombre
                """;
        List<Patient> patients = new ArrayList<>();
        String searchPattern = "%" + query + "%";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    patients.add(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching patients", e);
        }
        return patients;
    }

    private List<Patient> executeSearchQuery(String sql, String parameter) {
        List<Patient> patients = new ArrayList<>();
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, parameter);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    patients.add(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing search query", e);
        }
        return patients;
    }

    @Override
    public boolean existsByCedula(String cedula) {
        String sql = "SELECT COUNT(*) FROM patients WHERE cedula = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, cedula);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking if patient exists", e);
        }
        return false;
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM patients WHERE id = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting patient", e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM patients";
        try (Statement stmt = dbConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting patients", e);
        }
        return 0;
    }

    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        Patient patient = new Patient();
        patient.setId(rs.getInt("id"));
        patient.setCedula(rs.getString("cedula"));
        patient.setNombre(rs.getString("nombre"));
        patient.setApellido(rs.getString("apellido"));

        String fechaNacimiento = rs.getString("fecha_nacimiento");
        if (fechaNacimiento != null && !fechaNacimiento.isEmpty()) {
            patient.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
        }

        patient.setSexo(rs.getString("sexo"));
        patient.setDireccion(rs.getString("direccion"));
        patient.setTelefono(rs.getString("telefono"));
        patient.setEmail(rs.getString("email"));

        String fechaRegistro = rs.getString("fecha_registro");
        if (fechaRegistro != null && !fechaRegistro.isEmpty()) {
            patient.setFechaRegistro(LocalDate.parse(fechaRegistro));
        }

        return patient;
    }
}
