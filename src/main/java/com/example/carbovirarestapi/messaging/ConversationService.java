package com.example.carbovirarestapi.messaging;

import com.example.carbovirarestapi.messaging.dto.ConversationResponse;
import com.example.carbovirarestapi.messaging.dto.ConversationStartRequest;
import com.example.carbovirarestapi.messaging.dto.MessageResponse;
import com.example.carbovirarestapi.messaging.dto.MessageSendRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Görüşme ve mesajlaşma işlemleri. */
public interface ConversationService {

    /** Aynı firma + ilan için görüşme zaten varsa onu kullanır, yoksa yenisini açar; her durumda ilk/yeni mesajı ekler. */
    ConversationResponse startOrGet(Long callerUserId, Long callerCompanyId, ConversationStartRequest request);

    Page<ConversationResponse> getMyConversations(Long callerCompanyId, Pageable pageable);

    Page<MessageResponse> getMessages(Long callerCompanyId, Long conversationId, Pageable pageable);

    MessageResponse sendMessage(Long callerUserId, Long callerCompanyId, Long conversationId, MessageSendRequest request);
}
