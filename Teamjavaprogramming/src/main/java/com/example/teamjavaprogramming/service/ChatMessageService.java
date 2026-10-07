package com.example.teamjavaprogramming.service;

import com.example.teamjavaprogramming.ChatMessage;
import com.example.teamjavaprogramming.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    // 메시지 저장
    public ChatMessage sendMessage(Long roomId,
                                   String username,
                                   String senderName,
                                   String message) {

        ChatMessage chatMessage = new ChatMessage();

        chatMessage.setRoomId(roomId);
        chatMessage.setUsername(username);
        chatMessage.setSenderName(senderName);
        chatMessage.setMessage(message);
        chatMessage.setSentAt(LocalDateTime.now());

        return chatMessageRepository.save(chatMessage);
    }

    // 특정 채팅방의 메시지 가져오기
    public List<ChatMessage> findMessages(Long roomId) {
        return chatMessageRepository
                .findByRoomIdOrderBySentAtAsc(roomId);
    }
}