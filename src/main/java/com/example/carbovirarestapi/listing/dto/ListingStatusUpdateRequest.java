package com.example.carbovirarestapi.listing.dto;

import com.example.carbovirarestapi.listing.ListingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "İlan durumu değiştirme isteği (MATCHED durumu sadece görüşme akışıyla otomatik atanır)")
public record ListingStatusUpdateRequest(

        @Schema(description = "Yeni durum", example = "PASSIVE")
        @NotNull(message = "Durum boş olamaz")
        ListingStatus status
) {
}
