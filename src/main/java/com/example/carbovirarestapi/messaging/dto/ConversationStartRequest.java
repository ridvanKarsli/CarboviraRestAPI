package com.example.carbovirarestapi.messaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Bir ilan üzerinden görüşme başlatma isteği (ilk mesajla birlikte)")
public record ConversationStartRequest(

        @Schema(description = "Hakkında görüşme başlatılacak ilanın kimliği", example = "1")
        @NotNull(message = "İlan kimliği boş olamaz")
        Long listingId,

        @Schema(description = "İlk mesaj", example = "Merhaba, bu ilandaki PET atığın haftalık ne kadarını tedarik edebilirsiniz?")
        @NotBlank(message = "Mesaj boş olamaz")
        @Size(max = 4000, message = "Mesaj en fazla 4000 karakter olabilir")
        String message
) {
}
