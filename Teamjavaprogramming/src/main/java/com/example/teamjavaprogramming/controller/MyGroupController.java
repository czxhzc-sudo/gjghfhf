package com.example.teamjavaprogramming.controller;

import com.example.teamjavaprogramming.ChatRoom;
import org.springframework.stereotype.Controller;
import com.example.teamjavaprogramming.ChatRoomMember;
import com.example.teamjavaprogramming.repository.ChatRoomMemberRepository;
import com.example.teamjavaprogramming.repository.ChatRoomRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class MyGroupController {
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    public MyGroupController(ChatRoomMemberRepository chatRoomMemberRepository,
                             ChatRoomRepository chatRoomRepository) {
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.chatRoomRepository = chatRoomRepository;
    }
    @GetMapping("/my-group")
    public String myGroup(HttpSession session, Model model) {
        // 현재 로그인한 사용자의 username
        String userName = (String) session.getAttribute("userName");
        // 내가 가입한 모든 모임 정보 가져오기
        List<ChatRoomMember> members =
                chatRoomMemberRepository.findByUsername(userName);
        // ChatRoomMember에서 roomId만 뽑기
        List<Long> roomIds = members.stream()
                .map(ChatRoomMember::getRoomId)
                .toList();
        // roomId를 이용해서 실제 ChatRoom 가져오기
        List<ChatRoom> myGroups =
                chatRoomRepository.findByIdIn(roomIds);
        // HTML로 전달
        model.addAttribute("myGroups", myGroups);
        return "MyGroup";
    }
}
