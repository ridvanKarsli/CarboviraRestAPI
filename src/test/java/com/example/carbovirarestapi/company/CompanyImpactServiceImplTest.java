package com.example.carbovirarestapi.company;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.carbovirarestapi.company.dto.CompanyImpactReportResponse;
import com.example.carbovirarestapi.listing.Listing;
import com.example.carbovirarestapi.listing.ListingRepository;
import com.example.carbovirarestapi.listing.ListingStatus;
import com.example.carbovirarestapi.listing.ListingType;
import com.example.carbovirarestapi.messaging.Conversation;
import com.example.carbovirarestapi.messaging.ConversationRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CompanyImpactServiceImplTest {

    private static final Long COMPANY_ID = 1L;

    @Mock
    private ListingRepository listingRepository;
    @Mock
    private ConversationRepository conversationRepository;

    @InjectMocks
    private CompanyImpactServiceImpl companyImpactService;

    @Test
    void generate_summarizesListingsAndConversations() {
        Listing activeKg = listing(ListingStatus.ACTIVE, BigDecimal.valueOf(100), "kg");
        Listing activeKg2 = listing(ListingStatus.ACTIVE, BigDecimal.valueOf(50), "kg");
        Listing archivedTon = listing(ListingStatus.ARCHIVED, BigDecimal.valueOf(2), "ton");

        when(listingRepository.findByCompanyId(COMPANY_ID, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(activeKg, activeKg2, archivedTon)));

        Page<Conversation> conversations = new PageImpl<>(List.of(new Conversation(), new Conversation()));
        when(conversationRepository.findAllInvolvingCompany(COMPANY_ID, Pageable.unpaged()))
                .thenReturn(conversations);

        CompanyImpactReportResponse report = companyImpactService.generate(COMPANY_ID);

        assertThat(report.totalListings()).isEqualTo(3);
        assertThat(report.activeListings()).isEqualTo(2);
        assertThat(report.archivedListings()).isEqualTo(1);
        assertThat(report.totalQuantityByUnit()).containsEntry("kg", BigDecimal.valueOf(150))
                .containsEntry("ton", BigDecimal.valueOf(2));
        assertThat(report.totalConversations()).isEqualTo(2);
    }

    private Listing listing(ListingStatus status, BigDecimal quantity, String unit) {
        return Listing.builder()
                .type(ListingType.WASTE)
                .title("İlan")
                .category("Plastik")
                .quantity(quantity)
                .unit(unit)
                .city("İstanbul")
                .status(status)
                .build();
    }
}
