package com.example.carbovirarestapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** BaseEntity içindeki createdAt/updatedAt alanlarının otomatik doldurulmasını sağlar. */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
