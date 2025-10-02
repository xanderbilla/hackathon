package com.hackathon.common.exception;

import com.hackathon.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handler for all microservices in the hackathon platform.
 * Provides consistent error response format across all services and handles common
 * exceptions like validation errors, not found endpoints, and method not supported.
 * Extends the base functionality with custom error data mapping for better debugging.
 * 
 * @author Vikas Singh
 * @since October 2, 2025
 * @reference Global exception handling strategy for microservices architecture
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles NoHandlerFoundException when endpoint is not found.
     * 
     * @param ex NoHandlerFoundException instance
     * @return ResponseEntity with 404 status and error details
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(NoHandlerFoundException ex) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("requestedEndpoint", ex.getRequestURL());
        errorData.put("httpMethod", ex.getHttpMethod());
        
        ApiResponse<Object> response = ApiResponse.error(
            "Endpoint not found: " + ex.getRequestURL(), 
            errorData
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles HttpRequestMethodNotSupportedException for unsupported HTTP methods.
     * 
     * @param ex HttpRequestMethodNotSupportedException instance
     * @return ResponseEntity with 405 status and supported methods information
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String supportedMethods = String.join(", ", ex.getSupportedMethods());
        
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("requestedMethod", ex.getMethod());
        errorData.put("supportedMethods", supportedMethods);
        errorData.put("suggestion", "Please use one of the supported methods: " + supportedMethods);
        
        ApiResponse<Object> response = ApiResponse.error(
            "HTTP method not supported: " + ex.getMethod(), 
            errorData
        );
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Handles MethodArgumentNotValidException for request validation failures.
     * 
     * @param ex MethodArgumentNotValidException instance
     * @return ResponseEntity with 400 status and validation error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("field", ex.getBindingResult().getFieldError().getField());
        errorData.put("rejectedValue", ex.getBindingResult().getFieldError().getRejectedValue());
        errorData.put("validationMessage", ex.getBindingResult().getFieldError().getDefaultMessage());
        
        ApiResponse<Object> response = ApiResponse.error(
            "Validation failed: " + ex.getBindingResult().getFieldError().getDefaultMessage(),
            errorData
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles general Exception as a fallback for unhandled exceptions.
     * 
     * @param ex Exception instance
     * @return ResponseEntity with 500 status and error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception ex) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("exceptionType", ex.getClass().getSimpleName());
        errorData.put("message", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(
            "Internal server error occurred",
            errorData
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles IllegalArgumentException for invalid method arguments.
     * 
     * @param ex IllegalArgumentException instance
     * @return ResponseEntity with 400 status and error details
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("invalidArgument", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(
            "Invalid argument provided: " + ex.getMessage(),
            errorData
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles RuntimeException for runtime errors.
     * 
     * @param ex RuntimeException instance
     * @return ResponseEntity with 500 status and error details
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("runtimeError", ex.getMessage());
        errorData.put("exceptionType", ex.getClass().getSimpleName());
        
        ApiResponse<Object> response = ApiResponse.error(
            "Runtime error occurred: " + ex.getMessage(),
            errorData
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}