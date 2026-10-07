package com.example.teamjavaprogramming.repository;
import com.example.teamjavaprogramming.MatchingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MatchingRequestRepository extends JpaRepository<MatchingRequest,Long> {
    List<MatchingRequest> findByStatus(String status);
    MatchingRequest findByUsernameAndStatus(
            String username,
            String status
    );
    MatchingRequest findTopByUsernameOrderByCreatedAtDesc(
            String username
    );
}
