package com.example.carbovirarestapi.admin;

import com.example.carbovirarestapi.common.exception.ResourceNotFoundException;
import com.example.carbovirarestapi.company.Company;
import com.example.carbovirarestapi.company.CompanyMapper;
import com.example.carbovirarestapi.company.CompanyRepository;
import com.example.carbovirarestapi.company.dto.CompanyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCompanyServiceImpl implements AdminCompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    @Override
    public Page<CompanyResponse> list(Boolean verified, Pageable pageable) {
        Page<Company> page = verified != null
                ? companyRepository.findByVerified(verified, pageable)
                : companyRepository.findAll(pageable);
        return page.map(companyMapper::toResponse);
    }

    @Override
    @Transactional
    public CompanyResponse verify(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Firma bulunamadı: id=" + companyId));
        company.setVerified(true);
        return companyMapper.toResponse(company);
    }
}
