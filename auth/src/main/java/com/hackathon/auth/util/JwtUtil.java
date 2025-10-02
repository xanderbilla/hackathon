package com.hackathon.auth.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    
    @Value("${app.jwt.secret:mySecretKeyForSustainAThonHackathon2024WithEnoughLengthForHS256}")
    private String secretKey;
    
    @Value("${app.jwt.expiration:900000}") // 15 minutes in milliseconds
    private long jwtExpiration;
    
    @Value("${app.jwt.refresh-expiration:3600000}") // 1 hour in milliseconds  
    private long refreshExpiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    public String generateToken(String username, String aadhaarNumber, Integer userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("aadhaarNumber", aadhaarNumber);
        claims.put("userId", userId);
        claims.put("type", "access");
        return createToken(claims, username, jwtExpiration);
    }
    
    public String generateRefreshToken(String username, String aadhaarNumber, Integer userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("aadhaarNumber", aadhaarNumber);
        claims.put("userId", userId);
        claims.put("type", "refresh");
        return createToken(claims, username, refreshExpiration);
    }
    
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
    
    public String getAadhaarNumberFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get("aadhaarNumber");
    }
    
    public Integer getUserIdFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return (Integer) claims.get("userId");
    }
    
    public String getTokenType(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get("type");
    }
    
    public boolean isRefreshToken(String token) {
        return "refresh".equals(getTokenType(token));
    }
    
    public boolean isAccessToken(String token) {
        return "access".equals(getTokenType(token));
    }
}