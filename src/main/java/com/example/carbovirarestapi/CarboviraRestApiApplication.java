package com.example.carbovirarestapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CarboviraRestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarboviraRestApiApplication.class, args);
    }

}

/*
 * Paket yapısı, katmana göre değil özelliğe (feature/domain) göre kurulmuştur.
 * Her modül kendi controller/service/repository/dto'sunu bir arada barındırır;
 * bu, ilerleyen fazlarda (listing, messaging...) modüllerin birbirinden
 * bağımsız büyümesini ve bakımını kolaylaştırır.
 *
 * com.example.carbovirarestapi
 * ├── common       # BaseEntity, ApiError, custom exception'lar, GlobalExceptionHandler
 * ├── config       # JPA auditing, OpenAPI/Swagger yapılandırması
 * ├── security     # JWT üretimi/doğrulaması, SecurityConfig, UserPrincipal
 * ├── user         # User entity, Role enum, UserRepository
 * ├── company      # Company entity, DTO, Mapper, Service (arayüz + impl), Controller
 * ├── auth         # Kayıt/giriş DTO'ları, AuthService (arayüz + impl), AuthController
 * ├── listing      # (Faz 2) Atık/hammadde ilanları
 * └── messaging    # (Faz 4) Firmalar arası mesajlaşma
 */
