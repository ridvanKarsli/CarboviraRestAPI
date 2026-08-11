package com.example.carbovirarestapi.security;

import com.example.carbovirarestapi.common.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Kimliksiz/geçersiz erişim denemelerinde GlobalExceptionHandler ile aynı ApiError formatını döner.
 * ObjectMapper bean'i context'te yoksa (bazı test slice'larında olmayabiliyor) kendi örneğini kurar.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectProvider<ObjectMapper> objectMapperProvider) {
        // findAndRegisterModules olmadan cıplak ObjectMapper, Instant gibi java.time tiplerini
        // serialize edemiyor (jackson-datatype-jsr310 module'u yüklenmemiş oluyor) — bunu farkına
        // varmadan önce ApiError.timestamp burada patlatıyordu.
        this.objectMapper = objectMapperProvider.getIfAvailable(() -> new ObjectMapper().findAndRegisterModules());
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        ApiError error = new ApiError(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Bu kaynağa erişmek için geçerli bir kimlik doğrulama token'ı gereklidir.",
                request.getRequestURI(),
                null
        );
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
