package com.cms.infra;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DatabaseMigration {

    private final DatabaseConnection dbConnection;
    private static final String MIGRATIONS_PATH = "/db/migration/";

    public DatabaseMigration(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public void runMigrations() {
        AppLogger.info("Starting database migrations...");

        try {
            createSchemaVersionTable();

            List<String> pendingMigrations = getPendingMigrations();

            if (pendingMigrations.isEmpty()) {
                AppLogger.info("No pending migrations found.");
                return;
            }

            for (String migrationFile : pendingMigrations) {
                runMigration(migrationFile);
            }

            AppLogger.info("All migrations completed successfully.");
        } catch (Exception e) {
            AppLogger.error("Migration failed", e);
            throw new RuntimeException("Database migration failed: " + e.getMessage(), e);
        }
    }

    private void createSchemaVersionTable() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS schema_version (
                    version TEXT PRIMARY KEY,
                    description TEXT,
                    executed_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    success INTEGER DEFAULT 1
                )
                """;

        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private List<String> getPendingMigrations() throws SQLException {
        List<String> allMigrations = getAvailableMigrations();
        List<String> executedMigrations = getExecutedMigrations();

        return allMigrations.stream()
                .filter(m -> !executedMigrations.contains(extractVersion(m)))
                .sorted()
                .collect(Collectors.toList());
    }

    private List<String> getAvailableMigrations() {
        List<String> migrations = new ArrayList<>();

        String[] migrationFiles = {
                "V1__initial_schema.sql",
                "V2__add_indexes.sql",
                "V3__add_audit_table.sql",
                "V4__add_users_and_auth.sql"
        };

        for (String file : migrationFiles) {
            if (getClass().getResourceAsStream(MIGRATIONS_PATH + file) != null) {
                migrations.add(file);
            }
        }

        return migrations;
    }

    private List<String> getExecutedMigrations() throws SQLException {
        List<String> executed = new ArrayList<>();
        String sql = "SELECT version FROM schema_version WHERE success = 1";

        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                executed.add(rs.getString("version"));
            }
        }

        return executed;
    }

    private void runMigration(String migrationFile) throws SQLException {
        String version = extractVersion(migrationFile);
        String description = extractDescription(migrationFile);

        AppLogger.info("Running migration: %s - %s", version, description);

        String sql = loadMigrationSql(migrationFile);

        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement()) {
                String[] statements = sql.split(";");
                for (String statement : statements) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                        stmt.execute(trimmed);
                    }
                }

                recordMigration(conn, version, description, true);
                conn.commit();

                AppLogger.info("Migration %s completed successfully.", version);
            } catch (SQLException e) {
                conn.rollback();
                recordMigration(conn, version, description, false);
                throw e;
            }
        }
    }

    private String loadMigrationSql(String migrationFile) {
        try (InputStream is = getClass().getResourceAsStream(MIGRATIONS_PATH + migrationFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load migration file: " + migrationFile, e);
        }
    }

    private void recordMigration(Connection conn, String version, String description, boolean success)
            throws SQLException {
        String sql = """
                INSERT OR REPLACE INTO schema_version (version, description, executed_at, success)
                VALUES (?, ?, datetime('now'), ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, version);
            stmt.setString(2, description);
            stmt.setInt(3, success ? 1 : 0);
            stmt.executeUpdate();
        }
    }

    private String extractVersion(String fileName) {
        int underscoreIndex = fileName.indexOf("__");
        if (underscoreIndex > 0) {
            return fileName.substring(0, underscoreIndex);
        }
        return fileName;
    }

    private String extractDescription(String fileName) {
        int underscoreIndex = fileName.indexOf("__");
        if (underscoreIndex > 0 && fileName.endsWith(".sql")) {
            return fileName.substring(underscoreIndex + 2, fileName.length() - 4)
                    .replace("_", " ");
        }
        return fileName;
    }
}
