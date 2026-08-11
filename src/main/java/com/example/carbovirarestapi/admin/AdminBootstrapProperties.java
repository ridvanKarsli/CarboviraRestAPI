package com.example.carbovirarestapi.admin;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** application.properties içindeki app.admin.* ayarlarının tip güvenli karşılığı. */
@ConfigurationProperties(prefix = "app.admin")
public record AdminBootstrapProperties(String email, String password) {
}
