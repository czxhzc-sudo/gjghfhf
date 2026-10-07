package com.example.teamjavaprogramming.service;
import com.example.teamjavaprogramming.MemberSet;
import com.example.teamjavaprogramming.repository.MemberRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Optional;


@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    //회원가입
    public MemberSet join(MemberSet member) {
        member.setStdSchool("백석대학교");
        member.setCreateDate(LocalDate.now());
        return memberRepository.save(member);
    }
    //로그인
    public boolean login(String userName, String password) {

        Optional<MemberSet> member = memberRepository.findByUserName(userName);

        if (member.isPresent()) {
            return member.get().getPassword().equals(password);
        }
        return false;
    }

    public boolean checkUserName(String userName){
        return !memberRepository.existsByUserName(userName);//똑같은 아이디가 존재하는지?
    }
    public Optional<MemberSet> findByUserName(String userName){
        return memberRepository.findByUserName(userName);
    }
    public MemberSet update(MemberSet member){
        return memberRepository.save(member);
    }
}
