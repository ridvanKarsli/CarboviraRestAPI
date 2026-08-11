package com.example.carbovirarestapi.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

/**
 * Tüm hata yanıtları için tek tip (standart) gövde.
 * İstemcinin her endpoint için farklı hata formatı bekleme zorunluluğunu ortadan kaldırır.
 */
@Schema(description = "Standart hata yanıtı gövdesi")
public record ApiError(

        @Schema(description = "Hatanın oluştuğu zaman")
        Instant timestamp,

        @Schema(description = "HTTP durum kodu", example = "404")
        int status,

        @Schema(description = "HTTP durum ifadesi", example = "Not Found")
        String error,

        @Schema(description = "İnsan tarafından okunabilir hata mesajı", example = "Firma bulunamadı: id=42")
        String message,

        @Schema(description = "İsteğin yapıldığı yol", example = "/api/companies/42")
        String path,

        @Schema(description = "Doğrulama hatalarında alan bazlı detaylar (varsa)")
        List<FieldViolation> violations
) {
    @Schema(description = "Doğrulama hatası alan detayı")
    public record FieldViolation(
            @Schema(description = "Hatalı alan adı", example = "email") String field,
            @Schema(description = "Alan için hata mesajı", example = "Geçerli bir e-posta adresi giriniz") String message
    ) {
    }
}
