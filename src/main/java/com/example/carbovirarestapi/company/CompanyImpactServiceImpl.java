package com.example.carbovirarestapi.company;

import com.example.carbovirarestapi.company.dto.CompanyImpactReportResponse;
import com.example.carbovirarestapi.listing.Listing;
import com.example.carbovirarestapi.listing.ListingRepository;
import com.example.carbovirarestapi.listing.ListingStatus;
import com.example.carbovirarestapi.messaging.ConversationRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyImpactServiceImpl implements CompanyImpactService {

    private final ListingRepository listingRepository;
    private final ConversationRepository conversationRepository;

    @Override
    public CompanyImpactReportResponse generate(Long companyId) {
        List<Listing> listings = listingRepository.findByCompanyId(companyId, Pageable.unpaged()).getContent();

        long active = listings.stream().filter(l -> l.getStatus() == ListingStatus.ACTIVE).count();
        long archived = listings.stream().filter(l -> l.getStatus() == ListingStatus.ARCHIVED).count();

        Map<String, BigDecimal> quantityByUnit = listings.stream()
                .collect(Collectors.groupingBy(
                        Listing::getUnit,
                        Collectors.reducing(BigDecimal.ZERO, Listing::getQuantity, BigDecimal::add)));

        long conversationCount = conversationRepository
                .findAllInvolvingCompany(companyId, Pageable.unpaged())
                .getTotalElements();

        return new CompanyImpactReportResponse(listings.size(), active, archived, quantityByUnit, conversationCount);
    }
}
