package com.rkhd.platform.sdk.exception;

public class LockServiceException extends Exception {
    private String message;
    private Throwable cause = this;

    public String getMessage() {
        return this.message;
    }

    public LockServiceException(String message) {
        this.message = message;
    }

    public LockServiceException(Throwable cause) {
        fillInStackTrace();
        this.message = (cause == null) ? null : cause.toString();
        this.cause = cause;
    }

    public LockServiceException(String message, Throwable cause) {
        fillInStackTrace();
        this.message = message;
        this.cause = cause;
    }

    public Throwable getCause() {
        return (this.cause == this) ? null : this.cause;
    }
}