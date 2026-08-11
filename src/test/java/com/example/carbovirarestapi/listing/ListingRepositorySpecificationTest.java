package com.example.carbovirarestapi.listing;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.carbovirarestapi.company.Company;
import com.example.carbovirarestapi.company.CompanyRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * ListingSpecifications'ın gerçek bir veritabanına (H2) karşı doğru filtrelediğini
 * doğrular. Criteria API gibi derleme zamanında tip güvenliği sınırlı olan kodlar
 * için gerçek bir sorgu çalıştırmak, salt unit test'ten daha güvenilir bir doğrulamadır.
 * <p>
 * Not: Dar kapsamlı @DataJpaTest yerine tam context (@SpringBootTest) kullanılır —
 * bu proje ortamında test-slice autoconfigure paketlerinin konumu (Boot 4 modülerleşmesiyle)
 * belirsiz olduğundan, zaten çalıştığı doğrulanmış olan tam context kurulumu tercih edildi.
 * Her test @Transactional sayesinde çalıştıktan sonra otomatik geri alınır (rollback).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ListingRepositorySpecificationTest {

    @Autowired
    private ListingRepository listingRepository;
    @Autowired
    private CompanyRepository companyRepository;

    @Test
    void search_filtersByStatusTypeCityAndKeyword() {
        Company company = companyRepository.save(Company.builder().name("Acme").taxNumber("111").build());

        listingRepository.save(listing(company, ListingType.WASTE, ListingStatus.ACTIVE, "İstanbul", "500 kg PET plastik atığı", "Plastik"));
        listingRepository.save(listing(company, ListingType.WASTE, ListingStatus.ACTIVE, "Ankara", "1 ton metal talaşı", "Metal"));
        listingRepository.save(listing(company, ListingType.WASTE, ListingStatus.PASSIVE, "İstanbul", "200 kg cam kırığı", "Cam"));

        Specification<Listing> specification =
                ListingSpecifications.search(ListingStatus.ACTIVE, ListingType.WASTE, null, "İstanbul", "PET");
        Page<Listing> result = listingRepository.findAll(specification, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).contains("PET");
    }

    @Test
    void search_excludesNonActiveListings_byDefault() {
        Company company = companyRepository.save(Company.builder().name("Acme").taxNumber("222").build());
        listingRepository.save(listing(company, ListingType.WASTE, ListingStatus.ARCHIVED, "İzmir", "Arşivlenmiş ilan", "Kağıt"));

        Specification<Listing> specification = ListingSpecifications.search(ListingStatus.ACTIVE, null, null, null, null);
        Page<Listing> result = listingRepository.findAll(specification, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void search_filtersByCategory_caseInsensitive() {
        Company company = companyRepository.save(Company.builder().name("Acme").taxNumber("333").build());
        listingRepository.save(listing(company, ListingType.RAW_MATERIAL, ListingStatus.ACTIVE, "Bursa", "500 kg geri dönüştürülmüş plastik granül", "plastik"));

        Specification<Listing> specification =
                ListingSpecifications.search(ListingStatus.ACTIVE, null, "Plastik", null, null);
        Page<Listing> result = listingRepository.findAll(specification, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    private Listing listing(Company company, ListingType type, ListingStatus status, String city, String title, String category) {
        return Listing.builder()
                .type(type)
                .title(title)
                .category(category)
                .description(title)
                .quantity(BigDecimal.TEN)
                .unit("kg")
                .city(city)
                .status(status)
                .company(company)
                .build();
    }
}
