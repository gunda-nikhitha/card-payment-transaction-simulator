package com.jfs.cpts.exception;

import org.springframework.http.HttpStatus;

/**
 * Base application exception with HTTP status code.
 */
public class AppException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public AppException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public AppException(String message, HttpStatus status) {
        this(message, status, status.name());
    }

    public HttpStatus getStatus() { return status; }
    public String getErrorCode() { return errorCode; }
}
