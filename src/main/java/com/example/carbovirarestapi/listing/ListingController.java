package com.example.carbovirarestapi.listing;

import com.example.carbovirarestapi.common.dto.PageResponse;
import com.example.carbovirarestapi.common.exception.ApiError;
import com.example.carbovirarestapi.listing.dto.ListingCreateRequest;
import com.example.carbovirarestapi.listing.dto.ListingResponse;
import com.example.carbovirarestapi.listing.dto.ListingStatusUpdateRequest;
import com.example.carbovirarestapi.listing.dto.ListingUpdateRequest;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
@Tag(name = "Listing", description = "Atık/hammadde ilanları (token gerekli)")
public class ListingController {

    private static final String NOT_FOUND = "404";
    private static final String FORBIDDEN = "403";

    private final ListingService listingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Yeni ilan oluştur", description = "Giriş yapan kullanıcının firması adına atık veya hammadde ilanı oluşturur. Yeni ilan ACTIVE durumunda başlar.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "İlan oluşturuldu"),
            @ApiResponse(responseCode = "400", description = "Doğrulama hatası",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class)))
    })
    public ListingResponse create(@AuthenticationPrincipal UserPrincipal principal,
                                   @Valid @RequestBody ListingCreateRequest request) {
        return listingService.create(principal.getCompanyId(), request);
    }

    @GetMapping
    @Operation(
            summary = "İlanlarda ara",
            description = "Sadece ACTIVE durumundaki ilanlar arasında, verilen (hepsi opsiyonel) filtrelerle sayfalanmış arama yapar."
    )
    public PageResponse<ListingResponse> search(
            @Parameter(description = "İlan tipi") @RequestParam(required = false) ListingType type,
            @Parameter(description = "Kategori (tam eşleşme)", example = "Plastik") @RequestParam(required = false) String category,
            @Parameter(description = "Şehir (tam eşleşme)", example = "İstanbul") @RequestParam(required = false) String city,
            @Parameter(description = "Başlık/açıklama içinde anahtar kelime", example = "PET") @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(listingService.search(type, category, city, q, pageable));
    }

    @GetMapping("/mine")
    @Operation(summary = "Kendi ilanlarım", description = "Giriş yapan kullanıcının firmasına ait, durumu ne olursa olsun tüm ilanları döner.")
    public PageResponse<ListingResponse> getMine(@AuthenticationPrincipal UserPrincipal principal,
                                                  @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(listingService.getMine(principal.getCompanyId(), pageable));
    }

    @GetMapping("/nearby")
    @Operation(
            summary = "Yakındaki ilanlar",
            description = "Çağıran firmanın konumuna göre belirtilen yarıçap (km) içindeki ACTIVE ilanları "
                    + "en yakından en uzağa sıralar. Firma profilinde konum bilgisi (latitude/longitude) girilmiş olmalı."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "İlan listesi, en yakından en uzağa"),
            @ApiResponse(responseCode = "400", description = "Çağıran firmanın profilinde konum bilgisi yok",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class)))
    })
    public PageResponse<ListingResponse> searchNearby(@AuthenticationPrincipal UserPrincipal principal,
                                                        @Parameter(description = "Arama yarıçapı (km)", example = "50") @RequestParam double radiusKm,
                                                        @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(listingService.searchNearby(principal.getCompanyId(), radiusKm, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "İlan detayı")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "İlan bulundu"),
            @ApiResponse(responseCode = NOT_FOUND, description = "İlan bulunamadı",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class)))
    })
    public ListingResponse getById(@Parameter(description = "İlan kimliği", example = "1") @PathVariable Long id) {
        return listingService.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "İlanı güncelle", description = "Sadece ilan sahibi firma güncelleyebilir. Tip ve durum bu uçtan değiştirilemez.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Güncellendi"),
            @ApiResponse(responseCode = "400", description = "Doğrulama hatası",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = FORBIDDEN, description = "Bu ilan başka bir firmaya ait",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = NOT_FOUND, description = "İlan bulunamadı",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class)))
    })
    public ListingResponse update(@AuthenticationPrincipal UserPrincipal principal,
                                   @PathVariable Long id,
                                   @Valid @RequestBody ListingUpdateRequest request) {
        return listingService.update(principal.getCompanyId(), id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "İlan durumunu değiştir", description = "Örn. ACTIVE -> PASSIVE (aramadan gizle) veya ARCHIVED (kalıcı kapat). Sadece ilan sahibi firma değiştirebilir.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Durum güncellendi"),
            @ApiResponse(responseCode = FORBIDDEN, description = "Bu ilan başka bir firmaya ait",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = NOT_FOUND, description = "İlan bulunamadı",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class)))
    })
    public ListingResponse updateStatus(@AuthenticationPrincipal UserPrincipal principal,
                                         @PathVariable Long id,
                                         @Valid @RequestBody ListingStatusUpdateRequest request) {
        return listingService.updateStatus(principal.getCompanyId(), id, request.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "İlanı sil", description = "Sadece ilan sahibi firma silebilir.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Silindi"),
            @ApiResponse(responseCode = FORBIDDEN, description = "Bu ilan başka bir firmaya ait",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = NOT_FOUND, description = "İlan bulunamadı",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class)))
    })
    public void delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        listingService.delete(principal.getCompanyId(), id);
    }
}
