package com.example.carbovirarestapi.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.carbovirarestapi.common.exception.ResourceNotFoundException;
import com.example.carbovirarestapi.company.Company;
import com.example.carbovirarestapi.company.CompanyMapper;
import com.example.carbovirarestapi.company.CompanyRepository;
import com.example.carbovirarestapi.company.dto.CompanyResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AdminCompanyServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;

    private AdminCompanyServiceImpl adminCompanyService;

    @BeforeEach
    void setUp() {
        CompanyMapper companyMapper = Mappers.getMapper(CompanyMapper.class);
        adminCompanyService = new AdminCompanyServiceImpl(companyRepository, companyMapper);
    }

    @Test
    void list_usesVerifiedFilter_whenProvided() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Company> page = new PageImpl<>(java.util.List.of(
                Company.builder().name("Acme").taxNumber("111").verified(false).build()));
        when(companyRepository.findByVerified(false, pageable)).thenReturn(page);

        Page<CompanyResponse> result = adminCompanyService.list(false, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).verified()).isFalse();
    }

    @Test
    void list_returnsAllCompanies_whenFilterNotProvided() {
        Pageable pageable = PageRequest.of(0, 20);
        when(companyRepository.findAll(pageable)).thenReturn(Page.empty());

        adminCompanyService.list(null, pageable);

        verify(companyRepository).findAll(pageable);
        verify(companyRepository, never()).findByVerified(org.mockito.ArgumentMatchers.anyBoolean(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void verify_setsVerifiedTrue_whenCompanyExists() {
        Company company = Company.builder().name("Acme").taxNumber("111").verified(false).build();
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        CompanyResponse response = adminCompanyService.verify(1L);

        assertThat(response.verified()).isTrue();
        assertThat(company.isVerified()).isTrue();
    }

    @Test
    void verify_throwsResourceNotFoundException_whenCompanyMissing() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminCompanyService.verify(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
