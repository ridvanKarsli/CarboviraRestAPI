package com.example.carbovirarestapi.company;

import com.example.carbovirarestapi.company.dto.CompanyImpactReportResponse;

/**
 * CompanyService'ten ayrı tuttum çünkü onun işi profil CRUD, bu ise listing ve messaging
 * modüllerini okuyan bağımsız bir raporlama sorgusu — ikisini aynı sınıfa koymak istemedim.
 */
public interface CompanyImpactService {

    CompanyImpactReportResponse generate(Long companyId);
}
