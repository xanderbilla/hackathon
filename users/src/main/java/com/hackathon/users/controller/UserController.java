package com.hackathon.users.controller;

import com.hackathon.users.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    /**
     * Welcome endpoint
     */
    @GetMapping("/")
    public ResponseEntity<ApiResponse<String>> welcome() {
        return ResponseEntity.ok(ApiResponse.success("Welcome to Users Service", "Users service is running"));
    }
}