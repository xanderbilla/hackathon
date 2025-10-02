package com.hackathon.auth.controller;

import com.hackathon.auth.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> getHealth() {
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("service", "auth-service");
        healthData.put("status", "UP");
        healthData.put("version", "1.0.0");

        return ApiResponse.success("Auth Service is healthy", healthData);
    }

    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> getInfo() {
        Map<String, Object> infoData = new HashMap<>();
        infoData.put("service", "auth-service");
        infoData.put("description", "Authentication and Authorization Service");
        infoData.put("version", "1.0.0");
        infoData.put("port", "8082");

        return ApiResponse.success("Auth Service information", infoData);
    }
}