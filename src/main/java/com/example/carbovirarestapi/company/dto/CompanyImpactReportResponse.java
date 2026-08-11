package com.example.carbovirarestapi.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Firmanın platform üzerindeki faaliyetinin kaba bir özeti. Sürdürülebilirlik raporlaması
 * yapan firmalar için başlangıç noktası olsun diye ekledim, gerçek bir CO2 hesabı değil.
 */
@Schema(description = "Firma etki/faaliyet özeti")
public record CompanyImpactReportResponse(

        @Schema(description = "Toplam ilan sayısı (durum fark etmeksizin)", example = "12")
        long totalListings,

        @Schema(description = "Şu an ACTIVE durumdaki ilan sayısı", example = "5")
        long activeListings,

        @Schema(description = "ARCHIVED durumdaki ilan sayısı", example = "4")
        long archivedListings,

        @Schema(description = "Birim bazlı toplam ilan miktarı (kg, ton vs. birimler ayrı ayrı toplanır, karıştırılmaz)")
        Map<String, BigDecimal> totalQuantityByUnit,

        @Schema(description = "Taraf olunan toplam görüşme sayısı", example = "7")
        long totalConversations
) {
}
