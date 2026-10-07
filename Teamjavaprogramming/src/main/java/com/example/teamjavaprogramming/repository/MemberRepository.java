package com.example.teamjavaprogramming.repository;
import com.example.teamjavaprogramming.MemberSet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberSet, Long> {
    Optional<MemberSet> findByUserName(String userName);
    boolean existsByUserName(String userName);
}
