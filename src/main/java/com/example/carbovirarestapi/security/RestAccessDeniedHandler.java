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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * SecurityConfig'teki URL bazlı kurallardan (örn. /api/admin/**) gelen AccessDeniedException
 * DispatcherServlet'e hiç girmediği için GlobalExceptionHandler'a uğramıyor, Spring'in
 * varsayılan HTML hata sayfasına düşüyordu. Bunu da RestAuthenticationEntryPoint gibi
 * ApiError formatında JSON'a çeviriyoruz.
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectProvider<ObjectMapper> objectMapperProvider) {
        // Aynı sebeple RestAuthenticationEntryPoint'teki gibi: cıplak ObjectMapper Instant'ı
        // serialize edemiyor, findAndRegisterModules ile jsr310 modülünü de yükletiyoruz.
        this.objectMapper = objectMapperProvider.getIfAvailable(() -> new ObjectMapper().findAndRegisterModules());
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        ApiError error = new ApiError(
                Instant.now(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "Bu işlem için yetkiniz yok.",
                request.getRequestURI(),
                null
        );
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
