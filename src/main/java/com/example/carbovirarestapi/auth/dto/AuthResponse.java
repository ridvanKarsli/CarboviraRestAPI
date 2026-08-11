package com.example.carbovirarestapi.auth.dto;

import com.example.carbovirarestapi.user.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Başarılı kayıt/giriş sonrası dönen kimlik bilgisi ve JWT token")
public record AuthResponse(

        @Schema(description = "İstemcinin sonraki isteklerde 'Authorization: Bearer <token>' olarak kullanacağı JWT")
        String token,

        @Schema(description = "Kullanıcı kimliği", example = "1")
        Long userId,

        @Schema(description = "Kullanıcının e-posta adresi", example = "ayse@acme.com")
        String email,

        @Schema(description = "Kullanıcının adı soyadı", example = "Ayşe Yılmaz")
        String fullName,

        @Schema(description = "Kullanıcı rolü")
        Role role,

        @Schema(description = "Kullanıcının bağlı olduğu firmanın kimliği", example = "1")
        Long companyId,

        @Schema(description = "Firma adı", example = "Acme Geri Dönüşüm A.Ş.")
        String companyName
) {
}
