package com.example.carbovirarestapi.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Sayfalanmış tüm uç noktalar için ortak, kararlı JSON şekli.
 * Spring Data'nın {@link Page}/{@code PageImpl} tipini doğrudan HTTP sınırının dışına
 * sızdırmamak amacıyla kullanılır — böylece API sözleşmesi, Spring Data'nın iç
 * serileştirme davranışındaki değişikliklerden bağımsız kalır.
 */
@Schema(description = "Sayfalanmış liste yanıtı")
public record PageResponse<T>(
        @Schema(description = "Bu sayfadaki öğeler")
        List<T> content,

        @Schema(description = "Sayfa numarası (0 tabanlı)", example = "0")
        int page,

        @Schema(description = "Sayfa başına öğe sayısı", example = "20")
        int size,

        @Schema(description = "Toplam öğe sayısı", example = "42")
        long totalElements,

        @Schema(description = "Toplam sayfa sayısı", example = "3")
        int totalPages,

        @Schema(description = "Son sayfa mı")
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
