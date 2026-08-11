package com.example.carbovirarestapi.listing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.carbovirarestapi.common.exception.BusinessRuleViolationException;
import com.example.carbovirarestapi.common.exception.ResourceNotFoundException;
import com.example.carbovirarestapi.company.Company;
import com.example.carbovirarestapi.company.CompanyRepository;
import com.example.carbovirarestapi.listing.dto.ListingCreateRequest;
import com.example.carbovirarestapi.listing.dto.ListingResponse;
import com.example.carbovirarestapi.listing.dto.ListingUpdateRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class ListingServiceImplTest {

    private static final Long OWNER_COMPANY_ID = 1L;
    private static final Long OTHER_COMPANY_ID = 2L;

    @Mock
    private ListingRepository listingRepository;
    @Mock
    private CompanyRepository companyRepository;

    private ListingServiceImpl listingService;

    @BeforeEach
    void setUp() {
        ListingMapper listingMapper = Mappers.getMapper(ListingMapper.class);
        listingService = new ListingServiceImpl(listingRepository, companyRepository, listingMapper);
    }

    @Test
    void create_buildsActiveListing_forOwningCompany() {
        Company company = Company.builder().name("Acme").taxNumber("111").build();
        when(companyRepository.getReferenceById(OWNER_COMPANY_ID)).thenReturn(company);

        ListingCreateRequest request = new ListingCreateRequest(
                ListingType.WASTE, "500 kg PET", "Plastik", "Temiz PET atığı",
                BigDecimal.valueOf(500), "kg", "İstanbul", null, null, null);

        ListingResponse response = listingService.create(OWNER_COMPANY_ID, request);

        assertThat(response.status()).isEqualTo(ListingStatus.ACTIVE);
        assertThat(response.title()).isEqualTo("500 kg PET");
        assertThat(response.companyName()).isEqualTo("Acme");
        verify(listingRepository).save(any(Listing.class));
    }

    @Test
    void getById_throwsResourceNotFoundException_whenMissing() {
        when(listingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_appliesChanges_whenCallerOwnsListing() {
        Listing listing = activeListingOwnedBy(OWNER_COMPANY_ID);
        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));

        ListingUpdateRequest request = new ListingUpdateRequest(
                "Yeni başlık", "Metal", "Güncel açıklama", BigDecimal.TEN, "ton", "Ankara", BigDecimal.valueOf(100),
                null, null);

        ListingResponse response = listingService.update(OWNER_COMPANY_ID, 10L, request);

        assertThat(response.title()).isEqualTo("Yeni başlık");
        assertThat(response.city()).isEqualTo("Ankara");
        // type ve status bu uçtan değişmez:
        assertThat(response.type()).isEqualTo(ListingType.WASTE);
        assertThat(response.status()).isEqualTo(ListingStatus.ACTIVE);
    }

    @Test
    void update_throwsAccessDeniedException_whenCallerDoesNotOwnListing() {
        Listing listing = activeListingOwnedBy(OWNER_COMPANY_ID);
        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));

        ListingUpdateRequest request = new ListingUpdateRequest(
                "Başlık", "Metal", "Açıklama", BigDecimal.TEN, "ton", "Ankara", null, null, null);

        assertThatThrownBy(() -> listingService.update(OTHER_COMPANY_ID, 10L, request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void updateStatus_throwsAccessDeniedException_whenCallerDoesNotOwnListing() {
        Listing listing = activeListingOwnedBy(OWNER_COMPANY_ID);
        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> listingService.updateStatus(OTHER_COMPANY_ID, 10L, ListingStatus.PASSIVE))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void delete_removesListing_whenCallerOwnsListing() {
        Listing listing = activeListingOwnedBy(OWNER_COMPANY_ID);
        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));

        listingService.delete(OWNER_COMPANY_ID, 10L);

        verify(listingRepository).delete(listing);
    }

    @Test
    void delete_throwsAccessDeniedException_whenCallerDoesNotOwnListing() {
        Listing listing = activeListingOwnedBy(OWNER_COMPANY_ID);
        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> listingService.delete(OTHER_COMPANY_ID, 10L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void searchNearby_throwsBusinessRuleViolationException_whenCallerHasNoLocation() {
        Company caller = companyWithId(OWNER_COMPANY_ID);
        when(companyRepository.findById(OWNER_COMPANY_ID)).thenReturn(Optional.of(caller));

        assertThatThrownBy(() -> listingService.searchNearby(OWNER_COMPANY_ID, 50, PageRequest.of(0, 20)))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void searchNearby_filtersByRadiusAndSortsByDistance() {
        // İstanbul'da bir çağıran; biri hemen yanında (Kadıköy), biri çok uzakta (Ankara) iki ilan.
        Company caller = companyWithId(OWNER_COMPANY_ID);
        caller.setLatitude(41.0082);
        caller.setLongitude(28.9784);
        when(companyRepository.findById(OWNER_COMPANY_ID)).thenReturn(Optional.of(caller));

        Company nearCompany = companyWithId(2L);
        nearCompany.setLatitude(40.9909);
        nearCompany.setLongitude(29.0304);
        Listing nearListing = activeListingOwnedBy(nearCompany, "Yakın ilan");

        Company farCompany = companyWithId(3L);
        farCompany.setLatitude(39.9334);
        farCompany.setLongitude(32.8597);
        Listing farListing = activeListingOwnedBy(farCompany, "Uzak ilan");

        when(listingRepository.findActiveWithCompanyLocation(ListingStatus.ACTIVE))
                .thenReturn(List.of(farListing, nearListing));

        Page<ListingResponse> result = listingService.searchNearby(OWNER_COMPANY_ID, 50, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Yakın ilan");
    }

    private Listing activeListingOwnedBy(Company company, String title) {
        return Listing.builder()
                .type(ListingType.WASTE)
                .title(title)
                .category("Plastik")
                .description("Açıklama")
                .quantity(BigDecimal.valueOf(100))
                .unit("kg")
                .city("İstanbul")
                .status(ListingStatus.ACTIVE)
                .company(company)
                .build();
    }

    private Listing activeListingOwnedBy(Long companyId) {
        Company company = companyWithId(companyId);
        return Listing.builder()
                .type(ListingType.WASTE)
                .title("Eski başlık")
                .category("Plastik")
                .description("Eski açıklama")
                .quantity(BigDecimal.valueOf(100))
                .unit("kg")
                .city("İstanbul")
                .status(ListingStatus.ACTIVE)
                .company(company)
                .build();
    }

    /** Company.id, BaseEntity üzerinde private/generated olduğundan test için yansıma (reflection) ile atanır. */
    private Company companyWithId(Long id) {
        Company company = Company.builder().name("Acme").taxNumber("111").build();
        try {
            var idField = com.example.carbovirarestapi.common.entity.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(company, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        return company;
    }
}
