package com.example.teamjavaprogramming.repository;

import com.example.teamjavaprogramming.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember,Long>{
    List<ChatRoomMember> findByUsername(String username);
    List<ChatRoomMember> findByRoomId(Long roomId);
}
