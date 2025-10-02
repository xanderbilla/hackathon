package com.hackathon.users.controller;

import com.hackathon.users.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> getHealth() {
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("service", "users-service");
        healthData.put("version", "1.0.0");
        
        // Check MySQL connectivity and create database if needed
        try {
            checkMySQLConnectivity();
            healthData.put("database", "UP");
            healthData.put("database_name", "users_db");
            healthData.put("status", "UP");
            return ApiResponse.success("Users Service is healthy with database connectivity", healthData);
        } catch (Exception e) {
            healthData.put("database", "DOWN");
            healthData.put("database_error", e.getMessage());
            healthData.put("status", "UP");
            return ApiResponse.success("Users Service is running but database connection failed", healthData);
        }
    }
    
    private void checkMySQLConnectivity() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            // Test connection
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            
            // Check if users_db exists, if not create it
            try {
                jdbcTemplate.queryForObject("SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = 'users_db'", String.class);
            } catch (Exception e) {
                // Database doesn't exist, create it
                createUsersDatabase();
            }
        }
    }
    
    private void createUsersDatabase() throws Exception {
        try {
            ClassPathResource resource = new ClassPathResource("init-users-db.sql");
            String sql = new BufferedReader(new InputStreamReader(resource.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));
            
            // Execute the SQL script
            String[] statements = sql.split(";");
            for (String statement : statements) {
                if (!statement.trim().isEmpty() && !statement.trim().startsWith("--")) {
                    jdbcTemplate.execute(statement.trim());
                }
            }
        } catch (Exception e) {
            throw new Exception("Failed to create users database: " + e.getMessage());
        }
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