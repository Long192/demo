package com.example.demo.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Enum.StatusEnum;
import com.example.demo.Model.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> getPostByUserId(Long id, Pageable page);

    Page<Post> findAllByStatus(StatusEnum status, Pageable page);
}
