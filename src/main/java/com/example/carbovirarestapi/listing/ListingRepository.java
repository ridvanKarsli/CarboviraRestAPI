package com.example.carbovirarestapi.listing;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** JpaSpecificationExecutor sayesinde arama filtreleri için ayrı ayrı finder metodu yazmıyoruz, bkz. ListingSpecifications. */
public interface ListingRepository extends JpaRepository<Listing, Long>, JpaSpecificationExecutor<Listing> {

    Page<Listing> findByCompanyId(Long companyId, Pageable pageable);
}
