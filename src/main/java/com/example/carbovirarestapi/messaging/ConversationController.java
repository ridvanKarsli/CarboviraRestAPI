package com.example.carbovirarestapi.messaging;

import com.example.carbovirarestapi.common.dto.PageResponse;
import com.example.carbovirarestapi.common.exception.ApiError;
import com.example.carbovirarestapi.messaging.dto.ConversationResponse;
import com.example.carbovirarestapi.messaging.dto.ConversationStartRequest;
import com.example.carbovirarestapi.messaging.dto.MessageResponse;
import com.example.carbovirarestapi.messaging.dto.MessageSendRequest;
import com.example.carbovirarestapi.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Tag(name = "Messaging", description = "İlan üzerinden firmalar arası görüşme ve mesajlaşma (token gerekli)")
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    @Operation(
            summary = "Görüşme başlat / devam ettir",
            description = "Bir ilan hakkında ilk mesajı gönderir. Aynı firma aynı ilan için daha önce görüşme başlatmışsa, "
                    + "yeni bir görüşme açmak yerine mevcut görüşmeye mesaj eklenir. Kendi ilanınıza mesaj gönderemezsiniz."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Görüşme (yeni veya mevcut) ve gönderilen mesaj"),
            @ApiResponse(responseCode = "400", description = "Doğrulama hatası veya kendi ilanınıza mesaj gönderme denemesi",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "İlan bulunamadı",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class)))
    })
    public ConversationResponse startOrGet(@AuthenticationPrincipal UserPrincipal principal,
                                            @Valid @RequestBody ConversationStartRequest request) {
        return conversationService.startOrGet(principal.getId(), principal.getCompanyId(), request);
    }

    @GetMapping
    @Operation(summary = "Görüşmelerim", description = "Giriş yapan kullanıcının firmasının taraf olduğu (başlattığı veya kendisine ulaşılan) tüm görüşmeler.")
    public PageResponse<ConversationResponse> getMyConversations(@AuthenticationPrincipal UserPrincipal principal,
                                                                   @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(conversationService.getMyConversations(principal.getCompanyId(), pageable));
    }

    @GetMapping("/{id}/messages")
    @Operation(summary = "Görüşmedeki mesajlar", description = "Sadece görüşmenin tarafı olan firmalar erişebilir.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mesaj listesi"),
            @ApiResponse(responseCode = "403", description = "Bu görüşmenin tarafı değilsiniz",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Görüşme bulunamadı",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class)))
    })
    public PageResponse<MessageResponse> getMessages(@AuthenticationPrincipal UserPrincipal principal,
                                                       @Parameter(description = "Görüşme kimliği", example = "1") @PathVariable Long id,
                                                       @PageableDefault(size = 50) Pageable pageable) {
        return PageResponse.from(conversationService.getMessages(principal.getCompanyId(), id, pageable));
    }

    @PostMapping("/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Görüşmeye mesaj gönder", description = "Sadece görüşmenin tarafı olan firmalar mesaj gönderebilir.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mesaj gönderildi"),
            @ApiResponse(responseCode = "400", description = "Doğrulama hatası",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Bu görüşmenin tarafı değilsiniz",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Görüşme bulunamadı",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class)))
    })
    public MessageResponse sendMessage(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable Long id,
                                        @Valid @RequestBody MessageSendRequest request) {
        return conversationService.sendMessage(principal.getId(), principal.getCompanyId(), id, request);
    }
}
