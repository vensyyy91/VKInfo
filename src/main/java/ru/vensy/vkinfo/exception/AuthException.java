package ru.vensy.vkinfo.exception;

public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}