package com.example.carbovirarestapi.messaging;

import com.example.carbovirarestapi.common.exception.BusinessRuleViolationException;
import com.example.carbovirarestapi.common.exception.ResourceNotFoundException;
import com.example.carbovirarestapi.company.Company;
import com.example.carbovirarestapi.company.CompanyRepository;
import com.example.carbovirarestapi.listing.Listing;
import com.example.carbovirarestapi.listing.ListingRepository;
import com.example.carbovirarestapi.messaging.dto.ConversationResponse;
import com.example.carbovirarestapi.messaging.dto.ConversationStartRequest;
import com.example.carbovirarestapi.messaging.dto.MessageResponse;
import com.example.carbovirarestapi.messaging.dto.MessageSendRequest;
import com.example.carbovirarestapi.user.User;
import com.example.carbovirarestapi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ListingRepository listingRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;

    @Override
    @Transactional
    public ConversationResponse startOrGet(Long callerUserId, Long callerCompanyId, ConversationStartRequest request) {
        Listing listing = listingRepository.findById(request.listingId())
                .orElseThrow(() -> new ResourceNotFoundException("İlan bulunamadı: id=" + request.listingId()));

        if (listing.getCompany().getId().equals(callerCompanyId)) {
            throw new BusinessRuleViolationException("Kendi ilanınıza mesaj gönderemezsiniz.");
        }

        Conversation conversation = conversationRepository
                .findByListingIdAndInitiatorCompanyId(listing.getId(), callerCompanyId)
                .orElseGet(() -> conversationRepository.save(Conversation.builder()
                        .listing(listing)
                        .initiatorCompany(companyRepository.getReferenceById(callerCompanyId))
                        .build()));

        addMessage(conversation, callerUserId, request.message());

        return toConversationResponse(conversation, callerCompanyId);
    }

    @Override
    public Page<ConversationResponse> getMyConversations(Long callerCompanyId, Pageable pageable) {
        return conversationRepository.findAllInvolvingCompany(callerCompanyId, pageable)
                .map(conversation -> toConversationResponse(conversation, callerCompanyId));
    }

    @Override
    public Page<MessageResponse> getMessages(Long callerCompanyId, Long conversationId, Pageable pageable) {
        Conversation conversation = findConversationOrThrow(conversationId);
        checkParticipant(conversation, callerCompanyId);
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable)
                .map(messageMapper::toResponse);
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(Long callerUserId, Long callerCompanyId, Long conversationId, MessageSendRequest request) {
        Conversation conversation = findConversationOrThrow(conversationId);
        checkParticipant(conversation, callerCompanyId);
        return addMessage(conversation, callerUserId, request.content());
    }

    private MessageResponse addMessage(Conversation conversation, Long senderUserId, String content) {
        User sender = userRepository.getReferenceById(senderUserId);
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .build();
        messageRepository.save(message);
        return messageMapper.toResponse(message);
    }

    private Conversation findConversationOrThrow(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Görüşme bulunamadı: id=" + conversationId));
    }

    private void checkParticipant(Conversation conversation, Long callerCompanyId) {
        boolean isInitiator = conversation.getInitiatorCompany().getId().equals(callerCompanyId);
        boolean isListingOwner = conversation.getListing().getCompany().getId().equals(callerCompanyId);
        if (!isInitiator && !isListingOwner) {
            throw new AccessDeniedException("Bu görüşmeye erişim yetkiniz yok.");
        }
    }

    private ConversationResponse toConversationResponse(Conversation conversation, Long callerCompanyId) {
        boolean callerIsInitiator = conversation.getInitiatorCompany().getId().equals(callerCompanyId);
        Company counterpart = callerIsInitiator
                ? conversation.getListing().getCompany()
                : conversation.getInitiatorCompany();

        return new ConversationResponse(
                conversation.getId(),
                conversation.getListing().getId(),
                conversation.getListing().getTitle(),
                counterpart.getId(),
                counterpart.getName(),
                conversation.getCreatedAt()
        );
    }
}
