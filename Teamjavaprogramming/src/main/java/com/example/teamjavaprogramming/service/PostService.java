package com.example.teamjavaprogramming.service;

import com.example.teamjavaprogramming.ChatRoom;
import com.example.teamjavaprogramming.ChatRoomMember;
import com.example.teamjavaprogramming.MemberSet;
import com.example.teamjavaprogramming.Post;
import com.example.teamjavaprogramming.repository.ChatRoomMemberRepository;
import com.example.teamjavaprogramming.repository.ChatRoomRepository;
import com.example.teamjavaprogramming.repository.PostRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {
    private final MemberService memberService;
    private final PostRepository postRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    public PostService(PostRepository postRepository,
                       ChatRoomRepository chatRoomRepository,
                       ChatRoomMemberRepository chatRoomMemberRepository,
                       MemberService memberService) {
        this.postRepository = postRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.memberService = memberService;
    }
    public Post createPost(Post post, String username) {
        MemberSet member =
                memberService.findByUserName(username).orElseThrow();
        post.setCurrentMembers(1);
        // 성별별 모집인 경우
        if ("성별별".equals(post.getRecruitType())) {
            if ("남성".equals(member.getStdGender())) {
                if (post.getMaleMaxMembers() < 1) {
                    throw new IllegalArgumentException(
                            "남성 작성자는 남성 모집 인원을 최소 1명으로 설정해야 합니다."
                    );
                }
                post.setMaleCurrentMembers(1);
            }

            if ("여성".equals(member.getStdGender())) {
                if (post.getFemaleMaxMembers() < 1) {
                    throw new IllegalArgumentException(
                            "여성 작성자는 여성 모집 인원을 최소 1명으로 설정해야 합니다."
                    );
                }

                post.setFemaleCurrentMembers(1);
            }
        }
        // 1. 게시글 저장
        Post savedPost = postRepository.save(post);
        // 2. 채팅방 생성
        ChatRoom chatRoom = new ChatRoom();
        // 3. 게시글과 채팅방 연결
        chatRoom.setPost(savedPost);
        // 4. 채팅방 이름 = 게시글 제목
        chatRoom.setRoomName(savedPost.getTitle());
        // 5. 채팅방 생성 시간
        chatRoom.setCreatedAt(LocalDateTime.now());
        // 6. 채팅방 저장
        chatRoomRepository.save(chatRoom);
        // 7. 작성자를 채팅방에 자동 가입
        ChatRoomMember memberInfo = new ChatRoomMember();
        // 방 번호 저장
        memberInfo.setRoomId(chatRoom.getId());
        // 로그인한 사용자의 username 저장
        memberInfo.setUsername(username);
        // 가입 시간 저장
        memberInfo.setJoinedAt(LocalDateTime.now());
        // 채팅방 회원 저장
        chatRoomMemberRepository.save(memberInfo);
        return savedPost;
    }
    public String joinPost(Long postId, String username) {

        // 게시글에 연결된 채팅방 찾기
        ChatRoom chatRoom = chatRoomRepository.findAll()
                .stream()
                .filter(room ->
                        room.getPost().getId().equals(postId))
                .findFirst()
                .orElseThrow();
        Post post = chatRoom.getPost();
        // 현재 가입하려는 회원 정보
        MemberSet member =
                memberService.findByUserName(username).orElseThrow();

        //작성자 인지?
        if (post.getAuthor().equals(member.getStdName())){
            return "OWNER";
        }
        // 이미 가입했는지 확인
        List<ChatRoomMember> members =
                chatRoomMemberRepository.findByRoomId(chatRoom.getId());

        for (ChatRoomMember roomMember : members) {
            if (roomMember.getUsername().equals(username)) {
                return "ALREADY_JOINED";
            }
        }
        // 모집 전공 조건 확인
        if (!"무관".equals(post.getRecruitMajor())) {
            if (!post.getRecruitMajor().equals(member.getMajor())) {
                return "MAJOR_NOT_MATCH";
            }
        }
        // 모집 학번 조건 확인
        if (post.getRecruitStartYear() != null
                && post.getRecruitEndYear() != null) {
            // 8자리 학번에서 앞 4자리 추출
            // 예: 20231234 -> 2023
            if (member.getStdIDN() == null) {
                return "STUDENT_ID_NOT_MATCH";
            }
            Integer studentYear = member.getStdIDN() / 10000;
            if (studentYear < post.getRecruitStartYear()
                    || studentYear > post.getRecruitEndYear()) {
                return "STUDENT_ID_NOT_MATCH";
            }
        }
        // 현재 인원이 최대 인원에 도달했으면 가입 불가
        if (post.getCurrentMembers() >= post.getMaxMembers()) {
            return "FULL";
        }
        // 성별별 모집
        if ("성별별".equals(post.getRecruitType())) {
            String gender = member.getStdGender();

            // 남성 모집 인원이 가득 찬 경우
            if ("남성".equals(gender) &&
                    post.getMaleCurrentMembers() >= post.getMaleMaxMembers()) {
                return "MALE_FULL";
            }
            // 여성 모집 인원이 가득 찬 경우
            if ("여성".equals(gender) &&
                    post.getFemaleCurrentMembers() >= post.getFemaleMaxMembers()) {
                return "FEMALE_FULL";
            }
        }
        // 가입 정보 생성
        ChatRoomMember memberInfo = new ChatRoomMember();

        memberInfo.setRoomId(chatRoom.getId());
        memberInfo.setUsername(username);
        memberInfo.setJoinedAt(LocalDateTime.now());
        // 저장
        chatRoomMemberRepository.save( memberInfo);
        // 모집 인원 증가
        post.setCurrentMembers(
                post.getCurrentMembers() + 1
        );
        // 성별별 모집이면 성별 인원도 증가
        if ("성별별".equals(post.getRecruitType())) {
            if ("남성".equals(member.getStdGender())) {
                post.setMaleCurrentMembers(
                        post.getMaleCurrentMembers() + 1
                );
            } else if ("여성".equals(member.getStdGender())) {
                post.setFemaleCurrentMembers(
                        post.getFemaleCurrentMembers() + 1
                );
            }
        }
        postRepository.save(post);
        return "SUCCESS";
    }
    public List<Post> findAllPosts() {
        return postRepository.findAll();
    }
}