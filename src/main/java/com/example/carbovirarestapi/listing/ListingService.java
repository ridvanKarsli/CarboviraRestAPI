package com.example.carbovirarestapi.listing;

import com.example.carbovirarestapi.listing.dto.ListingCreateRequest;
import com.example.carbovirarestapi.listing.dto.ListingResponse;
import com.example.carbovirarestapi.listing.dto.ListingUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** İlan (atık/hammadde) işlemleri. */
public interface ListingService {

    ListingResponse create(Long companyId, ListingCreateRequest request);

    ListingResponse getById(Long listingId);

    /** Sadece ACTIVE ilanlar arasında, verilen (opsiyonel) filtrelerle arama yapar. */
    Page<ListingResponse> search(ListingType type, String category, String city, String keyword, Pageable pageable);

    /** Çağıran firmanın konumuna göre belirtilen yarıçap (km) içindeki ACTIVE ilanları, en yakından
     *  en uzağa sıralı döner. Çağıran firmanın profilinde konum (latitude/longitude) girilmiş olmalı. */
    Page<ListingResponse> searchNearby(Long callerCompanyId, double radiusKm, Pageable pageable);

    /** Çağıran firmanın durumu ne olursa olsun tüm kendi ilanları. */
    Page<ListingResponse> getMine(Long companyId, Pageable pageable);

    ListingResponse update(Long companyId, Long listingId, ListingUpdateRequest request);

    ListingResponse updateStatus(Long companyId, Long listingId, ListingStatus status);

    void delete(Long companyId, Long listingId);
}
