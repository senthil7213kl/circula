package com.library.circula.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.TreeMap;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new TreeMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("status", 400, "error", "Validation failed", "fields", fields));
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String, Object>> status(ResponseStatusException ex) {
        int status = ex.getStatusCode().value();
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("status", status, "error", ex.getReason() == null ? "Request failed" : ex.getReason()));
    }
}
