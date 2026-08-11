package com.example.carbovirarestapi.company;

import com.example.carbovirarestapi.company.dto.CompanyResponse;
import com.example.carbovirarestapi.company.dto.CompanyUpdateRequest;

/**
 * Firma profili iş kurallarının sözleşmesi. Controller bu arayüze bağımlıdır,
 * somut implementasyona değil (Dependency Inversion Principle) — böylece
 * implementasyon testlerde kolayca sahtelenebilir/değiştirilebilir.
 */
public interface CompanyService {

    CompanyResponse getById(Long id);

    CompanyResponse getCurrentCompany(Long companyId);

    CompanyResponse updateCurrentCompany(Long companyId, CompanyUpdateRequest request);
}
