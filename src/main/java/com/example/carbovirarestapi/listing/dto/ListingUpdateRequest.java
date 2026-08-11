package com.example.carbovirarestapi.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/** İlanın tipi (WASTE/RAW_MATERIAL) ve durumu bu uçtan değiştirilemez; durum için ayrı uç kullanılır. */
@Schema(description = "İlan güncelleme isteği")
public record ListingUpdateRequest(

        @Schema(description = "İlan başlığı", example = "500 kg PET plastik atığı")
        @NotBlank(message = "Başlık boş olamaz")
        String title,

        @Schema(description = "Malzeme kategorisi", example = "Plastik")
        @NotBlank(message = "Kategori boş olamaz")
        String category,

        @Schema(description = "İlan detayı")
        String description,

        @Schema(description = "Miktar", example = "500")
        @NotNull(message = "Miktar boş olamaz")
        @Positive(message = "Miktar sıfırdan büyük olmalı")
        BigDecimal quantity,

        @Schema(description = "Birim", example = "kg")
        @NotBlank(message = "Birim boş olamaz")
        String unit,

        @Schema(description = "Şehir", example = "İstanbul")
        @NotBlank(message = "Şehir boş olamaz")
        String city,

        @Schema(description = "Opsiyonel fiyat", example = "1500.00")
        @DecimalMin(value = "0.0", message = "Fiyat negatif olamaz")
        BigDecimal price
) {
}
