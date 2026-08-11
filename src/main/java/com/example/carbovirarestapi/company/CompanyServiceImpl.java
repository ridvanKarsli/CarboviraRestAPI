package com.example.carbovirarestapi.company;

import com.example.carbovirarestapi.common.exception.ResourceNotFoundException;
import com.example.carbovirarestapi.company.dto.CompanyResponse;
import com.example.carbovirarestapi.company.dto.CompanyUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    @Override
    public CompanyResponse getById(Long id) {
        return companyMapper.toResponse(findCompanyOrThrow(id));
    }

    @Override
    public CompanyResponse getCurrentCompany(Long companyId) {
        return companyMapper.toResponse(findCompanyOrThrow(companyId));
    }

    @Override
    @Transactional
    public CompanyResponse updateCurrentCompany(Long companyId, CompanyUpdateRequest request) {
        Company company = findCompanyOrThrow(companyId);
        companyMapper.updateEntity(request, company);
        return companyMapper.toResponse(company);
    }

    private Company findCompanyOrThrow(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Firma bulunamadı: id=" + id));
    }
}
