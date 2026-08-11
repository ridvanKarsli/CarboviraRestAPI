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
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

        CompanyImpactReportResponse report = companyImpactService.generate(COMPANY_ID, null, null);

        assertThat(report.totalListings()).isEqualTo(3);
        assertThat(report.activeListings()).isEqualTo(2);
        assertThat(report.archivedListings()).isEqualTo(1);
        assertThat(report.totalQuantityByUnit()).containsEntry("kg", BigDecimal.valueOf(150))
                .containsEntry("ton", BigDecimal.valueOf(2));
        assertThat(report.totalConversations()).isEqualTo(2);
    }

    @Test
    void generate_calculatesCo2FromSellingAndBuying_withinPeriod() {
        Instant now = Instant.now();
        Instant periodStart = now.minus(30, ChronoUnit.DAYS);
        Instant periodEnd = now;

        // Kendi ilanım, dönem içinde arşivlenmiş metal atık — satış tarafına girmeli.
        Listing soldWithinPeriod = listingWithUpdatedAt(ListingStatus.ARCHIVED, BigDecimal.valueOf(100), "kg",
                "Metal Hurda", now.minus(5, ChronoUnit.DAYS));
        // Kendi ilanım ama dönemden çok önce arşivlenmiş — satış tarafına girmemeli.
        Listing soldOutsidePeriod = listingWithUpdatedAt(ListingStatus.ARCHIVED, BigDecimal.valueOf(200), "kg",
                "Metal Hurda", now.minus(90, ChronoUnit.DAYS));

        when(listingRepository.findByCompanyId(COMPANY_ID, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(soldWithinPeriod, soldOutsidePeriod)));

        // Başka firmanın arşivlenmiş ilanı, ben görüşme başlatmışım ve görüşme dönem içinde açılmış — alış tarafına girmeli.
        Listing purchased = listingWithId(99L, ListingStatus.ARCHIVED, BigDecimal.valueOf(50), "kg", "Plastik Atık");
        Conversation asBuyer = conversationBetween(COMPANY_ID, purchased, now.minus(2, ChronoUnit.DAYS));

        when(conversationRepository.findAllInvolvingCompany(COMPANY_ID, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(asBuyer)));

        CompanyImpactReportResponse report = companyImpactService.generate(COMPANY_ID, periodStart, periodEnd);

        // 100 kg metal * 2.0 = 200
        assertThat(report.co2SavedKgFromSelling()).isEqualByComparingTo(BigDecimal.valueOf(200));
        // 50 kg plastik * 1.8 = 90
        assertThat(report.co2SavedKgFromBuying()).isEqualByComparingTo(BigDecimal.valueOf(90));
        assertThat(report.co2SavedKgTotal()).isEqualByComparingTo(BigDecimal.valueOf(290));
        assertThat(report.periodFrom()).isEqualTo(periodStart);
        assertThat(report.periodTo()).isEqualTo(periodEnd);
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

    private Listing listingWithUpdatedAt(ListingStatus status, BigDecimal quantity, String unit, String category,
                                          Instant updatedAt) {
        Listing listing = Listing.builder()
                .type(ListingType.WASTE)
                .title("İlan")
                .category(category)
                .quantity(quantity)
                .unit(unit)
                .city("İstanbul")
                .status(status)
                .build();
        setInherited(listing, "updatedAt", updatedAt);
        return listing;
    }

    private Listing listingWithId(long id, ListingStatus status, BigDecimal quantity, String unit, String category) {
        Listing listing = listing(status, quantity, unit);
        listing.setCategory(category);
        setInherited(listing, "id", id);
        return listing;
    }

    private Conversation conversationBetween(Long initiatorCompanyId, Listing listing, Instant createdAt) {
        Conversation conversation = Conversation.builder()
                .listing(listing)
                .initiatorCompany(companyWithId(initiatorCompanyId))
                .build();
        setInherited(conversation, "createdAt", createdAt);
        return conversation;
    }

    private com.example.carbovirarestapi.company.Company companyWithId(Long id) {
        com.example.carbovirarestapi.company.Company company =
                com.example.carbovirarestapi.company.Company.builder().name("Acme").taxNumber("111").build();
        setInherited(company, "id", id);
        return company;
    }

    /** id/createdAt/updatedAt BaseEntity üzerinde private/generated olduğundan test için yansıma ile atanır. */
    private void setInherited(Object target, String fieldName, Object value) {
        try {
            Field field = com.example.carbovirarestapi.common.entity.BaseEntity.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
