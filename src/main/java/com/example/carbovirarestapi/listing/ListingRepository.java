package com.example.carbovirarestapi.listing;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** JpaSpecificationExecutor sayesinde arama filtreleri için ayrı ayrı finder metodu yazmıyoruz, bkz. ListingSpecifications. */
public interface ListingRepository extends JpaRepository<Listing, Long>, JpaSpecificationExecutor<Listing> {

    Page<Listing> findByCompanyId(Long companyId, Pageable pageable);

    /** Yakınlık aramasının aday listesi: sadece konumu tanımlı firmaların ACTIVE ilanları.
     *  Mesafe hesabı veritabanında değil ListingServiceImpl.searchNearby()'de yapılıyor. */
    @Query("""
            select l from Listing l
            where l.status = :status
              and l.company.latitude is not null
              and l.company.longitude is not null
            """)
    List<Listing> findActiveWithCompanyLocation(@Param("status") ListingStatus status);
}
