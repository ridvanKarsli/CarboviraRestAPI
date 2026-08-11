package com.example.carbovirarestapi.messaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Görüşme içindeki tek bir mesaj")
public record MessageResponse(

        @Schema(description = "Mesaj kimliği", example = "1")
        Long id,

        @Schema(description = "Ait olduğu görüşmenin kimliği", example = "1")
        Long conversationId,

        @Schema(description = "Gönderen kullanıcının kimliği", example = "1")
        Long senderId,

        @Schema(description = "Gönderen kullanıcının adı soyadı", example = "Ayşe Yılmaz")
        String senderName,

        @Schema(description = "Gönderenin firma kimliği", example = "1")
        Long senderCompanyId,

        @Schema(description = "Gönderenin firma adı", example = "Acme Geri Dönüşüm A.Ş.")
        String senderCompanyName,

        @Schema(description = "Mesaj içeriği")
        String content,

        @Schema(description = "Gönderim zamanı")
        Instant sentAt
) {
}
