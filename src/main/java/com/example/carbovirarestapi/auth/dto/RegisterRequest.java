package com.example.carbovirarestapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Kayıt tek adımda hem firmayı hem de o firmanın ilk (COMPANY_ADMIN) kullanıcısını oluşturur. */
@Schema(description = "Firma ve firmanın ilk yöneticisini birlikte oluşturan kayıt isteği")
public record RegisterRequest(

        @Schema(description = "Firma adı", example = "Acme Geri Dönüşüm A.Ş.")
        @NotBlank(message = "Firma adı boş olamaz")
        String companyName,

        @Schema(description = "Vergi numarası (benzersiz)", example = "1234567890")
        @NotBlank(message = "Vergi numarası boş olamaz")
        String taxNumber,

        @Schema(description = "Faaliyet sektörü", example = "Metal Geri Dönüşüm")
        String sector,

        @Schema(description = "Firmanın bulunduğu şehir", example = "İstanbul")
        String city,

        @Schema(description = "Açık adres", example = "Organize Sanayi Bölgesi, 5. Cadde No:12")
        String address,

        @Schema(description = "Kayıt olan kullanıcının adı soyadı", example = "Ayşe Yılmaz")
        @NotBlank(message = "Ad soyad boş olamaz")
        String fullName,

        @Schema(description = "Giriş için kullanılacak e-posta adresi", example = "ayse@acme.com")
        @NotBlank(message = "E-posta boş olamaz")
        @Email(message = "Geçerli bir e-posta adresi giriniz")
        String email,

        @Schema(description = "En az 8 karakterli şifre", example = "guclu-sifre-123")
        @NotBlank(message = "Şifre boş olamaz")
        @Size(min = 8, message = "Şifre en az 8 karakter olmalı")
        String password
) {
}
