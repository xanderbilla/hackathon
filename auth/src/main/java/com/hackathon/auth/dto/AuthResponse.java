package com.hackathon.auth.dto;

public class AuthResponse {
    private String message;
    private String username;
    private Integer userId;
    private String aadhaarNumber;
    private String accessToken;
    private String refreshToken;

    // Constructors
    public AuthResponse() {
    }

    public AuthResponse(String message) {
        this.message = message;
    }

    public AuthResponse(String message, String username, Integer userId, String aadhaarNumber) {
        this.message = message;
        this.username = username;
        this.userId = userId;
        this.aadhaarNumber = aadhaarNumber;
    }

    public AuthResponse(String message, String username, Integer userId, String aadhaarNumber, 
                       String accessToken, String refreshToken) {
        this.message = message;
        this.username = username;
        this.userId = userId;
        this.aadhaarNumber = aadhaarNumber;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getAadhaarNumber() {
        return aadhaarNumber;
    }

    public void setAadhaarNumber(String aadhaarNumber) {
        this.aadhaarNumber = aadhaarNumber;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}