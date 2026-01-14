package com.cms.infra;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;
    private static final String DATABASE_PATH = "clinic_db.sqlite";
    private final String url = "jdbc:sqlite:" + DATABASE_PATH;

    private DatabaseConnection() {
        try {
            connection = DriverManager.getConnection(url);
            initializeTables();
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to database", e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reconnecting to database", e);
        }
        return connection;
    }

    public String getDatabasePath() {
        return DATABASE_PATH;
    }

    private void initializeTables() throws SQLException {
        try (var stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS patients (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "cedula TEXT UNIQUE NOT NULL, " +
                    "nombre TEXT NOT NULL, " +
                    "apellido TEXT, " +
                    "fecha_nacimiento TEXT, " +
                    "sexo TEXT, " +
                    "direccion TEXT, " +
                    "telefono TEXT, " +
                    "email TEXT, " +
                    "fecha_registro TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS clinical_histories (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_id INTEGER NOT NULL, " +
                    "fecha_consulta TEXT NOT NULL, " +
                    "motivo_consulta TEXT NOT NULL, " +
                    "antecedentes TEXT, " +
                    "examen_fisico TEXT, " +
                    "diagnostico TEXT, " +
                    "conducta TEXT, " +
                    "observaciones TEXT, " +
                    "medico TEXT, " +
                    "FOREIGN KEY(patient_id) REFERENCES patients(id) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS attachments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "clinical_history_id INTEGER NOT NULL, " +
                    "nombre TEXT NOT NULL, " +
                    "ruta_archivo TEXT NOT NULL, " +
                    "tipo TEXT, " +
                    "tamano_bytes INTEGER, " +
                    "fecha_creacion TEXT DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(clinical_history_id) REFERENCES clinical_histories(id) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT NOT NULL UNIQUE, " +
                    "password_hash TEXT NOT NULL, " +
                    "salt TEXT NOT NULL, " +
                    "role TEXT NOT NULL, " +
                    "full_name TEXT NOT NULL, " +
                    "active INTEGER DEFAULT 1, " +
                    "created_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                    "last_login TEXT, " +
                    "failed_attempts INTEGER DEFAULT 0, " +
                    "locked_until TEXT)");
        }
    }
}
