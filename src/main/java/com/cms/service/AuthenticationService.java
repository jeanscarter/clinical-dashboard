package com.cms.service;

import com.cms.domain.Role;
import com.cms.domain.User;
import com.cms.infra.AppLogger;
import com.cms.infra.SecurityContext;
import com.cms.repository.UserRepository;
import com.cms.service.exception.BusinessException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

public class AuthenticationService {

    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
        initializeDefaultAdmin();
    }

    public User login(String username, String password) {
        AppLogger.info("Login attempt for user: %s", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario o contraseña incorrectos"));

        if (!user.isActive()) {
            throw new BusinessException("Usuario desactivado");
        }

        if (user.isLocked()) {
            throw new BusinessException("Cuenta bloqueada. Intente nuevamente en unos minutos");
        }

        String hashedPassword = hashPassword(password, user.getSalt());
        if (!hashedPassword.equals(user.getPasswordHash())) {
            user.incrementFailedAttempts();
            userRepository.update(user);
            AppLogger.warn("Failed login attempt for user: " + username);
            throw new BusinessException("Usuario o contraseña incorrectos");
        }

        user.resetFailedAttempts();
        user.setLastLogin(LocalDateTime.now());
        userRepository.update(user);

        SecurityContext.setCurrentUser(user);
        AppLogger.info("User logged in successfully: %s", username);

        return user;
    }

    public void logout() {
        User currentUser = SecurityContext.getCurrentUser();
        if (currentUser != null) {
            AppLogger.info("User logged out: %s", currentUser.getUsername());
        }
        SecurityContext.clear();
    }

    public User getCurrentUser() {
        return SecurityContext.getCurrentUser();
    }

    public boolean hasPermission(String permission) {
        User user = SecurityContext.getCurrentUser();
        if (user == null)
            return false;

        return switch (permission) {
            case "MANAGE_USERS" -> user.getRole().canManageUsers();
            case "EDIT_HISTORIES" -> user.getRole().canEditHistories();
            case "DELETE_HISTORIES" -> user.getRole().canDeleteHistories();
            case "ACCESS_SETTINGS" -> user.getRole().canAccessSettings();
            case "GENERATE_REPORTS" -> user.getRole().canGenerateReports();
            case "MANAGE_BACKUPS" -> user.getRole().canManageBackups();
            default -> false;
        };
    }

    public boolean isAuthenticated() {
        return SecurityContext.isAuthenticated();
    }

    public boolean isUsingDefaultPassword(String username) {
        if (!"admin".equals(username)) {
            return false;
        }
        return userRepository.findByUsername(username)
                .map(user -> hashPassword("admin123", user.getSalt()).equals(user.getPasswordHash()))
                .orElse(false);
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        String oldHash = hashPassword(oldPassword, user.getSalt());
        if (!oldHash.equals(user.getPasswordHash())) {
            throw new BusinessException("Contraseña actual incorrecta");
        }

        if (newPassword.length() < 6) {
            throw new BusinessException("La nueva contraseña debe tener al menos 6 caracteres");
        }

        String newSalt = generateSalt();
        String newHash = hashPassword(newPassword, newSalt);
        user.setSalt(newSalt);
        user.setPasswordHash(newHash);
        userRepository.update(user);

        AppLogger.info("Password changed for user: %s", username);
    }

    private void initializeDefaultAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setFullName("Administrador");
            admin.setRole(Role.ADMIN);
            admin.setActive(true);

            String salt = generateSalt();
            admin.setSalt(salt);
            admin.setPasswordHash(hashPassword("admin123", salt));

            userRepository.save(admin);
            AppLogger.info("Default admin user created");
        }
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedPassword = password + salt;
            byte[] hash = digest.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
