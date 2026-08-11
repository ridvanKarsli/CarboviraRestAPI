package com.example.carbovirarestapi.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * Firmanın platform üzerindeki faaliyetinin kaba bir özeti. Sürdürülebilirlik raporlaması
 * yapan firmalar için başlangıç noktası olsun diye ekledim, gerçek bir CO2 hesabı değil.
 *
 * totalListings/activeListings/archivedListings/totalQuantityByUnit ve totalConversations firmanın
 * tüm geçmişini kapsar, periodFrom/periodTo'dan etkilenmez — sadece co2SavedKg* alanları dönem bazlı.
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
        long totalConversations,

        @Schema(description = "Karbon hesabının başlangıcı, verilmediyse tüm geçmiş dikkate alınır")
        Instant periodFrom,

        @Schema(description = "Karbon hesabının bitişi, verilmediyse tüm geçmiş dikkate alınır")
        Instant periodTo,

        @Schema(description = "Bu dönemde sattığım (arşivlenen kendi ilanlarım) malzemeden önlenen tahmini CO2, kg cinsinden")
        BigDecimal co2SavedKgFromSelling,

        @Schema(description = "Bu dönemde satın aldığım (görüşme başlattığım ve arşivlenmiş) malzemeden önlenen tahmini CO2, kg cinsinden")
        BigDecimal co2SavedKgFromBuying,

        @Schema(description = "co2SavedKgFromSelling + co2SavedKgFromBuying")
        BigDecimal co2SavedKgTotal
) {
}
