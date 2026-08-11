package com.example.carbovirarestapi.listing;

import com.example.carbovirarestapi.listing.dto.ListingResponse;
import com.example.carbovirarestapi.listing.dto.ListingUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ListingMapper {

    @Mapping(source = "company.id", target = "companyId")
    @Mapping(source = "company.name", target = "companyName")
    ListingResponse toResponse(Listing listing);

    // type, status ve company bu uçtan bilerek güncellenmez: type sabittir, status ve
    // company için ayrı iş kuralları (ListingService) uygulanır.
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "company", ignore = true)
    void updateEntity(ListingUpdateRequest request, @MappingTarget Listing listing);
}
