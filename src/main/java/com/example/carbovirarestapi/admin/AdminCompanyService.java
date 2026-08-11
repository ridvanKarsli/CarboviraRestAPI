package com.example.carbovirarestapi.admin;

import com.example.carbovirarestapi.company.dto.CompanyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Platform yönetimi (PLATFORM_ADMIN) için firma moderasyon işlemlerinin sözleşmesi (DIP). */
public interface AdminCompanyService {

    /** verified null ise tüm firmalar, değilse sadece o duruma sahip firmalar döner. */
    Page<CompanyResponse> list(Boolean verified, Pageable pageable);

    CompanyResponse verify(Long companyId);
}
