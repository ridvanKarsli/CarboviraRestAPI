package com.example.carbovirarestapi.listing;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * İlan aramasındaki filtrelerin (hepsi opsiyonel) tek bir sorguda birleştirilmesinden
 * sorumludur. Servis katmanının JPA Criteria API detaylarını bilmesine gerek kalmaz (SRP).
 * <p>
 * Büyük/küçük harf duyarsız karşılaştırmalarda, Türkçe "İ/I" gibi karakterlerin Java
 * tarafındaki {@code String.toLowerCase(Locale)} ile veritabanının {@code LOWER()} SQL
 * fonksiyonu arasında farklı sonuç üretebilmesi ("Türkçe I problemi") riskine karşı,
 * karşılaştırmanın HER İKİ tarafı da bilerek aynı {@code cb.lower(...)} (SQL LOWER())
 * üzerinden geçirilir; Java tarafında ayrıca String.toLowerCase() çağrılmaz.
 */
public final class ListingSpecifications {

    private ListingSpecifications() {
    }

    public static Specification<Listing> search(ListingStatus status, ListingType type, String category,
                                                  String city, String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(cb.lower(root.get("category")), cb.lower(cb.literal(category))));
            }
            if (StringUtils.hasText(city)) {
                predicates.add(cb.equal(cb.lower(root.get("city")), cb.lower(cb.literal(city))));
            }
            if (StringUtils.hasText(keyword)) {
                String likePattern = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), cb.lower(cb.literal(likePattern))),
                        cb.like(cb.lower(root.get("description")), cb.lower(cb.literal(likePattern)))
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
