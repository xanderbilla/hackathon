package com.hackathon.common.util;

import com.hackathon.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for building standardized HTTP responses across all microservices.
 * Provides convenient methods for creating ResponseEntity objects with proper
 * HTTP status codes and consistent ApiResponse format. Reduces code duplication
 * and ensures uniform response structure throughout the application.
 * 
 * @author Vikas Singh
 * @since October 2, 2025
 * @reference Centralized response building utility for microservices
 */
public class ResponseUtil {

    /**
     * Creates a successful response with data and 200 OK status.
     * 
     * @param <T> Generic type for response data
     * @param message Success message
     * @param data Response data payload
     * @return ResponseEntity with success response
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    /**
     * Creates a successful response with only message and 200 OK status.
     * 
     * @param message Success message
     * @return ResponseEntity with success response
     */
    public static ResponseEntity<ApiResponse<String>> success(String message) {
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /**
     * Creates an error response with 400 Bad Request status.
     * 
     * @param message Error message
     * @return ResponseEntity with error response
     */
    public static ResponseEntity<ApiResponse<Object>> badRequest(String message) {
        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }

    /**
     * Creates an error response with custom error data and 400 Bad Request status.
     * 
     * @param message Error message
     * @param errorData Additional error information
     * @return ResponseEntity with error response and data
     */
    public static ResponseEntity<ApiResponse<Object>> badRequest(String message, Object errorData) {
        return ResponseEntity.badRequest().body(ApiResponse.error(message, errorData));
    }

    /**
     * Creates an unauthorized response with 401 status.
     * 
     * @param message Error message
     * @return ResponseEntity with unauthorized response
     */
    public static ResponseEntity<ApiResponse<Object>> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(message));
    }

    /**
     * Creates a forbidden response with 403 status.
     * 
     * @param message Error message
     * @return ResponseEntity with forbidden response
     */
    public static ResponseEntity<ApiResponse<Object>> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(message));
    }

    /**
     * Creates a not found response with 404 status.
     * 
     * @param message Error message
     * @return ResponseEntity with not found response
     */
    public static ResponseEntity<ApiResponse<Object>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(message));
    }

    /**
     * Creates an internal server error response with 500 status.
     * 
     * @param message Error message
     * @return ResponseEntity with internal server error response
     */
    public static ResponseEntity<ApiResponse<Object>> internalServerError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(message));
    }

    /**
     * Creates an internal server error response with custom error data and 500 status.
     * 
     * @param message Error message
     * @param errorData Additional error information
     * @return ResponseEntity with internal server error response and data
     */
    public static ResponseEntity<ApiResponse<Object>> internalServerError(String message, Object errorData) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(message, errorData));
    }

    /**
     * Creates a custom response with specified HTTP status.
     * 
     * @param <T> Generic type for response data
     * @param status HTTP status code
     * @param message Response message
     * @param data Response data payload
     * @return ResponseEntity with custom status and response
     */
    public static <T> ResponseEntity<ApiResponse<T>> customResponse(HttpStatus status, String message, T data) {
        if (status.is2xxSuccessful()) {
            return ResponseEntity.status(status).body(ApiResponse.success(message, data));
        } else {
            return ResponseEntity.status(status).body(ApiResponse.error(message, data));
        }
    }

    /**
     * Creates a validation error response with field-specific error information.
     * 
     * @param fieldName Name of the invalid field
     * @param rejectedValue Value that was rejected
     * @param validationMessage Validation error message
     * @return ResponseEntity with validation error response
     */
    public static ResponseEntity<ApiResponse<Object>> validationError(String fieldName, Object rejectedValue, String validationMessage) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("field", fieldName);
        errorData.put("rejectedValue", rejectedValue);
        errorData.put("validationMessage", validationMessage);
        
        return ResponseEntity.badRequest().body(ApiResponse.error("Validation failed: " + validationMessage, errorData));
    }
}