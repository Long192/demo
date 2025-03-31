package com.example.demo.Repository;

import com.example.demo.Model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    long countByUserIdAndCreatedAtGreaterThan(long id, Timestamp createdAt);
}
