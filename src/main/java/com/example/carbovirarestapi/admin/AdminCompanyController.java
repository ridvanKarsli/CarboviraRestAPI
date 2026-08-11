package com.example.carbovirarestapi.admin;

import com.example.carbovirarestapi.common.dto.PageResponse;
import com.example.carbovirarestapi.common.exception.ApiError;
import com.example.carbovirarestapi.company.dto.CompanyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Onay şu an için sadece bilgilendirme amaçlı; verified=false firmaların ilan vermesini engellemiyor. */
@RestController
@RequestMapping("/api/admin/companies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
@Tag(name = "Admin", description = "Platform yönetimi — firma onayı (sadece PLATFORM_ADMIN)")
public class AdminCompanyController {

    private final AdminCompanyService adminCompanyService;

    @GetMapping
    @Operation(summary = "Firmaları listele", description = "verified parametresi verilmezse tüm firmalar, verilirse sadece o duruma sahip firmalar döner.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Firma listesi"),
            @ApiResponse(responseCode = "403", description = "PLATFORM_ADMIN rolü gerekli",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class)))
    })
    public PageResponse<CompanyResponse> list(
            @Parameter(description = "Onay durumu filtresi (boş = tümü)", example = "false") @RequestParam(required = false) Boolean verified,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(adminCompanyService.list(verified, pageable));
    }

    @PatchMapping("/{id}/verify")
    @Operation(summary = "Firmayı onayla", description = "Firmanın verified alanını true yapar. Zaten onaylıysa değişiklik yapılmaz.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Onaylandı"),
            @ApiResponse(responseCode = "403", description = "PLATFORM_ADMIN rolü gerekli",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Firma bulunamadı",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class)))
    })
    public CompanyResponse verify(@Parameter(description = "Firma kimliği", example = "1") @PathVariable Long id) {
        return adminCompanyService.verify(id);
    }
}
