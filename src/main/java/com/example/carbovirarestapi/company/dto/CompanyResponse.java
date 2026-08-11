package com.example.carbovirarestapi.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/** Dışa açık firma profili görünümü. */
@Schema(description = "Firma profili görünümü")
public record CompanyResponse(

        @Schema(description = "Firma kimliği", example = "1")
        Long id,

        @Schema(description = "Firma adı", example = "Acme Geri Dönüşüm A.Ş.")
        String name,

        @Schema(description = "Vergi numarası", example = "1234567890")
        String taxNumber,

        @Schema(description = "Faaliyet sektörü", example = "Metal Geri Dönüşüm")
        String sector,

        @Schema(description = "Şehir", example = "İstanbul")
        String city,

        @Schema(description = "Açık adres", example = "Organize Sanayi Bölgesi, 5. Cadde No:12")
        String address,

        @Schema(description = "Firma hakkında serbest metin açıklama")
        String description,

        @Schema(description = "Platform yönetimi tarafından onaylanmış mı")
        boolean verified,

        @Schema(description = "Kayıt tarihi")
        Instant createdAt
) {
}
