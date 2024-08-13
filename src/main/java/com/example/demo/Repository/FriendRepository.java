package com.example.demo.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.Model.Friend;
import com.example.demo.Model.User;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    @Query("SELECT f FROM Friend f WHERE (f.friendReceiver = :user OR f.friendRequester = :user) AND f.status = \"accepted\"")
    List<Friend> findFriends(@Param("user") User user);

    @Query("SELECT f FROM Friend f WHERE (f.friendReceiver = :user OR f.friendRequester = :user) AND f.status = \"pending\"")
    List<Friend> findFriendRequests(@Param("user") User user);
    
    @Query("DELETE FROM Friend WHERE (friendReceiver = :user AND friendRequester = :friend) OR (friendReceiver = :friend AND friendRequester = :user)")
    void deleteFriend(@Param("user") User user, @Param("friend") User friend);

    Friend findByFriendRequesterAndFriendReceiver(User friendRequester, User friendReceiver);
}
