package com.example.carbovirarestapi.messaging;

import com.example.carbovirarestapi.common.entity.BaseEntity;
import com.example.carbovirarestapi.company.Company;
import com.example.carbovirarestapi.listing.Listing;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Bir ilan üzerinden, ilan sahibi olmayan bir firmanın (initiator) başlattığı görüşme.
 * Görüşmenin diğer tarafı her zaman {@code listing.getCompany()}'dir — ayrıca alan
 * olarak tutulmaz (tek doğruluk kaynağı, veri tekrarı yok).
 */
@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiator_company_id", nullable = false)
    private Company initiatorCompany;
}
