package com.cms.service;

import com.cms.domain.Role;
import com.cms.domain.User;
import com.cms.infra.AppLogger;
import com.cms.repository.UserRepository;
import com.cms.service.exception.BusinessException;
import com.cms.service.exception.NotFoundException;
import com.cms.service.exception.ValidationException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String username, String password, String fullName, Role role) {
        List<String> errors = new ArrayList<>();

        if (username == null || username.trim().isEmpty()) {
            errors.add("Usuario es obligatorio");
        }
        if (password == null || password.length() < 6) {
            errors.add("La contraseña debe tener al menos 6 caracteres");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            errors.add("Nombre completo es obligatorio");
        }
        if (role == null) {
            errors.add("Rol es obligatorio");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("DUPLICATE_USERNAME", "El nombre de usuario ya existe");
        }

        User user = new User();
        user.setUsername(username.trim().toLowerCase());
        user.setFullName(fullName.trim());
        user.setRole(role);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        String salt = generateSalt();
        user.setSalt(salt);
        user.setPasswordHash(hashPassword(password, salt));

        User saved = userRepository.save(user);
        AppLogger.info("User created: %s", username);
        return saved;
    }

    public User updateUser(Integer id, String fullName, Role role, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario", id));

        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName.trim());
        }
        if (role != null) {
            user.setRole(role);
        }
        user.setActive(active);

        userRepository.update(user);
        AppLogger.info("User updated: %s", user.getUsername());
        return user;
    }

    public void resetPassword(Integer id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario", id));

        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("La contraseña debe tener al menos 6 caracteres");
        }

        String salt = generateSalt();
        user.setSalt(salt);
        user.setPasswordHash(hashPassword(newPassword, salt));
        user.resetFailedAttempts();

        userRepository.update(user);
        AppLogger.info("Password reset for user: %s", user.getUsername());
    }

    public void deactivateUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario", id));

        if (user.getRole() == Role.ADMIN) {
            long adminCount = userRepository.findActive().stream()
                    .filter(u -> u.getRole() == Role.ADMIN)
                    .count();
            if (adminCount <= 1) {
                throw new BusinessException("No se puede desactivar el único administrador");
            }
        }

        userRepository.deactivate(id);
        AppLogger.info("User deactivated: %s", user.getUsername());
    }

    public User getUser(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario", id));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getActiveUsers() {
        return userRepository.findActive();
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
