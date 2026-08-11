package com.example.carbovirarestapi.company;

import com.example.carbovirarestapi.company.dto.CompanyImpactReportResponse;
import java.time.Instant;

/**
 * CompanyService'ten ayrı tuttum çünkü onun işi profil CRUD, bu ise listing ve messaging
 * modüllerini okuyan bağımsız bir raporlama sorgusu — ikisini aynı sınıfa koymak istemedim.
 */
public interface CompanyImpactService {

    /** from/to null verilirse ilgili taraf sınırlanmaz, yani tüm geçmiş dikkate alınır. */
    CompanyImpactReportResponse generate(Long companyId, Instant from, Instant to);
}
