package com.example.teamjavaprogramming.controller;

import com.example.teamjavaprogramming.ChatMessage;
import com.example.teamjavaprogramming.MemberSet;
import com.example.teamjavaprogramming.service.ChatMessageService;
import com.example.teamjavaprogramming.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final MemberService memberService;
    public ChatMessageController(ChatMessageService chatMessageService,
                                 MemberService memberService) {
        this.chatMessageService = chatMessageService;
        this.memberService = memberService;
    }

    // 특정 채팅방의 메시지 가져오기
    @GetMapping("/{roomId}/messages")
    public List<ChatMessage> getMessages(@PathVariable Long roomId) {

        return chatMessageService.findMessages(roomId);
    }

    // 메시지 보내기
    @PostMapping("/{roomId}/messages")
    public ChatMessage sendMessage(
            @PathVariable Long roomId,
            @RequestParam String message,
            HttpSession session) {
        String username = (String) session.getAttribute("userName");
        MemberSet member = memberService.findByUserName(username).orElseThrow();
        String senderName =  member.getStdName();
        return chatMessageService.sendMessage(
                roomId,
                username,
                senderName,
                message
        );
    }
}

