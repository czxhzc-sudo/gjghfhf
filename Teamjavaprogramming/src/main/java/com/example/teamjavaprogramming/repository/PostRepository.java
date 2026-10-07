package com.example.teamjavaprogramming.repository;
import com.example.teamjavaprogramming.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post,Long>{
}
