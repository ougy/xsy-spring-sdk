package com.rkhd.platform.sdk.exception;

public class BatchJobException extends Exception {
    private String message;
    private Throwable cause = this;

    public String getMessage() {
        return this.message;
    }

    public BatchJobException(String message) {
        this.message = message;
    }

    public BatchJobException(Throwable cause) {
        fillInStackTrace();
        this.message = (cause == null) ? null : cause.toString();
        this.cause = cause;
    }

    public BatchJobException(String message, Throwable cause) {
        fillInStackTrace();
        this.message = message;
        this.cause = cause;
    }

    public Throwable getCause() {
        return (this.cause == this) ? null : this.cause;
    }
}