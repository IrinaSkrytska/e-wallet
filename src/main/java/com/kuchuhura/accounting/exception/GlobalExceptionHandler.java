package com.kuchuhura.accounting.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception exception, WebRequest webRequest) {

        if (exception instanceof BadCredentialsException) {
            var errorDetails = new ErrorDetails(
                    Instant.now(),
                    "The username or password is incorrect",
                    webRequest.getDescription(false)
            );
            logger.error("bad credential exception: {}", exception);
            return ResponseEntity.status(HttpStatusCode.valueOf(401)).body(errorDetails);
        }

        if (exception instanceof AccountStatusException) {
            var errorDetails = new ErrorDetails(
                    Instant.now(),
                    "The account is locked",
                    webRequest.getDescription(false)
            );
            logger.error("account status exception: {}", exception);
            return ResponseEntity.status(HttpStatusCode.valueOf(403)).body(errorDetails);
        }

        if (exception instanceof AccessDeniedException) {
            var errorDetails = new ErrorDetails(
                    Instant.now(),
                    "You are not authorized to access this resource",
                    webRequest.getDescription(false)
            );
            logger.error("access denied exception: {}", exception);
            return ResponseEntity.status(HttpStatusCode.valueOf(403)).body(errorDetails);
        }

        if (exception instanceof SignatureException) {
            var errorDetails = new ErrorDetails(
                    Instant.now(),
                    "The JWT signature is invalid",
                    webRequest.getDescription(false)
            );
            logger.error("signature exception: {}", exception);
            return ResponseEntity.status(HttpStatusCode.valueOf(403)).body(errorDetails);
        }

        if (exception instanceof ExpiredJwtException) {
            var errorDetails = new ErrorDetails(
                    Instant.now(),
                    "The JWT has expired",
                    webRequest.getDescription(false)
            );
            logger.error("expired jwt exception exception: {}", exception);
            return ResponseEntity.status(HttpStatusCode.valueOf(403)).body(errorDetails);
        }

        if (exception instanceof CustomException customException) {
            var errorDetails = new ErrorDetails(
                    Instant.now(),
                    customException.getMessage(),
                    webRequest.getDescription(false)
            );
            logger.error("custom exception: {}", exception);
            return ResponseEntity.status(customException.getHttpStatus()).body(errorDetails);
        }
        logger.error("internal server error: {}", exception);
        return ResponseEntity.status(HttpStatusCode.valueOf(500))
                .body(
                        new ErrorDetails(Instant.now(),
                                "Unknown internal server error",
                                webRequest.getDescription(false)
                        ));
    }
}
