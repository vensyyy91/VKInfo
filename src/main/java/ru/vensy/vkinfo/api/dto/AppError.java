package ru.vensy.vkinfo.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class AppError {
    private final int status;
    private final String reason;
    private final String message;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;
    private final String path;

    private AppError(Builder builder) {
        this.status = builder.status;
        this.reason = builder.reason;
        this.message = builder.message;
        this.timestamp = builder.timestamp;
        this.path = builder.path;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int status;
        private String reason;
        private String message;
        private LocalDateTime timestamp;
        private String path;

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public AppError build() {
            return new AppError(this);
        }
    }

    public int getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getPath() {
        return path;
    }
}