package com.example.wefly_app.util.exception;

public class IncorrectUserCredentialException extends RuntimeException {
    public IncorrectUserCredentialException(String message) {
        super(message);
    }
}