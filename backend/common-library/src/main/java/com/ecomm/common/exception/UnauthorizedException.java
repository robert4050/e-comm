package com.ecomm.common.exception;

public class UnauthorizedException extends BaseDomainException {

    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED", 401);
    }
}
