package com.example.carbovirarestapi.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CarbonEstimatorTest {

    @Test
    void estimateCo2SavedKg_usesMetalFactor_forKgQuantity() {
        BigDecimal result = CarbonEstimator.estimateCo2SavedKg("Metal Hurda", BigDecimal.valueOf(100), "kg");

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(200));
    }

    @Test
    void estimateCo2SavedKg_convertsTonToKg_beforeApplyingFactor() {
        BigDecimal result = CarbonEstimator.estimateCo2SavedKg("Plastik", BigDecimal.valueOf(2), "ton");

        // 2 ton = 2000 kg, plastik katsayısı 1.8 -> 3600
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(3600));
    }

    @Test
    void estimateCo2SavedKg_matchesTurkishUppercaseCategory_despiteLocaleQuirk() {
        // "İ" büyük harfi Türkçe olmayan locale'de lowercase'e çevrilince sorun çıkarabiliyordu,
        // burada "TEKSTİL" gibi büyük harfli bir kategori de düzgün eşleşmeli.
        BigDecimal result = CarbonEstimator.estimateCo2SavedKg("TEKSTİL ATIĞI", BigDecimal.valueOf(10), "kg");

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(30));
    }

    @Test
    void estimateCo2SavedKg_fallsBackToDefaultFactor_forUnknownCategory() {
        BigDecimal result = CarbonEstimator.estimateCo2SavedKg("Bilinmeyen malzeme", BigDecimal.valueOf(10), "kg");

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(5));
    }

    @Test
    void estimateCo2SavedKg_returnsZero_forUnknownUnit() {
        BigDecimal result = CarbonEstimator.estimateCo2SavedKg("Metal", BigDecimal.valueOf(10), "litre");

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
