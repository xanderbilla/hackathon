package com.hackathon.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standardized API response wrapper for all microservices in the hackathon platform.
 * Provides consistent response structure across all endpoints with proper error handling
 * and data encapsulation. Supports both success and error responses with optional data payload.
 * 
 * @param <T> Generic type for the data payload
 * @author Vikas Singh
 * @since October 2, 2025
 * @reference Centralized response handling for microservices architecture
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String message;
    private boolean status;
    private Boolean error;
    private LocalDateTime timestamp;
    private T data;

    /**
     * Creates a successful response with message and data payload.
     * 
     * @param <T> Generic type for data
     * @param message Success message
     * @param data Response data payload
     * @return ApiResponse with success status
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .message(message)
                .status(true)
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }

    /**
     * Creates a successful response with only message.
     * 
     * @param <T> Generic type for data
     * @param message Success message
     * @return ApiResponse with success status
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .message(message)
                .status(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an error response with error message.
     * 
     * @param <T> Generic type for data
     * @param message Error message
     * @return ApiResponse with error status
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .message(message)
                .status(false)
                .error(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an error response with message and additional error data.
     * 
     * @param <T> Generic type for data
     * @param message Error message
     * @param errorData Additional error information
     * @return ApiResponse with error status and data
     */
    public static <T> ApiResponse<T> error(String message, T errorData) {
        return ApiResponse.<T>builder()
                .message(message)
                .status(false)
                .error(true)
                .timestamp(LocalDateTime.now())
                .data(errorData)
                .build();
    }
}