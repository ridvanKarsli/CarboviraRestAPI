package com.example.carbovirarestapi.messaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * "Karşı taraf" (counterpart), bu yanıtı alan kullanıcının bakış açısına göre hesaplanır:
 * kullanıcı görüşmeyi başlatansa karşı taraf ilan sahibi firmadır, ilan sahibiyse karşı
 * taraf görüşmeyi başlatan firmadır. Bu yüzden bu dönüşüm MapStruct ile değil, çağıranın
 * kimliğini bilen ConversationServiceImpl içinde elle yapılır.
 */
@Schema(description = "Görüşme özeti (karşı taraf, çağıran kullanıcının bakış açısına göre gösterilir)")
public record ConversationResponse(

        @Schema(description = "Görüşme kimliği", example = "1")
        Long id,

        @Schema(description = "Hakkında görüşülen ilanın kimliği", example = "1")
        Long listingId,

        @Schema(description = "İlan başlığı", example = "500 kg PET plastik atığı")
        String listingTitle,

        @Schema(description = "Karşı tarafın firma kimliği", example = "2")
        Long counterpartCompanyId,

        @Schema(description = "Karşı tarafın firma adı", example = "Yeşil Dönüşüm Ltd.")
        String counterpartCompanyName,

        @Schema(description = "Görüşmenin başladığı tarih")
        Instant createdAt
) {
}
