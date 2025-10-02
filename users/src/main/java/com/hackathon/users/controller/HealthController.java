package com.hackathon.users.controller;

import com.hackathon.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> getHealth() {
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("service", "users-service");
        healthData.put("version", "1.0.0");
        healthData.put("status", "UP");
        
        return ApiResponse.success("Users Service is healthy", healthData);
    }

    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> getInfo() {
        Map<String, Object> infoData = new HashMap<>();
        infoData.put("service", "users-service");
        infoData.put("description", "User Management Service");
        infoData.put("version", "1.0.0");
        infoData.put("port", "8083");

        return ApiResponse.success("Users Service information", infoData);
    }
}