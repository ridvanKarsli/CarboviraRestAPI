package com.example.carbovirarestapi.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** application.properties içindeki app.jwt.* ayarlarının tip güvenli karşılığı. */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expirationMs) {
}
