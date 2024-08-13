package com.example.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Model.Favourite;

@Repository
public interface FavouriteRepository extends JpaRepository<Favourite, Long> {
    Favourite findByUserIdAndPostId(Long userId, Long postId);
}
