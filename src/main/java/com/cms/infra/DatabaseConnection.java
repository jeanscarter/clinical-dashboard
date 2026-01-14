package com.cms.infra;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;
    private final String url = "jdbc:sqlite:clinic_db.sqlite";

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

    private void initializeTables() throws SQLException {
        // Basic tables creation could go here or in a separate SchemaManager
        // Keeping it simple for Phase 1 as requested
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

            stmt.execute("CREATE TABLE IF NOT EXISTS clinical_history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_id INTEGER, " +
                    "antecedentes TEXT, " +
                    "conducta TEXT, " +
                    "fecha TEXT, " +
                    "FOREIGN KEY(patient_id) REFERENCES patients(id))");
        }
    }
}
