package ru.vensy.vkinfo.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import ru.vensy.vkinfo.api.dto.AppError;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class,
            MissingServletRequestParameterException.class,
            MissingRequestHeaderException.class})
    public AppError handleBadRequestException(Exception ex, WebRequest request) {
        log.error("Incorrectly made request", ex);

        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public AppError handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        StringJoiner joiner = new StringJoiner("; ");
        StringBuilder builder = new StringBuilder();
        List<ObjectError> allErrors = ex.getAllErrors();
        for (int i = 0; i < allErrors.size(); i++) {
            builder.append(i + 1).append(") ");
            ObjectError error = allErrors.get(i);
            if (error instanceof FieldError) {
                String field = ((FieldError) error).getField();
                builder.append("Неверно заполнено поле ").append(field).append(": ");
            }
            builder.append(error.getDefaultMessage());

            joiner.add(builder);
            builder.setLength(0);
        }
        String message = "Ошибка валидации: " + joiner;
        log.error(message, ex);

        return buildError(HttpStatus.BAD_REQUEST, message, request);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public AppError handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        StringJoiner joiner = new StringJoiner("; ");
        StringBuilder builder = new StringBuilder();
        Set<ConstraintViolation<?>> constraintViolations = ex.getConstraintViolations();
        for (ConstraintViolation<?> constraintViolation : constraintViolations) {
            ConstraintDescriptorImpl<?> constraintDescriptor = (ConstraintDescriptorImpl<?>) constraintViolation.getConstraintDescriptor();
            switch (constraintDescriptor.getConstraintLocationKind()) {
                case FIELD -> {
                    builder.append("Неверно заполнено поле ");
                    String[] parts = constraintViolation.getPropertyPath().toString().split("\\.");
                    String fieldName = parts[parts.length - 1];
                    builder.append(fieldName).append(": ").append(constraintViolation.getMessage());
                }
                case PARAMETER -> {
                    builder.append("Неверно указан параметр ");
                    String[] parts = constraintViolation.getPropertyPath().toString().split("\\.");
                    String fieldName = parts[parts.length - 1];
                    builder.append(fieldName).append(": ").append(constraintViolation.getMessage());
                }
                default -> builder.append(constraintViolation.getMessage());
            }
            joiner.add(builder);
            builder.setLength(0);
        }
        String message = "Ошибка валидации: " + joiner;
        log.error(message, ex);

        return buildError(HttpStatus.BAD_REQUEST, message, request);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler
    public AppError handleAuthException(AuthException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);

        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler
    public AppError handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        String message = "Неверный логин или пароль";
        log.error(message, ex);

        return buildError(HttpStatus.UNAUTHORIZED, message, request);
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

    @ExceptionHandler
    public ResponseEntity<AppError> handleVkApiException(VkApiException ex, WebRequest request) {
        String message = "Ошибка VK API " + ex.getErrorCode() + ": " + ex.getMessage();
        log.error(message, ex);

        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(buildError(ex.getHttpStatus(), message, request));
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