package com.cms.repository.sqlite;

import com.cms.domain.Role;
import com.cms.domain.User;
import com.cms.infra.DatabaseConnection;
import com.cms.infra.AppLogger;
import com.cms.repository.UserRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteUserRepository implements UserRepository {

    private final DatabaseConnection dbConnection;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SQLiteUserRepository(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public Optional<User> findById(Integer id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            AppLogger.error("Error finding user by id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            AppLogger.error("Error finding user by username: " + username, e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY full_name";
        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            AppLogger.error("Error finding all users", e);
        }
        return users;
    }

    @Override
    public List<User> findActive() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE active = 1 ORDER BY full_name";
        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            AppLogger.error("Error finding active users", e);
        }
        return users;
    }

    @Override
    public User save(User user) {
        if (user.getId() != null) {
            update(user);
            return user;
        }

        String sql = """
                INSERT INTO users (username, password_hash, salt, role, full_name, active, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getSalt());
            stmt.setString(4, user.getRole().name());
            stmt.setString(5, user.getFullName());
            stmt.setInt(6, user.isActive() ? 1 : 0);
            stmt.setString(7, LocalDateTime.now().format(DATE_FORMATTER));

            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                user.setId(keys.getInt(1));
            }
            AppLogger.info("User created: " + user.getUsername());
        } catch (SQLException e) {
            AppLogger.error("Error saving user", e);
        }
        return user;
    }

    @Override
    public void update(User user) {
        String sql = """
                UPDATE users SET username = ?, password_hash = ?, salt = ?, role = ?,
                       full_name = ?, active = ?, last_login = ?, failed_attempts = ?, locked_until = ?
                WHERE id = ?
                """;
        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getSalt());
            stmt.setString(4, user.getRole().name());
            stmt.setString(5, user.getFullName());
            stmt.setInt(6, user.isActive() ? 1 : 0);
            stmt.setString(7, user.getLastLogin() != null ? user.getLastLogin().format(DATE_FORMATTER) : null);
            stmt.setInt(8, user.getFailedAttempts());
            stmt.setString(9, user.getLockedUntil() != null ? user.getLockedUntil().format(DATE_FORMATTER) : null);
            stmt.setInt(10, user.getId());

            stmt.executeUpdate();
            AppLogger.info("User updated: " + user.getUsername());
        } catch (SQLException e) {
            AppLogger.error("Error updating user", e);
        }
    }

    @Override
    public void deactivate(Integer id) {
        String sql = "UPDATE users SET active = 0 WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            AppLogger.info("User deactivated: " + id);
        } catch (SQLException e) {
            AppLogger.error("Error deactivating user", e);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            AppLogger.info("User deleted: " + id);
        } catch (SQLException e) {
            AppLogger.error("Error deleting user", e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            AppLogger.error("Error checking username existence", e);
        }
        return false;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setSalt(rs.getString("salt"));
        user.setRole(Role.valueOf(rs.getString("role")));
        user.setFullName(rs.getString("full_name"));
        user.setActive(rs.getInt("active") == 1);

        String createdAt = rs.getString("created_at");
        if (createdAt != null && !createdAt.isEmpty()) {
            user.setCreatedAt(LocalDateTime.parse(createdAt, DATE_FORMATTER));
        }

        String lastLogin = rs.getString("last_login");
        if (lastLogin != null && !lastLogin.isEmpty()) {
            user.setLastLogin(LocalDateTime.parse(lastLogin, DATE_FORMATTER));
        }

        user.setFailedAttempts(rs.getInt("failed_attempts"));

        String lockedUntil = rs.getString("locked_until");
        if (lockedUntil != null && !lockedUntil.isEmpty()) {
            user.setLockedUntil(LocalDateTime.parse(lockedUntil, DATE_FORMATTER));
        }

        return user;
    }
}
