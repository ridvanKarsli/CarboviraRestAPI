package com.example.carbovirarestapi.messaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Mevcut bir görüşmeye mesaj gönderme isteği")
public record MessageSendRequest(

        @Schema(description = "Mesaj içeriği", example = "Aylık 2 ton kadar tedarik edebiliriz.")
        @NotBlank(message = "Mesaj boş olamaz")
        @Size(max = 4000, message = "Mesaj en fazla 4000 karakter olabilir")
        String content
) {
}
