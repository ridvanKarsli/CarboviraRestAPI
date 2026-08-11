package com.example.carbovirarestapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Giriş isteği")
public record LoginRequest(

        @Schema(description = "Kayıtlı e-posta adresi", example = "ayse@acme.com")
        @NotBlank(message = "E-posta boş olamaz")
        @Email(message = "Geçerli bir e-posta adresi giriniz")
        String email,

        @Schema(description = "Şifre", example = "guclu-sifre-123")
        @NotBlank(message = "Şifre boş olamaz")
        String password
) {
}
