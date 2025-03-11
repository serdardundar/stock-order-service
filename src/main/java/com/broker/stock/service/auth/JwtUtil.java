package com.broker.stock.service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret.key}")
    private String secretKeyString;

    private Key secretKey;

    @PostConstruct
    public void init() {
        // Convert the raw secret key string into a Key instance
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
    }

    public String generateToken(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // Token valid for 10 hours
            .signWith(secretKey) // Use Key instance directly
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .setSigningKey(secretKey)          // Use secretKey to verify the token
                .parseClaimsJws(token);            // Parse and validate the token
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;                          // Invalid token
        }
    }

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject(); // Extract the 'sub' (subject/username) field
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(secretKeyString.getBytes()) // Use secret key to parse the token
            .build()
            .parseClaimsJws(token)
            .getBody(); // Extract claims from the token
    }
}
