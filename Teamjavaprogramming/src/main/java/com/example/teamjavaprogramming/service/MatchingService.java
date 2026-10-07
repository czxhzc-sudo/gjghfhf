package com.example.teamjavaprogramming.service;

import com.example.teamjavaprogramming.ChatRoom;
import com.example.teamjavaprogramming.ChatRoomMember;
import com.example.teamjavaprogramming.MatchingRequest;
import com.example.teamjavaprogramming.MemberSet;
import com.example.teamjavaprogramming.repository.ChatRoomMemberRepository;
import com.example.teamjavaprogramming.repository.ChatRoomRepository;
import com.example.teamjavaprogramming.repository.MatchingRequestRepository;
import com.example.teamjavaprogramming.repository.MemberRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MatchingService {

    private final MatchingRequestRepository matchingRequestRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    public MatchingService(
            MatchingRequestRepository matchingRequestRepository,
            MemberRepository memberRepository,
             ChatRoomRepository chatRoomRepository,
            ChatRoomMemberRepository chatRoomMemberRepository
    ) {
        this.matchingRequestRepository = matchingRequestRepository;
        this.memberRepository = memberRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
    }

    // 매칭 신청을 저장하고 상대를 찾는다.
    public MatchingRequest startMatching(
            String username,
            String targetGender,
            String targetMajor,
            Integer minStudentYear,
            Integer maxStudentYear,
            String targetContents
    ) {

        // 이미 매칭 중인지 확인
        MatchingRequest existingRequest =
                matchingRequestRepository.findByUsernameAndStatus(
                        username,
                        "WAITING"
                );

        if (existingRequest != null) {
            return existingRequest;
        }

        // 새로운 매칭 신청 생성
        MatchingRequest request = new MatchingRequest();

        request.setUsername(username);
        request.setTargetGender(targetGender);
        request.setTargetMajor(targetMajor);
        request.setMinStudentYear(minStudentYear);
        request.setMaxStudentYear(maxStudentYear);
        request.setTargetContents(targetContents);
        request.setStatus("WAITING");
        request.setCreatedAt(LocalDateTime.now());

        // DB에 저장
        matchingRequestRepository.save(request);
        // 상대방 찾기
        findMatch(request);
        return request;
    }

    // 현재 WAITING 중인 사람 중 나와 맞는 사람을 찾는다.
    private void findMatch(MatchingRequest myRequest) {

        List<MatchingRequest> waitingRequests =
                matchingRequestRepository.findByStatus("WAITING");

        MemberSet me = memberRepository
                .findByUserName(myRequest.getUsername())
                .orElse(null);

        if (me == null) {
            return;
        }
        for (MatchingRequest otherRequest : waitingRequests) {

            // 자기 자신은 제외
            if (otherRequest.getUsername()
                    .equals(myRequest.getUsername())) {
                continue;
            }
            // 상대방 회원정보 가져오기
            MemberSet other = memberRepository
                    .findByUserName(otherRequest.getUsername())
                    .orElse(null);

            if (other == null) {
                continue;
            }

            // 서로의 조건을 확인
            boolean myCondition =
                    isMatch(other, myRequest);
            boolean otherCondition =
                    isMatch(me, otherRequest);
            System.out.println("================================");
            System.out.println("매칭 확인");
            System.out.println("나 : " + me.getStdName());
            System.out.println("상대 : " + other.getStdName());
            System.out.println("내 조건 → 상대 : " + myCondition);
            System.out.println("상대 조건 → 나 : " + otherCondition);
            System.out.println("================================");

            // 양쪽 모두 만족하면 매칭 성공
            if (myCondition && otherCondition) {

                myRequest.setStatus("MATCHED");
                otherRequest.setStatus("MATCHED");

                matchingRequestRepository.save(myRequest);
                matchingRequestRepository.save(otherRequest);
                //채팅방 생성
                ChatRoom chatRoom = new ChatRoom();

                chatRoom.setRoomName(
                        me.getStdName() + ", " + other.getStdName()
                );
                chatRoom.setCreatedAt(LocalDateTime.now());
                chatRoom.setPost(null);
                chatRoomRepository.save(chatRoom);

                myRequest.setChatRoomId(chatRoom.getId());
                otherRequest.setChatRoomId(chatRoom.getId());

                matchingRequestRepository.save(myRequest);
                matchingRequestRepository.save(otherRequest);
                System.out.println(
                        "매칭 성공 : "
                                + me.getStdName()
                                + " ↔ "
                                + other.getStdName() );
                //채팅방에 합류
                ChatRoomMember myMember = new ChatRoomMember();

                myMember.setRoomId(chatRoom.getId());
                myMember.setUsername(me.getUserName());
                myMember.setJoinedAt(LocalDateTime.now());

                chatRoomMemberRepository.save(myMember);


                ChatRoomMember otherMember = new ChatRoomMember();

                otherMember.setRoomId(chatRoom.getId());
                otherMember.setUsername(other.getUserName());
                otherMember.setJoinedAt(LocalDateTime.now());

                chatRoomMemberRepository.save(otherMember);
                break;
            }
        }
    }

    // 한 사람이 원하는 조건에 상대방이 맞는지 확인
    private boolean isMatch(
            MemberSet target,
            MatchingRequest request
    ) {
        // 성별 확인
        if (!isGenderMatch(
                target.getStdGender(),
                request.getTargetGender()
        )) {
            return false;
        }
        // 전공 확인
        if (!isMajorMatch(
                target.getMajor(),
                request.getTargetMajor()
        )) {
            return false;
        }
        // 학번 확인
        if (!isStudentYearMatch(
                target.getStdIDN(),
                request.getMinStudentYear(),
                request.getMaxStudentYear()
        )) {
            return false;
        }
        // 콘텐츠 확인
        if (!isContentMatch(
                target.getStdCategory(),
                request.getTargetContents()
        )) {
            return false;
        }
        return true;
    }
    // 성별 비교
    private boolean isGenderMatch(
            String targetGender,
            String wantedGender
    ) {
        // 무관이면 누구든 가능
        if ("무관".equals(wantedGender)) {
            return true;
        }
        return wantedGender != null
                && wantedGender.equals(targetGender);
    }

    // 전공 비교
    private boolean isMajorMatch(
            String targetMajor,
            String wantedMajor
    ) {
        // 아무 전공도 선택하지 않았으면 무관
        if (wantedMajor == null
                || wantedMajor.isBlank()
                || "무관".equals(wantedMajor)) {
            return true;
        }
        // 상대방 전공이 없으면 실패
        if (targetMajor == null
                || targetMajor.isBlank()) {
            return false;
        }
        String[] wantedList = wantedMajor.split(",");
        // 내가 선택한 전공 중 하나라도 상대방 전공과 같으면 OK
        for (String wanted : wantedList) {

            if (wanted.trim().equals(targetMajor.trim())) {
                return true;
            }
        }
        return false;
    }

    // 학번 비교
    private boolean isStudentYearMatch(
            Integer studentId,
            Integer minYear,
            Integer maxYear
    ) {
        // 최소/최대 학번을 모두 선택하지 않았다면 무관
        if (minYear == null && maxYear == null) {
            return true;
        }
        // 학번이 없으면 실패
        if (studentId == null) {
            return false;
        }
        // 학번의 앞 4자리를 학번 연도로 사용
        int studentYear = studentId / 10000;
        // 최소 학번 확인
        if (minYear != null && studentYear < minYear) {
            return false;
        }
        // 최대 학번 확인
        if (maxYear != null && studentYear > maxYear) {
            return false;
        }

        return true;
    }
    // 콘텐츠 비교
    private boolean isContentMatch(
            String targetContents,
            String wantedContents
    ) {
        // 내가 콘텐츠를 선택하지 않았다면 무관으로 처리
        if (wantedContents == null || wantedContents.isBlank()) {
            return true;
        }
        // 상대방 콘텐츠가 없으면 실패
        if (targetContents == null || targetContents.isBlank()) {
            return false;
        }

        String[] targetList = targetContents.split(",");
        String[] wantedList = wantedContents.split(",");
        // 하나라도 겹치면 OK
        for (String target : targetList) {
            for (String wanted : wantedList) {

                if (target.trim().equals(wanted.trim())) {
                    return true;
                }
            }
        }
        return false;
    }
    public MatchingRequest getMatchingStatus(String username) {

        return matchingRequestRepository
                .findTopByUsernameOrderByCreatedAtDesc(username);
    }
}
