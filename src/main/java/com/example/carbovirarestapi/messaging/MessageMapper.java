package com.example.carbovirarestapi.messaging;

import com.example.carbovirarestapi.messaging.dto.MessageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(source = "conversation.id", target = "conversationId")
    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "sender.fullName", target = "senderName")
    @Mapping(source = "sender.company.id", target = "senderCompanyId")
    @Mapping(source = "sender.company.name", target = "senderCompanyName")
    @Mapping(source = "createdAt", target = "sentAt")
    MessageResponse toResponse(Message message);
}
