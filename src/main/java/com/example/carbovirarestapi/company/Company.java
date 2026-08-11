package com.example.carbovirarestapi.company;

import com.example.carbovirarestapi.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Platformdaki bir firmayı temsil eder. Atık/hammadde ilanlarının sahibidir. */
@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "tax_number", nullable = false, unique = true)
    private String taxNumber;

    private String sector;

    private String city;

    private String address;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false;

    // Yakınlık aramasında kullanılıyor (bkz. ListingServiceImpl.searchNearby). İkisi de opsiyonel,
    // firma profilinde konum girilmemişse o firma bu aramaya hiç dahil olmuyor.
    private Double latitude;

    private Double longitude;
}
