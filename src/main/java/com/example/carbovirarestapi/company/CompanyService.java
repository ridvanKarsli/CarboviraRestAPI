package com.example.carbovirarestapi.company;

import com.example.carbovirarestapi.company.dto.CompanyResponse;
import com.example.carbovirarestapi.company.dto.CompanyUpdateRequest;

/** Firma profili işlemleri. */
public interface CompanyService {

    CompanyResponse getById(Long id);

    CompanyResponse getCurrentCompany(Long companyId);

    CompanyResponse updateCurrentCompany(Long companyId, CompanyUpdateRequest request);
}
