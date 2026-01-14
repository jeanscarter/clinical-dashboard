package com.cms.core.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ValidationResult {

    private final List<String> errors;

    public ValidationResult() {
        this.errors = new ArrayList<>();
    }

    public void addError(String error) {
        errors.add(error);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    public String getErrorsAsString() {
        return String.join("\n", errors);
    }

    public static ValidationResult valid() {
        return new ValidationResult();
    }

    public static ValidationResult invalid(String error) {
        ValidationResult result = new ValidationResult();
        result.addError(error);
        return result;
    }

    public ValidationResult merge(ValidationResult other) {
        this.errors.addAll(other.errors);
        return this;
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty())
            return true;
        return Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", email);
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty())
            return true;
        return Pattern.matches("^[0-9+\\-\\s()]{7,20}$", phone);
    }

    public static boolean isValidCedula(String cedula) {
        if (cedula == null)
            return false;
        return Pattern.matches("^[VEJGvejg]?-?\\d{5,10}$", cedula.trim());
    }
}
