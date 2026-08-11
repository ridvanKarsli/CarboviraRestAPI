package com.example.carbovirarestapi.listing.dto;

import com.example.carbovirarestapi.listing.ListingStatus;
import com.example.carbovirarestapi.listing.ListingType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Schema(description = "İlan görünümü")
public record ListingResponse(

        @Schema(description = "İlan kimliği", example = "1")
        Long id,

        @Schema(description = "İlan tipi")
        ListingType type,

        @Schema(description = "Başlık", example = "500 kg PET plastik atığı")
        String title,

        @Schema(description = "Kategori", example = "Plastik")
        String category,

        @Schema(description = "Açıklama")
        String description,

        @Schema(description = "Miktar", example = "500")
        BigDecimal quantity,

        @Schema(description = "Birim", example = "kg")
        String unit,

        @Schema(description = "Şehir", example = "İstanbul")
        String city,

        @Schema(description = "Fiyat (varsa)", example = "1500.00")
        BigDecimal price,

        @Schema(description = "Durum")
        ListingStatus status,

        @Schema(description = "İlan sahibi firmanın kimliği", example = "1")
        Long companyId,

        @Schema(description = "İlan sahibi firmanın adı", example = "Acme Geri Dönüşüm A.Ş.")
        String companyName,

        @Schema(description = "Malzeme güvenlik bilgi formu / kompozisyon sertifikası linki (varsa)")
        String specSheetUrl,

        @Schema(description = "Malzemeye özgü ek spesifikasyonlar")
        Map<String, String> attributes,

        @Schema(description = "Oluşturulma tarihi")
        Instant createdAt
) {
}
