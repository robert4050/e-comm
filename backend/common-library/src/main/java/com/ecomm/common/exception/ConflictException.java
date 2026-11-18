package com.ecomm.common.exception;

public class ConflictException extends BaseDomainException {

    public ConflictException(String resourceName, String field, String value) {
        super(String.format("%s already exists with %s: '%s'", resourceName, field, value),
                "RESOURCE_CONFLICT", 409);
    }

    public ConflictException(String message) {
        super(message, "RESOURCE_CONFLICT", 409);
    }
}
