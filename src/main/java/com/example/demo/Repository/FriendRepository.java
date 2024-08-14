package com.example.demo.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.Model.Friend;
import com.example.demo.Model.User;

import jakarta.transaction.Transactional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    @Query("SELECT f FROM Friend f WHERE (f.friendReceiver.id = :user OR f.friendRequester.id = :user) AND f.status = \"accepted\"")
    List<Friend> findFriends(@Param("user") Long user);

    @Query("SELECT f FROM Friend f WHERE (f.friendReceiver.id = :user OR f.friendRequester.id = :user) AND f.status = \"pending\"")
    List<Friend> findFriendRequests(@Param("user") Long user);

    @Transactional
    @Modifying
    @Query("DELETE FROM Friend WHERE (friendReceiver.id = :userId AND friendRequester.id = :friendId) OR (friendReceiver.id = :friendId AND friendRequester.id = :userId)")
    void deleteFriend(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Query("SELECT f FROM Friend f WHERE (f.friendReceiver = :user AND f.friendRequester = :friend) OR (f.friendReceiver = :friend AND f.friendRequester = :user)")
    Friend findByFriendRequesterAndFriendReceiver(User user, User friend);
}
