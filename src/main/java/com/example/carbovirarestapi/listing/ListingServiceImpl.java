package com.example.carbovirarestapi.listing;

import com.example.carbovirarestapi.common.exception.ResourceNotFoundException;
import com.example.carbovirarestapi.company.Company;
import com.example.carbovirarestapi.company.CompanyRepository;
import com.example.carbovirarestapi.listing.dto.ListingCreateRequest;
import com.example.carbovirarestapi.listing.dto.ListingResponse;
import com.example.carbovirarestapi.listing.dto.ListingUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListingServiceImpl implements ListingService {

    private final ListingRepository listingRepository;
    private final CompanyRepository companyRepository;
    private final ListingMapper listingMapper;

    @Override
    @Transactional
    public ListingResponse create(Long companyId, ListingCreateRequest request) {
        // getReferenceById: firma zaten kimlik doğrulamasından geçmiş kullanıcının firması
        // olduğundan var olduğu garanti; ekstra bir SELECT yerine lazy proxy kullanılır.
        Company company = companyRepository.getReferenceById(companyId);

        Listing listing = Listing.builder()
                .type(request.type())
                .title(request.title())
                .category(request.category())
                .description(request.description())
                .quantity(request.quantity())
                .unit(request.unit())
                .city(request.city())
                .price(request.price())
                .status(ListingStatus.ACTIVE)
                .company(company)
                .build();

        listingRepository.save(listing);
        return listingMapper.toResponse(listing);
    }

    @Override
    public ListingResponse getById(Long listingId) {
        return listingMapper.toResponse(findListingOrThrow(listingId));
    }

    @Override
    public Page<ListingResponse> search(ListingType type, String category, String city, String keyword, Pageable pageable) {
        var specification = ListingSpecifications.search(ListingStatus.ACTIVE, type, category, city, keyword);
        return listingRepository.findAll(specification, pageable).map(listingMapper::toResponse);
    }

    @Override
    public Page<ListingResponse> getMine(Long companyId, Pageable pageable) {
        return listingRepository.findByCompanyId(companyId, pageable).map(listingMapper::toResponse);
    }

    @Override
    @Transactional
    public ListingResponse update(Long companyId, Long listingId, ListingUpdateRequest request) {
        Listing listing = findOwnedListingOrThrow(companyId, listingId);
        listingMapper.updateEntity(request, listing);
        return listingMapper.toResponse(listing);
    }

    @Override
    @Transactional
    public ListingResponse updateStatus(Long companyId, Long listingId, ListingStatus status) {
        Listing listing = findOwnedListingOrThrow(companyId, listingId);
        listing.setStatus(status);
        return listingMapper.toResponse(listing);
    }

    @Override
    @Transactional
    public void delete(Long companyId, Long listingId) {
        Listing listing = findOwnedListingOrThrow(companyId, listingId);
        listingRepository.delete(listing);
    }

    private Listing findListingOrThrow(Long listingId) {
        return listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("İlan bulunamadı: id=" + listingId));
    }

    private Listing findOwnedListingOrThrow(Long companyId, Long listingId) {
        Listing listing = findListingOrThrow(listingId);
        if (!listing.getCompany().getId().equals(companyId)) {
            throw new AccessDeniedException("Bu ilan üzerinde işlem yapma yetkiniz yok.");
        }
        return listing;
    }
}
