package com.example.teamjavaprogramming.controller;

import com.example.teamjavaprogramming.MemberSet;
import com.example.teamjavaprogramming.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Optional;

@Controller
public class MyPageController {
    private final MemberService memberService;
    public MyPageController(MemberService memberService){
        this.memberService = memberService;
    }
    @GetMapping("/mypage")
    public String myPage(HttpSession session, Model model){
        String userName = (String)session.getAttribute("userName");
        if(userName == null){
            return "redirect:/member/login";
        }
        Optional<MemberSet> member = memberService.findByUserName(userName);
        if(member.isPresent()){
            model.addAttribute("member",member.get());
        }
        return "MyPage";
    }
    @GetMapping("/mypage/edit")
    public String editForm(HttpSession session, Model model){
        String userName = (String) session.getAttribute("userName");
        if(userName == null){
            return "redirect:/member/login";
        }
        Optional<MemberSet> member = memberService.findByUserName(userName);
        if (member.isPresent()){
            model.addAttribute("member",member.get());
        }
        return "MyPageEdit";
    }
    @PostMapping("/mypage/edit")
    public String edit(
            @RequestParam String stdName,
            @RequestParam String stdGender,
            @RequestParam LocalDate stdBirth,
            HttpSession session
    ){
        String userName = (String) session.getAttribute("userName");
        if(userName == null){
            return "redirect:/member/login";
        }
        Optional<MemberSet> member = memberService.findByUserName(userName);
        if(member.isPresent()){
            MemberSet currentMember = member.get();
            currentMember.setStdName(stdName);
            currentMember.setStdGender(stdGender);
            currentMember.setStdBirth(stdBirth);
            memberService.update(currentMember);
        }
        return "redirect:/mypage";
    }
}
