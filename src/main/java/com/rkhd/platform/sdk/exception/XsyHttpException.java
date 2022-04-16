package com.rkhd.platform.sdk.exception;

public class XsyHttpException extends Exception {
    private Long errorCode;
    private String message;
    private Throwable cause = this;

    public static final long DEFAULT_ERROR_CODE = 100000L;

    public String getMessage() {
        return this.message;
    }

    public XsyHttpException(String message) {
        this.errorCode = Long.valueOf(100000L);
        this.message = "error_code[" + this.errorCode + "]: " + message;
    }

    public XsyHttpException(String message, Long errorCode) {
        this.errorCode = errorCode;
        this.message = "error_code[" + errorCode + "]: " + message;
    }

    public XsyHttpException(Throwable cause) {
        fillInStackTrace();
        this.message = (cause == null) ? null : cause.toString();
        this.errorCode = Long.valueOf(100000L);
        this.message = "error_code[" + this.errorCode + "]: " + this.message;
        this.cause = cause;
    }

    public XsyHttpException(String message, Long errorCode, Throwable cause) {
        fillInStackTrace();
        this.errorCode = errorCode;
        this.message = "error_code[" + errorCode + "]: " + message;
        this.cause = cause;
    }

    public XsyHttpException(String message, Throwable cause) {
        fillInStackTrace();
        this.message = message;
        this.cause = cause;
    }

    public Throwable getCause() {
        return (this.cause == this) ? null : this.cause;
    }
}