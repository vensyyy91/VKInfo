package ru.vensy.vkinfo.exception;

import org.springframework.http.HttpStatus;

public class VkApiException extends RuntimeException {
    private final int errorCode;
    private final HttpStatus httpStatus;

    public VkApiException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = mapHttpStatus(errorCode);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    private HttpStatus mapHttpStatus(int errorCode) {
        return switch (errorCode) {
            case 5 -> HttpStatus.UNAUTHORIZED;
            case 6 -> HttpStatus.TOO_MANY_REQUESTS;
            case 15 -> HttpStatus.FORBIDDEN;
            case 18 -> HttpStatus.GONE;
            case 100 -> HttpStatus.BAD_REQUEST;
            case 113 -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_GATEWAY;
        };
    }
}