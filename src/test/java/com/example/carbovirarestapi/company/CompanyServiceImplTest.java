package com.example.carbovirarestapi.company;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.carbovirarestapi.common.exception.ResourceNotFoundException;
import com.example.carbovirarestapi.company.dto.CompanyResponse;
import com.example.carbovirarestapi.company.dto.CompanyUpdateRequest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CompanyMapper, mock yerine MapStruct'ın gerçek (derlenmiş) implementasyonu ile
 * kullanılır — böylece dönüşüm kurallarındaki bir hata test tarafından da yakalanır.
 */
@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;

    private CompanyServiceImpl companyService;

    @BeforeEach
    void setUp() {
        CompanyMapper companyMapper = Mappers.getMapper(CompanyMapper.class);
        companyService = new CompanyServiceImpl(companyRepository, companyMapper);
    }

    @Test
    void getById_returnsCompany_whenFound() {
        Company company = Company.builder().name("Acme").taxNumber("111").city("İstanbul").build();
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        CompanyResponse response = companyService.getById(1L);

        assertThat(response.name()).isEqualTo("Acme");
        assertThat(response.city()).isEqualTo("İstanbul");
    }

    @Test
    void getById_throwsResourceNotFoundException_whenMissing() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCurrentCompany_appliesChanges_whenCompanyExists() {
        Company company = Company.builder().name("Eski Ad").taxNumber("111").build();
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        CompanyUpdateRequest request = new CompanyUpdateRequest("Yeni Ad", "Plastik", "Ankara", "Adres", "Açıklama");
        CompanyResponse response = companyService.updateCurrentCompany(1L, request);

        assertThat(response.name()).isEqualTo("Yeni Ad");
        assertThat(response.city()).isEqualTo("Ankara");
        assertThat(response.taxNumber()).isEqualTo("111"); // vergi no bu uçtan değişmez
    }
}
