package com.cms.infra;

import com.cms.domain.Role;
import com.cms.domain.User;

public class SecurityContext {

    private static User currentUser; // Cambio: de ThreadLocal a static simple para evitar NPE

    private SecurityContext() {
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }

    public static boolean isAuthenticated() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }

    public static boolean canManageUsers() {
        return currentUser != null && currentUser.getRole().canManageUsers();
    }

    public static boolean canManageBackups() {
        return currentUser != null && currentUser.getRole().canManageBackups();
    }

    public static Integer getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }

    public static String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }
}