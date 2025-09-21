/***************************************************************************
 *
 * Pocket web backend
 * Copyright (C) 2018/2025 Antonio Salsi <passy.linux@zresa.it>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ***************************************************************************/

package it.salsi.pocket.configs;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Log
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public @NotNull final ResponseEntity<Map<String, Object>> handleConstraintViolationException(
            @NotNull final ConstraintViolationException ex,
            @NotNull final WebRequest request
    ) {
        log.warning("Validation error: " + ex.getMessage());
        
        final var errorResponse = new HashMap<String, Object>();
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Validation Failed");
        errorResponse.put("message", "Input validation failed");
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        
        // Collect all validation errors
        final var validationErrors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        
        errorResponse.put("validationErrors", validationErrors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public @NotNull final ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(
            @NotNull final MethodArgumentNotValidException ex,
            @NotNull final WebRequest request
    ) {
        log.warning("Method argument validation error: " + ex.getMessage());

        final var errorResponse = new HashMap<String, Object>();
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Validation Failed");
        errorResponse.put("message", "Request body validation failed");
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        
        // Collect field errors
        final var fieldErrors = new HashMap<String, String>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        
        errorResponse.put("fieldErrors", fieldErrors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(AuthenticationException.class)
    public @NotNull final ResponseEntity<Map<String, Object>> handleAuthenticationException(
            @NotNull final AuthenticationException ex,
            @NotNull final WebRequest request
    ) {
        log.warning("Authentication error: " + ex.getMessage());

        final var errorResponse = new HashMap<String, Object>();
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        errorResponse.put("error", "Authentication Failed");
        errorResponse.put("message", "Invalid credentials or authentication required");
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public @NotNull ResponseEntity<Map<String, Object>> handleBadCredentialsException(
            @NotNull final BadCredentialsException ex,
            @NotNull final WebRequest request
    ) {
        log.warning("Bad credentials: " + ex.getMessage());

        final var errorResponse = new HashMap<String, Object>();
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        errorResponse.put("error", "Bad Credentials");
        errorResponse.put("message", "Invalid username or password");
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public @NotNull ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            @NotNull final AccessDeniedException ex,
            @NotNull final WebRequest request
    ) {
        log.warning("Access denied: " + ex.getMessage());

        final var errorResponse = new HashMap<String, Object>();
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("status", HttpStatus.FORBIDDEN.value());
        errorResponse.put("error", "Access Denied");
        errorResponse.put("message", "Insufficient permissions to access this resource");
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public @NotNull ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            @NotNull final IllegalArgumentException ex,
            @NotNull final WebRequest request
    ) {
        log.warning("Illegal argument: " + ex.getMessage());

        final var errorResponse = new HashMap<String, Object>();
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Invalid Argument");
        errorResponse.put("message", "Invalid request parameter");
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public @NotNull ResponseEntity<Map<String, Object>> handleGenericException(
            @NotNull final Exception ex,
            @NotNull final WebRequest request
    ) {
        log.severe("Unexpected error: " + ex.getMessage());

        final var errorResponse = new HashMap<String, Object>();
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", "An unexpected error occurred");
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        
        // In production, don't expose stack trace details
        // errorResponse.put("details", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}