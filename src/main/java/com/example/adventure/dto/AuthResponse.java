package com.example.adventure.dto;

public record AuthResponse(String token, String tokenType, long expiresIn) {
}
