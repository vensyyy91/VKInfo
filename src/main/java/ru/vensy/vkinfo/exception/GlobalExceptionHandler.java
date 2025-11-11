package ru.vensy.vkinfo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler
    public AppError handleAuthException(AuthException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);

        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler
    public AppError handleNotFoundException(NotFoundException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);

        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public AppError handleException(Exception ex, WebRequest request) {
        log.error(ex.getMessage(), ex);

        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    private AppError buildError(HttpStatus status, String message, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();

        return AppError.builder()
                .status(status.value())
                .reason(status.getReasonPhrase())
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }
}