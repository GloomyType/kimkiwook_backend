package com.example.transferservice.exception;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleAccountNotFound(AccountNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String,Object>> handleInsufficientBalance(InsufficientBalanceException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleAllExceptions(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        Object message = ex.getMessage();

        // Validation 예외일 경우 -> 400 강제
        if (ex instanceof MethodArgumentNotValidException validationEx) {
            status = HttpStatus.BAD_REQUEST;
            message = validationEx.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            FieldError::getDefaultMessage,
                            (existing, replacement) -> existing
                    ));
        }

        // ResponseStatusException이면 상태 코드 그대로 사용
        if (ex instanceof org.springframework.web.server.ResponseStatusException rsEx) {
            status = (HttpStatus) rsEx.getStatusCode();
            message = rsEx.getReason();
        }

        return buildResponse(status, message);
    }

    private ResponseEntity<Map<String,Object>> buildResponse(HttpStatus status, Object message) {
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}