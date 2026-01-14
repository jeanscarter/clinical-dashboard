package com.cms.service.exception;

public class NotFoundException extends BusinessException {

    private final String entityType;
    private final Object entityId;

    public NotFoundException(String entityType, Object entityId) {
        super("NOT_FOUND", entityType + " con ID " + entityId + " no encontrado");
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public NotFoundException(String message) {
        super("NOT_FOUND", message);
        this.entityType = null;
        this.entityId = null;
    }

    public String getEntityType() {
        return entityType;
    }

    public Object getEntityId() {
        return entityId;
    }
}
