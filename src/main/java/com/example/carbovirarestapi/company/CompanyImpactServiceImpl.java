package com.example.carbovirarestapi.company;

import com.example.carbovirarestapi.common.CarbonEstimator;
import com.example.carbovirarestapi.company.dto.CompanyImpactReportResponse;
import com.example.carbovirarestapi.listing.Listing;
import com.example.carbovirarestapi.listing.ListingRepository;
import com.example.carbovirarestapi.listing.ListingStatus;
import com.example.carbovirarestapi.messaging.Conversation;
import com.example.carbovirarestapi.messaging.ConversationRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
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
    public CompanyImpactReportResponse generate(Long companyId, Instant from, Instant to) {
        List<Listing> listings = listingRepository.findByCompanyId(companyId, Pageable.unpaged()).getContent();

        long active = listings.stream().filter(l -> l.getStatus() == ListingStatus.ACTIVE).count();
        long archived = listings.stream().filter(l -> l.getStatus() == ListingStatus.ARCHIVED).count();

        Map<String, BigDecimal> quantityByUnit = listings.stream()
                .collect(Collectors.groupingBy(
                        Listing::getUnit,
                        Collectors.reducing(BigDecimal.ZERO, Listing::getQuantity, BigDecimal::add)));

        List<Conversation> conversations = conversationRepository
                .findAllInvolvingCompany(companyId, Pageable.unpaged())
                .getContent();

        BigDecimal co2FromSelling = co2FromSelling(listings, from, to);
        BigDecimal co2FromBuying = co2FromBuying(companyId, conversations, from, to);

        return new CompanyImpactReportResponse(
                listings.size(),
                active,
                archived,
                quantityByUnit,
                conversations.size(),
                from,
                to,
                co2FromSelling,
                co2FromBuying,
                co2FromSelling.add(co2FromBuying));
    }

    // Satış tarafı: kendi ilanlarımdan, dönem içinde arşivlenmiş (yani el değiştirmiş sayılan) olanlar.
    // Arşivlenme anını ayrı bir alanla tutmuyoruz, updatedAt'i bu amaçla proxy olarak kullanıyorum.
    private BigDecimal co2FromSelling(List<Listing> listings, Instant from, Instant to) {
        return listings.stream()
                .filter(l -> l.getStatus() == ListingStatus.ARCHIVED)
                .filter(l -> withinPeriod(l.getUpdatedAt(), from, to))
                .map(l -> CarbonEstimator.estimateCo2SavedKg(l.getCategory(), l.getQuantity(), l.getUnit()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Alış tarafı: benim başlattığım görüşmeler üzerinden, karşı tarafın ilanı arşivlenmişse
    // (satış tamamlanmış sayılır) o ilanı alım olarak sayıyorum. Aynı ilana birden fazla görüşme
    // açılamadığı için (bkz. ConversationRepository) burada ekstra dedupe teorik olarak gerekmiyor,
    // yine de listing id'sine göre map'e toplayıp garantiye alıyorum.
    private BigDecimal co2FromBuying(Long companyId, List<Conversation> conversations, Instant from, Instant to) {
        Map<Long, Listing> purchasedListings = new LinkedHashMap<>();
        for (Conversation conversation : conversations) {
            boolean initiatedByMe = conversation.getInitiatorCompany().getId().equals(companyId);
            if (!initiatedByMe || !withinPeriod(conversation.getCreatedAt(), from, to)) {
                continue;
            }
            Listing listing = conversation.getListing();
            if (listing.getStatus() == ListingStatus.ARCHIVED) {
                purchasedListings.put(listing.getId(), listing);
            }
        }
        return purchasedListings.values().stream()
                .map(l -> CarbonEstimator.estimateCo2SavedKg(l.getCategory(), l.getQuantity(), l.getUnit()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean withinPeriod(Instant instant, Instant from, Instant to) {
        if (from != null && instant.isBefore(from)) {
            return false;
        }
        return to == null || !instant.isAfter(to);
    }
}
