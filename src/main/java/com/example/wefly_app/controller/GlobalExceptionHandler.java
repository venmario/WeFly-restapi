package com.example.wefly_app.controller;

import com.example.wefly_app.util.TemplateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<Object, Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        Throwable mostSpecificCause = ex.getMostSpecificCause();
        if (mostSpecificCause instanceof DateTimeParseException) {
            String errorMessage = "JSON parse error: Invalid date format, expected format dd-MM-yyyy.";
            return new ResponseEntity<>(templateResponse.error(errorMessage), HttpStatus.BAD_REQUEST);
        }
        // Generic error message for other types of HttpMessageNotReadableException
        return new ResponseEntity<>(templateResponse.error("JSON parse error: Invalid request body."), HttpStatus.BAD_REQUEST);
    }
}
