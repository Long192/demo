package com.example.demo.Repository;

import com.example.demo.Enum.StatusEnum;
import com.example.demo.Model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> getPostByUserId(Long id, Pageable page);

    Page<Post> findAllByStatus(StatusEnum status, Pageable page);

    @Query("SELECT p FROM Post p WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :textSearch, '%')) AND p.status = :status")
    Page<Post> findPostWithSearchAndSort(
        @Param("textSearch") String textSearch,
        @Param("status") StatusEnum status,
        Pageable page
    );

    @Query("SELECT p FROM Post p WHERE p.user.id in :ids AND p.createdAt > :timestamp")
    Page<Post> findPostByUserIdsAndCreatedAt(@Param("ids") List<Long> ids, @Param("timestamp") Timestamp timestamp, Pageable page);
}
