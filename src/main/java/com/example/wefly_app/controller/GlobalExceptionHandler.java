package com.example.wefly_app.controller;

import com.example.wefly_app.util.TemplateResponse;
import com.example.wefly_app.util.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @Autowired
    private TemplateResponse templateResponse;
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SpelEvaluationException.class)
    public ResponseEntity<Map<Object, Object>> handleSpelEvaluationException(SpelEvaluationException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Unauthorized User");
        errors.put("cause", ex.getMessage());
        return new ResponseEntity<>(templateResponse.error(errors), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<Object, Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        Throwable mostSpecificCause = ex.getMostSpecificCause();
        if (mostSpecificCause instanceof DateTimeParseException) {
            String errorMessage = "JSON parse error: Invalid date format, expected format dd-MM-yyyy.";
            return new ResponseEntity<>(templateResponse.error(errorMessage, 400), HttpStatus.BAD_REQUEST);
        }
        // Generic error message for other types of HttpMessageNotReadableException
        return new ResponseEntity<>(templateResponse.error("JSON parse error: Invalid request body."), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserDisabledException.class)
    public ResponseEntity<Object> handleUserDisabledException(UserDisabledException ex) {
        return new ResponseEntity<>(templateResponse.error(ex.getMessage(), 403), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IncorrectUserCredentialException.class)
    public ResponseEntity<Object> handleIncorrectUserCredential(IncorrectUserCredentialException ex) {
        return new ResponseEntity<>(templateResponse.error(ex.getMessage(), 401), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(SpringTokenServerException.class)
    public ResponseEntity<Object> handleSpringTokenServerException(SpringTokenServerException ex) {
        return new ResponseEntity<>(templateResponse.error(ex.getMessage(), 500), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException(ValidationException ex) {
        return new ResponseEntity<>(templateResponse.error(ex.getMessage(), 400), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<Object> handleEntityExistsException(EntityExistsException ex) {
        return new ResponseEntity<>(templateResponse.error(ex.getMessage(), 409), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException ex) {
        return new ResponseEntity<>(templateResponse.error(ex.getMessage(), 404), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FileHandlingException.class)
    public ResponseEntity<Object> handleFileHandlingException(FileHandlingException ex) {
        return new ResponseEntity<>(templateResponse.error(ex.getMessage(), 404), HttpStatus.NOT_FOUND);
    }

}
