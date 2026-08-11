package com.example.carbovirarestapi.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.carbovirarestapi.common.entity.BaseEntity;
import com.example.carbovirarestapi.common.exception.BusinessRuleViolationException;
import com.example.carbovirarestapi.common.exception.ResourceNotFoundException;
import com.example.carbovirarestapi.company.Company;
import com.example.carbovirarestapi.company.CompanyRepository;
import com.example.carbovirarestapi.listing.Listing;
import com.example.carbovirarestapi.listing.ListingRepository;
import com.example.carbovirarestapi.listing.ListingStatus;
import com.example.carbovirarestapi.listing.ListingType;
import com.example.carbovirarestapi.messaging.dto.ConversationResponse;
import com.example.carbovirarestapi.messaging.dto.ConversationStartRequest;
import com.example.carbovirarestapi.messaging.dto.MessageResponse;
import com.example.carbovirarestapi.messaging.dto.MessageSendRequest;
import com.example.carbovirarestapi.user.User;
import com.example.carbovirarestapi.user.UserRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class ConversationServiceImplTest {

    private static final Long LISTING_OWNER_COMPANY_ID = 1L;
    private static final Long INITIATOR_COMPANY_ID = 2L;
    private static final Long OTHER_COMPANY_ID = 3L;
    private static final Long USER_ID = 10L;
    private static final Long LISTING_ID = 100L;

    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private ListingRepository listingRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private UserRepository userRepository;

    private ConversationServiceImpl conversationService;

    @BeforeEach
    void setUp() {
        MessageMapper messageMapper = Mappers.getMapper(MessageMapper.class);
        conversationService = new ConversationServiceImpl(
                conversationRepository, messageRepository, listingRepository, companyRepository, userRepository, messageMapper);
    }

    @Test
    void startOrGet_createsNewConversation_whenNoneExists() {
        Listing listing = listingOwnedBy(LISTING_OWNER_COMPANY_ID);
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        when(conversationRepository.findByListingIdAndInitiatorCompanyId(LISTING_ID, INITIATOR_COMPANY_ID))
                .thenReturn(Optional.empty());
        Company initiator = withId(Company.builder().name("Yeşil Dönüşüm").taxNumber("222").build(), INITIATOR_COMPANY_ID);
        when(companyRepository.getReferenceById(INITIATOR_COMPANY_ID)).thenReturn(initiator);
        Conversation savedConversation = withId(
                Conversation.builder().listing(listing).initiatorCompany(initiator).build(), 50L);
        when(conversationRepository.save(any(Conversation.class))).thenReturn(savedConversation);
        when(userRepository.getReferenceById(USER_ID)).thenReturn(withId(new User(), USER_ID));

        ConversationStartRequest request = new ConversationStartRequest(LISTING_ID, "Merhaba, ilgileniyorum.");
        ConversationResponse response = conversationService.startOrGet(USER_ID, INITIATOR_COMPANY_ID, request);

        assertThat(response.listingId()).isEqualTo(LISTING_ID);
        assertThat(response.counterpartCompanyId()).isEqualTo(LISTING_OWNER_COMPANY_ID);
        verify(conversationRepository).save(any(Conversation.class));
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void startOrGet_reusesExistingConversation_insteadOfCreatingNew() {
        Listing listing = listingOwnedBy(LISTING_OWNER_COMPANY_ID);
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));

        Company initiator = withId(Company.builder().name("Yeşil Dönüşüm").taxNumber("222").build(), INITIATOR_COMPANY_ID);
        Conversation existing = withId(Conversation.builder().listing(listing).initiatorCompany(initiator).build(), 50L);
        when(conversationRepository.findByListingIdAndInitiatorCompanyId(LISTING_ID, INITIATOR_COMPANY_ID))
                .thenReturn(Optional.of(existing));
        when(userRepository.getReferenceById(USER_ID)).thenReturn(withId(new User(), USER_ID));

        ConversationStartRequest request = new ConversationStartRequest(LISTING_ID, "Tekrar yazıyorum.");
        conversationService.startOrGet(USER_ID, INITIATOR_COMPANY_ID, request);

        verify(conversationRepository, never()).save(any(Conversation.class));
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void startOrGet_throwsBusinessRuleViolationException_whenMessagingOwnListing() {
        Listing listing = listingOwnedBy(LISTING_OWNER_COMPANY_ID);
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));

        ConversationStartRequest request = new ConversationStartRequest(LISTING_ID, "Merhaba");

        assertThatThrownBy(() -> conversationService.startOrGet(USER_ID, LISTING_OWNER_COMPANY_ID, request))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void startOrGet_throwsResourceNotFoundException_whenListingMissing() {
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.empty());

        ConversationStartRequest request = new ConversationStartRequest(LISTING_ID, "Merhaba");

        assertThatThrownBy(() -> conversationService.startOrGet(USER_ID, INITIATOR_COMPANY_ID, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMessages_throwsAccessDeniedException_whenCallerIsNotParticipant() {
        Conversation conversation = conversationBetween(LISTING_OWNER_COMPANY_ID, INITIATOR_COMPANY_ID);
        when(conversationRepository.findById(50L)).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> conversationService.getMessages(OTHER_COMPANY_ID, 50L, org.springframework.data.domain.Pageable.unpaged()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void sendMessage_succeeds_whenCallerIsListingOwner() {
        Conversation conversation = conversationBetween(LISTING_OWNER_COMPANY_ID, INITIATOR_COMPANY_ID);
        when(conversationRepository.findById(50L)).thenReturn(Optional.of(conversation));
        when(userRepository.getReferenceById(USER_ID)).thenReturn(withId(new User(), USER_ID));

        MessageResponse response = conversationService.sendMessage(
                USER_ID, LISTING_OWNER_COMPANY_ID, 50L, new MessageSendRequest("Tabii, konuşalım."));

        assertThat(response.content()).isEqualTo("Tabii, konuşalım.");
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void sendMessage_throwsAccessDeniedException_whenCallerIsNotParticipant() {
        Conversation conversation = conversationBetween(LISTING_OWNER_COMPANY_ID, INITIATOR_COMPANY_ID);
        when(conversationRepository.findById(50L)).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> conversationService.sendMessage(
                USER_ID, OTHER_COMPANY_ID, 50L, new MessageSendRequest("Ben de araya girmek istiyorum.")))
                .isInstanceOf(AccessDeniedException.class);
        verify(messageRepository, never()).save(any());
    }

    private Listing listingOwnedBy(Long companyId) {
        Company owner = withId(Company.builder().name("Acme").taxNumber("111").build(), companyId);
        return withId(Listing.builder()
                .type(ListingType.WASTE)
                .title("500 kg PET plastik atığı")
                .category("Plastik")
                .quantity(BigDecimal.valueOf(500))
                .unit("kg")
                .city("İstanbul")
                .status(ListingStatus.ACTIVE)
                .company(owner)
                .build(), LISTING_ID);
    }

    private Conversation conversationBetween(Long listingOwnerCompanyId, Long initiatorCompanyId) {
        Listing listing = listingOwnedBy(listingOwnerCompanyId);
        Company initiator = withId(Company.builder().name("Yeşil Dönüşüm").taxNumber("222").build(), initiatorCompanyId);
        return withId(Conversation.builder().listing(listing).initiatorCompany(initiator).build(), 50L);
    }

    /** id alanı BaseEntity üzerinde private/generated olduğundan test için yansıma (reflection) ile atanır. */
    private <T extends BaseEntity> T withId(T entity, Long id) {
        try {
            var idField = BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        return entity;
    }
}
