package com.hackathon.service_registry.controller;

import com.hackathon.service_registry.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ApiResponse<Map<String, Object>> getHealth() {
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("service", "service-registry");
        healthData.put("status", "UP");
        healthData.put("version", "1.0.0");

        return ApiResponse.success("Service Registry is healthy", healthData);
    }

    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> getInfo() {
        Map<String, Object> infoData = new HashMap<>();
        infoData.put("service", "service-registry");
        infoData.put("description", "Eureka Service Registry for microservices");
        infoData.put("version", "1.0.0");
        infoData.put("port", "8761");

        return ApiResponse.success("Service Registry information", infoData);
    }
}