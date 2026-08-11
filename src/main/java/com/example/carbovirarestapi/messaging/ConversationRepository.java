package com.example.carbovirarestapi.messaging;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /** Aynı firma aynı ilan için ikinci bir görüşme başlatamaz; varsa mevcut görüşme yeniden kullanılır. */
    Optional<Conversation> findByListingIdAndInitiatorCompanyId(Long listingId, Long initiatorCompanyId);

    /** Bir firmanın taraf olduğu tüm görüşmeler: ya görüşmeyi başlatan ya da ilan sahibi olarak. */
    @Query("""
            select c from Conversation c
            where c.initiatorCompany.id = :companyId or c.listing.company.id = :companyId
            """)
    Page<Conversation> findAllInvolvingCompany(@Param("companyId") Long companyId, Pageable pageable);
}
