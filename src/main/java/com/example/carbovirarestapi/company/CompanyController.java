package com.example.carbovirarestapi.company;

import com.example.carbovirarestapi.common.exception.ApiError;
import com.example.carbovirarestapi.company.dto.CompanyResponse;
import com.example.carbovirarestapi.company.dto.CompanyUpdateRequest;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name = "Company", description = "Firma profili işlemleri (token gerekli)")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/me")
    @Operation(summary = "Kendi firma profilimi getir", description = "Token sahibi kullanıcının bağlı olduğu firmanın profilini döner.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Firma profili"),
            @ApiResponse(responseCode = "401", description = "Token eksik/geçersiz",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class)))
    })
    public CompanyResponse getCurrentCompany(@AuthenticationPrincipal UserPrincipal principal) {
        return companyService.getCurrentCompany(principal.getCompanyId());
    }

    @PutMapping("/me")
    @Operation(
            summary = "Kendi firma profilimi güncelle",
            description = "Firma adı, sektör, şehir, adres ve açıklama güncellenebilir. Vergi numarası kimlik bilgisi olduğu için bu uçtan değiştirilemez."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Güncellenmiş firma profili"),
            @ApiResponse(responseCode = "400", description = "Doğrulama hatası",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Token eksik/geçersiz",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class)))
    })
    public CompanyResponse updateCurrentCompany(@AuthenticationPrincipal UserPrincipal principal,
                                                 @Valid @RequestBody CompanyUpdateRequest request) {
        return companyService.updateCurrentCompany(principal.getCompanyId(), request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Herkese açık firma profili", description = "Başka bir firmanın kimliğini vererek genel profil bilgisine erişir (ilan sahibiyle iletişim öncesi firma kontrolü için).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Firma profili"),
            @ApiResponse(responseCode = "401", description = "Token eksik/geçersiz",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Firma bulunamadı",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class)))
    })
    public CompanyResponse getCompany(@Parameter(description = "Firma kimliği", example = "1") @PathVariable Long id) {
        return companyService.getById(id);
    }
}
