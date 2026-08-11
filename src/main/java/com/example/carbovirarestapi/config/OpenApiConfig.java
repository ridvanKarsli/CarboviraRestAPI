package com.example.carbovirarestapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger/OpenAPI dokümantasyonu ve JWT bearer şeması tanımı (/swagger-ui.html). */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI carboviraOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Carbovira API")
                        .description("""
                                Firmaların atık ve hammadde ilanları üzerinden eşleştiği endüstriyel simbiyoz platformu.

                                **Nasıl kullanılır:**
                                1. `POST /api/auth/register` ile firma + kullanıcı oluştur, dönen `token`'ı kopyala.
                                2. Sağ üstteki **Authorize** butonuna tıkla, `Bearer <token>` yerine sadece token'ı yapıştır.
                                3. Artık `/api/companies/**` gibi korumalı uçları deneyebilirsin.
                                """)
                        .version("v1")
                        .contact(new Contact()
                                .name("Carbovira")
                                .email("rdvn35050@gmail.com")))
                .tags(List.of(
                        new Tag().name("Auth").description("Firma + kullanıcı kaydı ve giriş işlemleri"),
                        new Tag().name("Company").description("Firma profili işlemleri"),
                        new Tag().name("Listing").description("Atık/hammadde ilanları"),
                        new Tag().name("Messaging").description("İlan üzerinden firmalar arası görüşme ve mesajlaşma"),
                        new Tag().name("Admin").description("Platform yönetimi (sadece PLATFORM_ADMIN)")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                .name(BEARER_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Giriş/kayıt sonrası dönen token. Sadece token değerini girin, 'Bearer ' önekini Swagger otomatik ekler.")));
    }
}
