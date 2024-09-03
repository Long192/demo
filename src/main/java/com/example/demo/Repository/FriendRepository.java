package com.example.demo.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.Enum.FriendStatusEnum;
import com.example.demo.Model.Friend;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    @Query("SELECT f FROM Friend f WHERE " + "(f.friendReceiver.id = :user OR f.friendRequester.id = :user) "
            + "AND f.status = 'accepted' AND ((LOWER(f.friendReceiver.email) LIKE LOWER(CONCAT('%', :search, '%')) OR "
            + "LOWER(f.friendRequester.email) LIKE LOWER(CONCAT('%', :search, '%'))) OR "
            + "(LOWER(f.friendReceiver.email) LIKE LOWER(CONCAT('%', :search, '%')) OR "
            + "LOWER(f.friendRequester.email) LIKE LOWER(CONCAT('%', :search, '%'))))")
    Page<Friend> findFriends(@Param("user") Long user, @Param("search") String search, Pageable page);

    @Query("SELECT f FROM Friend f WHERE (f.friendReceiver.id = :user OR f.friendRequester.id = :user) "
            + "AND f.status = 'accepted'")
    List<Friend> findAllFriends(@Param("user") Long user);

    @Query("SELECT f FROM Friend f WHERE (f.friendReceiver.id = :user OR f.friendRequester.id = :user) "
            + "AND f.status = 'accepted'")
    Optional<Friend> findFriend(@Param("user") Long user);

    @Query("SELECT f FROM Friend f WHERE f.friendReceiver.id = :user AND f.status = 'pending'")
    Page<Friend> findFriendRequests(@Param("user") Long user, Pageable page);

    @Query("SELECT f FROM Friend f WHERE f.friendRequester.id = :user AND f.status = 'pending'")
    Page<Friend> findFriendReceiver(@Param("user") Long user, Pageable page);

    @Query("SELECT f FROM Friend f WHERE ((f.friendReceiver.id = :user AND f.friendRequester.id = :friend)"
            + " OR (f.friendReceiver.id = :friend AND f.friendRequester.id = :user))")
    Optional<Friend> findByFriendRequesterAndFriendReceiver(@Param("user") Long user, @Param("friend") Long friend);

    @Query("SELECT f FROM Friend f WHERE ((f.friendReceiver.id = :user AND f.friendRequester.id = :friend)"
            + " OR (f.friendReceiver.id = :friend AND f.friendRequester.id = :user)) AND f.status = :status")
    Optional<Friend> findByFriendRequesterAndFriendReceiverAndStatus(@Param("user") Long user,
            @Param("friend") Long friend, @Param("status") FriendStatusEnum status);
}
