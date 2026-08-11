package com.example.carbovirarestapi.auth;

import com.example.carbovirarestapi.auth.dto.AuthResponse;
import com.example.carbovirarestapi.auth.dto.LoginRequest;
import com.example.carbovirarestapi.auth.dto.RegisterRequest;
import com.example.carbovirarestapi.common.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Firma + kullanıcı kaydı ve giriş işlemleri (herkese açık, token gerekmez)")
@SecurityRequirements
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Firma ve ilk kullanıcı kaydı",
            description = "Yeni bir firma ile o firmanın COMPANY_ADMIN rolüne sahip ilk kullanıcısını tek adımda oluşturur ve giriş yapılmış sayılacak şekilde bir JWT döner."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Kayıt başarılı, token döndü"),
            @ApiResponse(responseCode = "400", description = "Doğrulama hatası (eksik/hatalı alan)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "E-posta veya vergi numarası zaten kayıtlı",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class)))
    })
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Giriş yap", description = "E-posta/şifre ile giriş yapar ve bir JWT döner.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Giriş başarılı, token döndü"),
            @ApiResponse(responseCode = "400", description = "Doğrulama hatası",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "E-posta veya şifre hatalı",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiError.class)))
    })
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
