package com.hackathon.auth.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class SessionManagementService {
    
    // In-memory session storage (in production, use Redis)
    private final Map<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    public SessionManagementService() {
        // Clean up expired sessions every minute
        scheduler.scheduleAtFixedRate(this::cleanupExpiredSessions, 1, 1, TimeUnit.MINUTES);
    }
    
    public void createSession(String username, String accessToken, String refreshToken) {
        SessionInfo session = new SessionInfo(username, accessToken, refreshToken, System.currentTimeMillis());
        activeSessions.put(username, session);
    }
    
    public boolean isSessionValid(String username, String token) {
        SessionInfo session = activeSessions.get(username);
        if (session == null) {
            return false;
        }
        
        // Check if session is expired (15 minutes)
        if (System.currentTimeMillis() - session.getLastActivity() > 15 * 60 * 1000) {
            activeSessions.remove(username);
            return false;
        }
        
        // Update last activity
        session.setLastActivity(System.currentTimeMillis());
        return session.getAccessToken().equals(token);
    }
    
    public void updateSession(String username, String newAccessToken) {
        SessionInfo session = activeSessions.get(username);
        if (session != null) {
            session.setAccessToken(newAccessToken);
            session.setLastActivity(System.currentTimeMillis());
        }
    }
    
    public String getRefreshToken(String username) {
        SessionInfo session = activeSessions.get(username);
        return session != null ? session.getRefreshToken() : null;
    }
    
    public void invalidateSession(String username) {
        activeSessions.remove(username);
    }
    
    public void invalidateSessionByToken(String token) {
        activeSessions.entrySet().removeIf(entry -> 
            entry.getValue().getAccessToken().equals(token) || 
            entry.getValue().getRefreshToken().equals(token)
        );
    }
    
    private void cleanupExpiredSessions() {
        long currentTime = System.currentTimeMillis();
        activeSessions.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().getLastActivity() > 15 * 60 * 1000
        );
    }
    
    public boolean hasActiveSession(String username) {
        SessionInfo session = activeSessions.get(username);
        if (session == null) {
            return false;
        }
        
        // Check if session is still valid
        if (System.currentTimeMillis() - session.getLastActivity() > 15 * 60 * 1000) {
            activeSessions.remove(username);
            return false;
        }
        
        return true;
    }
    
    // Inner class for session information
    private static class SessionInfo {
        private String username;
        private String accessToken;
        private String refreshToken;
        private long lastActivity;
        
        public SessionInfo(String username, String accessToken, String refreshToken, long lastActivity) {
            this.username = username;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.lastActivity = lastActivity;
        }
        
        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
        
        public long getLastActivity() { return lastActivity; }
        public void setLastActivity(long lastActivity) { this.lastActivity = lastActivity; }
    }
}