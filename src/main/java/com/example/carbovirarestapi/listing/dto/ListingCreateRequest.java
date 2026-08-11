package com.example.carbovirarestapi.listing.dto;

import com.example.carbovirarestapi.listing.ListingType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Map;

@Schema(description = "Yeni atık/hammadde ilanı oluşturma isteği")
public record ListingCreateRequest(

        @Schema(description = "İlanın atık mı yoksa aranan hammadde mi olduğu")
        @NotNull(message = "İlan tipi boş olamaz")
        ListingType type,

        @Schema(description = "İlan başlığı", example = "500 kg PET plastik atığı")
        @NotBlank(message = "Başlık boş olamaz")
        String title,

        @Schema(description = "Malzeme kategorisi", example = "Plastik")
        @NotBlank(message = "Kategori boş olamaz")
        String category,

        @Schema(description = "İlan detayı", example = "Temiz, tek tip PET şişe atığı, aylık düzenli tedarik mümkün.")
        String description,

        @Schema(description = "Miktar", example = "500")
        @NotNull(message = "Miktar boş olamaz")
        @Positive(message = "Miktar sıfırdan büyük olmalı")
        BigDecimal quantity,

        @Schema(description = "Birim", example = "kg")
        @NotBlank(message = "Birim boş olamaz")
        String unit,

        @Schema(description = "İlanın bulunduğu şehir", example = "İstanbul")
        @NotBlank(message = "Şehir boş olamaz")
        String city,

        @Schema(description = "Opsiyonel fiyat; belirtilmezse ücretsiz/pazarlığa açık kabul edilir", example = "1500.00")
        @DecimalMin(value = "0.0", message = "Fiyat negatif olamaz")
        BigDecimal price,

        @Schema(description = "Malzeme güvenlik bilgi formu / kompozisyon sertifikası linki (opsiyonel)",
                example = "https://example.com/sds/pet-plastik.pdf")
        String specSheetUrl,

        @Schema(description = "Malzemeye özgü ek spesifikasyonlar (opsiyonel), örn. nem oranı, saflık yüzdesi")
        Map<String, String> attributes
) {
}
