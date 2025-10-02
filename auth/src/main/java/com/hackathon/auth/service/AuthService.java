package com.hackathon.auth.service;

import com.hackathon.auth.dto.AuthResponse;
import com.hackathon.auth.dto.ChangePasswordRequest;
import com.hackathon.auth.dto.LoginRequest;
import com.hackathon.auth.dto.RegisterRequest;
import com.hackathon.auth.dto.UserInfoResponse;
import com.hackathon.auth.entity.AadhaarUser;
import com.hackathon.auth.entity.BankAccount;
import com.hackathon.auth.entity.LocalUser;
import com.hackathon.auth.repository.LocalUserRepository;
import com.hackathon.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class AuthService {

    @Autowired
    private LocalUserRepository localUserRepository;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SessionManagementService sessionManagementService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        // Initialize databases on startup
        try {
            databaseService.initializeDatabases();
        } catch (Exception e) {
            System.err.println("Warning: Failed to initialize databases: " + e.getMessage());
        }
    }

    /**
     * Register a new user
     */
    public AuthResponse register(RegisterRequest request) {
        try {
            // Validate inputs
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return new AuthResponse("Username is required");
            }

            if (request.getPassword() == null || request.getPassword().length() < 6) {
                return new AuthResponse("Password must be at least 6 characters");
            }

            if (request.getAadhaarNumber() == null || request.getAadhaarNumber().length() != 12) {
                return new AuthResponse("Aadhaar number must be exactly 12 digits");
            }

            // Check if username already exists
            if (localUserRepository.existsByUsername(request.getUsername())) {
                return new AuthResponse("Username already exists");
            }

            // Step 1: Verify Aadhaar number exists in aadhaar_db
            AadhaarUser aadhaarUser = databaseService.getAadhaarUserByNumber(request.getAadhaarNumber());
            if (aadhaarUser == null) {
                return new AuthResponse("Invalid Aadhaar number. Aadhaar not found in government database.");
            }

            // Step 2: Check if this Aadhaar user already has a local account
            if (localUserRepository.existsByAadhaarNumber(aadhaarUser.getAadhaarNumber())) {
                return new AuthResponse("This Aadhaar number is already registered with another account");
            }

            // Step 3: Verify bank account exists for this Aadhaar user
            BankAccount bankAccount = databaseService.getBankAccountByAadhaarNumber(aadhaarUser.getAadhaarNumber());
            if (bankAccount == null) {
                return new AuthResponse(
                        "No bank account found for this Aadhaar number. Please link your bank account first.");
            }

            // Step 4: Create local user account
            String hashedPassword = passwordEncoder.encode(request.getPassword());
            LocalUser localUser = new LocalUser(request.getUsername(), hashedPassword, aadhaarUser.getAadhaarNumber());
            localUser = localUserRepository.save(localUser);

            return new AuthResponse(
                    "Registration successful",
                    localUser.getUsername(),
                    localUser.getUserId(),
                    localUser.getAadhaarNumber());

        } catch (Exception e) {
            return new AuthResponse("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Login user
     */
    public AuthResponse login(LoginRequest request) {
        try {
            // Validate inputs
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return new AuthResponse("Username is required");
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return new AuthResponse("Password is required");
            }

            // Find user by username
            LocalUser user = localUserRepository.findByUsername(request.getUsername()).orElse(null);
            if (user == null) {
                return new AuthResponse("Invalid username or password");
            }

            // Verify password
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                return new AuthResponse("Invalid username or password");
            }

            // Generate JWT tokens
            String accessToken = jwtUtil.generateToken(user.getUsername(), user.getAadhaarNumber(), user.getUserId());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getAadhaarNumber(), user.getUserId());

            // Create session
            sessionManagementService.createSession(user.getUsername(), accessToken, refreshToken);

            AuthResponse response = new AuthResponse();
            response.setUsername(user.getUsername());
            response.setUserId(user.getUserId());
            response.setAadhaarNumber(user.getAadhaarNumber());
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            
            return response;

        } catch (Exception e) {
            return new AuthResponse("Login failed: " + e.getMessage());
        }
    }

    /**
     * Change user password
     */
    public AuthResponse changePassword(ChangePasswordRequest request) {
        try {
            // Validate inputs
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return new AuthResponse("Username is required");
            }

            if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
                return new AuthResponse("Current password is required");
            }

            if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
                return new AuthResponse("New password must be at least 6 characters");
            }

            // Find user by username
            LocalUser user = localUserRepository.findByUsername(request.getUsername()).orElse(null);
            if (user == null) {
                return new AuthResponse("User not found");
            }

            // Verify current password
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                return new AuthResponse("Current password is incorrect");
            }

            // Update password
            String hashedNewPassword = passwordEncoder.encode(request.getNewPassword());
            user.setPasswordHash(hashedNewPassword);
            localUserRepository.save(user);

            return new AuthResponse("Password changed successfully");

        } catch (Exception e) {
            return new AuthResponse("Password change failed: " + e.getMessage());
        }
    }

    /**
     * Get user information for /me endpoint
     */
    public UserInfoResponse getUserInfo(String username, String aadhaarNumber) {
        try {
            // Get user information from local database
            LocalUser localUser = localUserRepository.findByUsername(username).orElse(null);
            if (localUser == null) {
                return null;
            }

            // Get Aadhaar information
            AadhaarUser aadhaarUser = databaseService.getAadhaarUser(aadhaarNumber);
            
            // Get Bank information
            BankAccount bankAccount = databaseService.getBankAccount(aadhaarNumber);

            // Create Firy Info (Government/Aadhaar data)
            UserInfoResponse.FiryInfo firyInfo = null;
            if (aadhaarUser != null) {
                firyInfo = new UserInfoResponse.FiryInfo(
                    aadhaarUser.getAadhaarNumber(),
                    aadhaarUser.getFullName(),
                    "", // father_name not in current schema
                    aadhaarUser.getGender(),
                    aadhaarUser.getDob().toString(),
                    aadhaarUser.getAddress(),
                    "" // pincode not in current schema
                );
            }

            // Create Bank Details
            UserInfoResponse.BankDetails bankDetails = null;
            if (bankAccount != null) {
                // Get account holder name from Aadhaar data
                String accountHolderName = aadhaarUser != null ? aadhaarUser.getFullName() : "";
                
                bankDetails = new UserInfoResponse.BankDetails(
                    bankAccount.getAccountNumber().toString(),
                    accountHolderName,
                    bankAccount.getBankName(),
                    bankAccount.getIfscCode(),
                    "Savings", // default account type
                    bankAccount.getBalance().doubleValue()
                );
            }

            return new UserInfoResponse(
                localUser.getUsername(),
                localUser.getUserId(),
                localUser.getAadhaarNumber(),
                firyInfo,
                bankDetails
            );

        } catch (Exception e) {
            System.err.println("Error getting user info: " + e.getMessage());
            return null;
        }
    }
}