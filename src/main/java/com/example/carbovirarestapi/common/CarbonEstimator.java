package com.example.carbovirarestapi.common;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

/**
 * Malzeme kategorisine göre kaba bir "geri kazanım sayesinde önlenen CO2" tahmini yapar.
 * Ölçülmüş bir değer değil — yayınlanmış ortalama emisyon katsayılarına (yaklaşık kg CO2e /
 * kg malzeme) dayanan bir yaklaşım, kategori serbest metin olduğu için anahtar kelime eşleşmesi
 * kullanıyorum. Sayı bilinçli olarak kaba tutuldu, gerçek bir yaşam döngüsü analizinin yerini tutmaz.
 */
public final class CarbonEstimator {

    private static final Map<String, BigDecimal> FACTORS_BY_KEYWORD = Map.of(
            "metal", BigDecimal.valueOf(2.0),
            "plastik", BigDecimal.valueOf(1.8),
            "kagit", BigDecimal.valueOf(0.9),
            "karton", BigDecimal.valueOf(0.9),
            "cam", BigDecimal.valueOf(0.3),
            "tekstil", BigDecimal.valueOf(3.0)
    );

    private static final BigDecimal DEFAULT_FACTOR_KG_CO2_PER_KG = BigDecimal.valueOf(0.5);
    private static final BigDecimal KG_PER_TON = BigDecimal.valueOf(1000);

    private CarbonEstimator() {
    }

    /** quantity/unit kg'a çevrilemiyorsa (bilinmeyen birim) 0 döner — yanlış rakam vermektense hesaba katmıyoruz. */
    public static BigDecimal estimateCo2SavedKg(String category, BigDecimal quantity, String unit) {
        BigDecimal quantityInKg = toKg(quantity, unit);
        if (quantityInKg == null) {
            return BigDecimal.ZERO;
        }
        return quantityInKg.multiply(factorFor(category));
    }

    private static BigDecimal factorFor(String category) {
        if (category == null) {
            return DEFAULT_FACTOR_KG_CO2_PER_KG;
        }
        // "İ" (noktalı büyük İ) Locale.ROOT'ta düz "i"ye değil "i̇" (birleşik noktalı) karaktere
        // dönüşüyor, ListingSpecifications'ta yaşadığımız aynı sorun burada da çıkabilirdi —
        // önce İ'yi düz I'ya çeviriyoruz ki lowercase sonrası anahtar kelimeler gerçekten eşleşsin.
        String normalized = category.replace('İ', 'I').toLowerCase(Locale.ROOT);
        return FACTORS_BY_KEYWORD.entrySet().stream()
                .filter(entry -> normalized.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(DEFAULT_FACTOR_KG_CO2_PER_KG);
    }

    private static BigDecimal toKg(BigDecimal quantity, String unit) {
        if (quantity == null || unit == null) {
            return null;
        }
        String normalizedUnit = unit.trim().toLowerCase(Locale.ROOT);
        return switch (normalizedUnit) {
            case "kg" -> quantity;
            case "ton", "t" -> quantity.multiply(KG_PER_TON);
            default -> null;
        };
    }
}
