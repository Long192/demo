package com.example.demo.Repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.Model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.fullname) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE (CONCAT('%', :search, '%'))")
    Page<User> findByEmailOrFullname(Pageable page, String search);
}
