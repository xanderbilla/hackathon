package com.hackathon.api_gateway.controller;

import com.hackathon.common.dto.ApiResponse;
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
        healthData.put("service", "api-gateway");
        healthData.put("version", "1.0.0");
        healthData.put("status", "UP");
        healthData.put("role", "Request routing and load balancing");

        return ApiResponse.success("API Gateway is healthy", healthData);
    }

    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> getInfo() {
        Map<String, Object> infoData = new HashMap<>();
        infoData.put("service", "api-gateway");
        infoData.put("description", "Spring Cloud Gateway for microservices routing");
        infoData.put("version", "1.0.0");
        infoData.put("port", "8080");

        return ApiResponse.success("API Gateway information", infoData);
    }
}