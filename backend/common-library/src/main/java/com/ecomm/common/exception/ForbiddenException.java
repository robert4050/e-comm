package com.ecomm.common.exception;

public class ForbiddenException extends BaseDomainException {

    public ForbiddenException(String message) {
        super(message, "FORBIDDEN", 403);
    }
}
