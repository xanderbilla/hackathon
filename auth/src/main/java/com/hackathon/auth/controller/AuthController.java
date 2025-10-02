package com.hackathon.auth.controller;

import com.hackathon.auth.dto.*;
import com.hackathon.auth.service.AuthService;
import com.hackathon.auth.service.SessionManagementService;
import com.hackathon.auth.util.JwtUtil;
import com.hackathon.common.dto.ApiResponse;
import com.hackathon.common.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Validated
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private SessionManagementService sessionManagementService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);

            if (response.getUsername() != null) {
                return ResponseUtil.success("Registration successful", "User registered successfully");
            } else {
                return ResponseUtil.badRequest("Registration failed");
            }

        } catch (Exception e) {
            return ResponseUtil.internalServerError("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Login user - Sets JWT tokens as HttpOnly cookies
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> login(@Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        try {
            AuthResponse authResponse = authService.login(request);

            if (authResponse.getAccessToken() != null) {
                // Set access token as HttpOnly cookie (15 minutes)
                Cookie accessTokenCookie = new Cookie("accessToken", authResponse.getAccessToken());
                accessTokenCookie.setHttpOnly(true);
                accessTokenCookie.setSecure(false); // Set to true in production with HTTPS
                accessTokenCookie.setPath("/");
                accessTokenCookie.setMaxAge(15 * 60); // 15 minutes
                response.addCookie(accessTokenCookie);

                // Set refresh token as HttpOnly cookie (1 hour)
                Cookie refreshTokenCookie = new Cookie("refreshToken", authResponse.getRefreshToken());
                refreshTokenCookie.setHttpOnly(true);
                refreshTokenCookie.setSecure(false); // Set to true in production with HTTPS
                refreshTokenCookie.setPath("/");
                refreshTokenCookie.setMaxAge(60 * 60); // 1 hour
                response.addCookie(refreshTokenCookie);

                // Don't send tokens or user data in response body
                return ResponseUtil.success("Login successful", "User logged in successfully");
            } else {
                return ResponseUtil.badRequest("Invalid credentials");
            }

        } catch (Exception e) {
            return ResponseUtil.internalServerError("Login failed: " + e.getMessage());
        }
    }

    /**
     * Change user password
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Object>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            AuthResponse response = authService.changePassword(request);

            if ("Password changed successfully".equals(response.getMessage())) {
                return ResponseUtil.success("Password changed successfully", "Password updated");
            } else {
                return ResponseUtil.badRequest(response.getMessage());
            }

        } catch (Exception e) {
            return ResponseUtil.internalServerError("Password change failed: " + e.getMessage());
        }
    }

    /**
     * Get current user information - /me endpoint
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser(HttpServletRequest request) {
        try {
            String token = extractTokenFromCookies(request);
            if (token == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("User need to login first"));
            }

            String username = jwtUtil.extractUsername(token);
            String aadhaarNumber = jwtUtil.getAadhaarNumberFromToken(token);

            UserInfoResponse userInfo = authService.getUserInfo(username, aadhaarNumber);

            return ResponseEntity.ok(ApiResponse.success("User information retrieved", userInfo));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get user info: " + e.getMessage()));
        }
    }

    /**
     * Logout user - Clears JWT cookies
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            String token = extractTokenFromCookies(request);
            if (token == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("User need to login first"));
            }

            String username = jwtUtil.extractUsername(token);

            // Invalidate session both by username and token to ensure complete cleanup
            sessionManagementService.invalidateSession(username);
            sessionManagementService.invalidateSessionByToken(token);

            // Clear cookies with proper security attributes
            Cookie accessTokenCookie = new Cookie("accessToken", "");
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(false); // Set to true in production with HTTPS
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(0);
            response.addCookie(accessTokenCookie);

            Cookie refreshTokenCookie = new Cookie("refreshToken", "");
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false); // Set to true in production with HTTPS
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(0);
            response.addCookie(refreshTokenCookie);

            // Add security headers to prevent caching of logout response
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");

            return ResponseUtil.success("Logout successful", "User logged out");

        } catch (Exception e) {
            return ResponseUtil.internalServerError("Logout failed: " + e.getMessage());
        }
    }

    /**
     * Refresh JWT token - Special endpoint that accepts refresh tokens
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<Object>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            String refreshToken = extractRefreshTokenFromCookies(request);
            if (refreshToken == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("User need to login first"));
            }

            // Validate refresh token without session check
            if (!jwtUtil.isRefreshToken(refreshToken) || jwtUtil.isTokenExpired(refreshToken)) {
                return ResponseEntity.status(401).body(ApiResponse.error("User need to login first"));
            }

            String username = jwtUtil.extractUsername(refreshToken);
            String storedRefreshToken = sessionManagementService.getRefreshToken(username);

            if (!refreshToken.equals(storedRefreshToken)) {
                return ResponseEntity.status(401).body(ApiResponse.error("User need to login first"));
            }

            String aadhaarNumber = jwtUtil.getAadhaarNumberFromToken(refreshToken);
            Integer userId = jwtUtil.getUserIdFromToken(refreshToken);

            // Generate new access token
            String newAccessToken = jwtUtil.generateToken(username, aadhaarNumber, userId);
            sessionManagementService.updateSession(username, newAccessToken);

            // Set new access token as cookie
            Cookie accessTokenCookie = new Cookie("accessToken", newAccessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(false);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(15 * 60); // 15 minutes
            response.addCookie(accessTokenCookie);

            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", "New access token set"));

        } catch (Exception e) {
            return ResponseEntity.status(401).body(ApiResponse.error("User need to login first"));
        }
    }

    /**
     * Extract JWT access token from cookies
     */
    private String extractTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Extract refresh token from cookies
     */
    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}