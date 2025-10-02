package com.hackathon.users.controller;

import com.hackathon.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * User controller for managing user-related operations
 * 
 * Author: Vikas Singh
 * Date: October 2, 2025
 * Description: REST controller for user service endpoints
 */
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    /**
     * Welcome endpoint
     */
    @GetMapping("/")
    public ResponseEntity<ApiResponse<Object>> welcome() {
        return ResponseEntity.ok(ApiResponse.success("Welcome to Users Service", "Users service is running"));
    }
}