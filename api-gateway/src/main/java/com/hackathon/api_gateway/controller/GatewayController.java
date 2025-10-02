package com.hackathon.api_gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class GatewayController {

    private final WebClient webClient;

    public GatewayController() {
        this.webClient = WebClient.builder().build();
    }

    @GetMapping("/service/health")
    public Mono<ResponseEntity<Map<String, Object>>> serviceRegistryHealth() {
        return webClient.get()
                .uri("http://localhost:8761/api/v1/health")
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> typedResponse = (Map<String, Object>) response;
                    return ResponseEntity.ok(typedResponse);
                })
                .onErrorReturn(ResponseEntity.status(503).body(Map.of("status", "DOWN", "service", "service-registry")));
    }

    @GetMapping("/auth/health")
    public Mono<ResponseEntity<Map<String, Object>>> authHealth() {
        return webClient.get()
                .uri("http://localhost:8082/api/v1/auth/health")
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> typedResponse = (Map<String, Object>) response;
                    return ResponseEntity.ok(typedResponse);
                })
                .onErrorReturn(ResponseEntity.status(503).body(Map.of("status", "DOWN", "service", "auth")));
    }

    @GetMapping("/users/health")
    public Mono<ResponseEntity<Map<String, Object>>> usersHealth() {
        return webClient.get()
                .uri("http://localhost:8083/api/v1/users/health")
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> typedResponse = (Map<String, Object>) response;
                    return ResponseEntity.ok(typedResponse);
                })
                .onErrorReturn(ResponseEntity.status(503).body(Map.of("status", "DOWN", "service", "users")));
    }

    @GetMapping("/gateway/health")
    public ResponseEntity<Map<String, Object>> gatewayHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "api-gateway");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}