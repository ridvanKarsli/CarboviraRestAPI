package com.example.carbovirarestapi.company;

import com.example.carbovirarestapi.company.dto.CompanyResponse;
import com.example.carbovirarestapi.company.dto.CompanyUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/** Entity <-> DTO dönüşümü. Derleme zamanında MapStruct tarafından implemente edilir. */
@Mapper(componentModel = "spring")
public interface CompanyMapper {

    CompanyResponse toResponse(Company company);

    // taxNumber (kimlik bilgisi) ve verified (sadece admin onayı ile değişir) bu uçtan bilerek güncellenmez.
    @Mapping(target = "taxNumber", ignore = true)
    @Mapping(target = "verified", ignore = true)
    void updateEntity(CompanyUpdateRequest request, @MappingTarget Company company);
}
