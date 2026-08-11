package com.example.carbovirarestapi.company;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    boolean existsByTaxNumber(String taxNumber);

    Page<Company> findByVerified(boolean verified, Pageable pageable);
}
