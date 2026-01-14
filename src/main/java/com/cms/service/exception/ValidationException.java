package com.cms.service.exception;

import java.util.ArrayList;
import java.util.List;

public class ValidationException extends BusinessException {

    private final List<String> errors;

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
        this.errors = new ArrayList<>();
        this.errors.add(message);
    }

    public ValidationException(List<String> errors) {
        super("VALIDATION_ERROR", String.join("; ", errors));
        this.errors = new ArrayList<>(errors);
    }

    public ValidationException(String field, String message) {
        super("VALIDATION_ERROR", field + ": " + message);
        this.errors = new ArrayList<>();
        this.errors.add(field + ": " + message);
    }

    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    public String getErrorsAsString() {
        return String.join("\n", errors);
    }
}
