package com.ecomm.common.exception;

public class BadRequestException extends BaseDomainException {

    public BadRequestException(String message) {
        super(message, "BAD_REQUEST", 400);
    }
}
