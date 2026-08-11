package com.example.carbovirarestapi.listing;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * {@link JpaSpecificationExecutor}, arama uçlarındaki değişken sayıdaki
 * filtrenin (tip/kategori/şehir/anahtar kelime) her kombinasyonu için ayrı
 * bir finder metodu yazmak yerine, çalışma zamanında birleştirilebilir
 * {@link org.springframework.data.jpa.domain.Specification} kullanılmasını sağlar.
 */
public interface ListingRepository extends JpaRepository<Listing, Long>, JpaSpecificationExecutor<Listing> {

    Page<Listing> findByCompanyId(Long companyId, Pageable pageable);
}
