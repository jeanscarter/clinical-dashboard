package com.cms.infra;

import com.cms.domain.Role;
import com.cms.domain.User;

public class SecurityContext {

    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

    private SecurityContext() {
    }

    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }

    public static User getCurrentUser() {
        return currentUser.get();
    }

    public static void clear() {
        currentUser.remove();
    }

    public static boolean isAuthenticated() {
        return currentUser.get() != null;
    }

    public static boolean hasRole(Role role) {
        User user = currentUser.get();
        return user != null && user.getRole() == role;
    }

    public static boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    public static boolean isDoctor() {
        return hasRole(Role.DOCTOR);
    }

    public static boolean canManageUsers() {
        User user = currentUser.get();
        return user != null && user.getRole().canManageUsers();
    }

    public static boolean canEditHistories() {
        User user = currentUser.get();
        return user != null && user.getRole().canEditHistories();
    }

    public static boolean canAccessSettings() {
        User user = currentUser.get();
        return user != null && user.getRole().canAccessSettings();
    }

    public static boolean canGenerateReports() {
        User user = currentUser.get();
        return user != null && user.getRole().canGenerateReports();
    }

    public static boolean canManageBackups() {
        User user = currentUser.get();
        return user != null && user.getRole().canManageBackups();
    }

    public static Integer getCurrentUserId() {
        User user = currentUser.get();
        return user != null ? user.getId() : null;
    }

    public static String getCurrentUsername() {
        User user = currentUser.get();
        return user != null ? user.getUsername() : null;
    }
}
