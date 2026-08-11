package com.example.carbovirarestapi.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Vergi numarası kimlik bilgisi olduğu için bu uçtan güncellenemez; sadece profil bilgileri değiştirilebilir. */
@Schema(description = "Firma profili güncelleme isteği (vergi numarası bu uçtan değiştirilemez)")
public record CompanyUpdateRequest(

        @Schema(description = "Firma adı", example = "Acme Geri Dönüşüm A.Ş.")
        @NotBlank(message = "Firma adı boş olamaz")
        String name,

        @Schema(description = "Faaliyet sektörü", example = "Metal Geri Dönüşüm")
        String sector,

        @Schema(description = "Şehir", example = "İstanbul")
        String city,

        @Schema(description = "Açık adres", example = "Organize Sanayi Bölgesi, 5. Cadde No:12")
        String address,

        @Schema(description = "Firma hakkında serbest metin açıklama", example = "Metal hurda geri dönüşümü konusunda uzman firma.")
        @Size(max = 2000, message = "Açıklama en fazla 2000 karakter olabilir")
        String description
) {
}
