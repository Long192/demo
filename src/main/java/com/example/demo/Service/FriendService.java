package com.example.demo.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.Dto.Response.PostDto;
import com.example.demo.Enum.FriendStatusEnum;
import com.example.demo.Model.Friend;
import com.example.demo.Model.User;
import com.example.demo.Repository.FriendRepository;

@Service
public class FriendService {
    @Autowired
    private UserService userService;
    @Autowired
    private FriendRepository friendRepository;
    @Autowired
    private PostService postService;

    public Page<PostDto> getFriendPost(Pageable pageable) throws Exception {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7));
        List<Long> friendIds = new ArrayList<>();
        getAllFriend().forEach(user -> {
            friendIds.add(user.getId());
        });
        try {
            return postService.findPostByUserIdsAndCreatedAt(friendIds, timestamp, pageable);
        } catch (InvalidDataAccessApiUsageException e) {
            throw new Exception("cannot find attribute " + pageable.getSort().toString());
        }
    }

    public void addFriend(Long friendId) throws Exception {
        User me = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.findById(me.getId());
        User friend = userService.findById(friendId);
        if (friendRepository.findByFriendRequesterAndFriendReceiver(user.getId(), friend.getId()) != null) {
            throw new Exception("already friend or waiting to be accept");
        }
        Friend friendShip = new Friend();
        friendShip.setFriendRequester(user);
        friendShip.setFriendReceiver(friend);
        friendShip.setStatus(FriendStatusEnum.pending);
        friendRepository.save(friendShip);
    }

    public void updateFriendStatus(Long requesterId) throws Exception {
        User me = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Friend friend = friendRepository.findByFriendRequesterAndFriendReceiver(requesterId, me.getId());
        if (friend == null) {
            throw new Exception("friend not found");
        }

        if (friend.getStatus().equals(FriendStatusEnum.accepted)) {
            throw new Exception("already friend");
        }
        
        friend.setStatus(FriendStatusEnum.accepted);
        friendRepository.save(friend);
    }

    public void removeFriend(Long friendId) throws Exception {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        friendRepository.deleteFriend(user.getId(), friendId);
    }

    public Page<User> getFriends(Pageable pageable, String search) throws Exception {
        User userToken = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            Page<Friend> friends = friendRepository.findFriends(userToken.getId(), search, pageable);
            List<User> users = friendToUser(friends.getContent(), userToken.getId());
            return new PageImpl<>(users, friends.getPageable(), friends.getTotalPages());
        } catch (InvalidDataAccessApiUsageException e) {
            throw new Exception("cannot find attribute " + pageable.getSort());
        }
    }

    private List<User> getAllFriend() {
        User userToken = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Friend> friends = friendRepository.findAllFriends(userToken.getId());
        return friendToUser(friends, userToken.getId());
    }

    public Page<User> getFriendRequests(Pageable pageable) throws Exception {
        User userToken = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Page<Friend> friends = friendRepository.findFriendRequests(userToken.getId(), pageable);
        List<User> users = friendToUser(friends.getContent(), userToken.getId());
        return new PageImpl<>(users, friends.getPageable(), friends.getTotalPages());
    }

    private List<User> friendToUser(List<Friend> friends, Long id) {
        List<User> friendList = new ArrayList<>();
        for (Friend friend : friends) {
            if (friend.getFriendReceiver().getId().equals(id)) {
                friendList.add(friend.getFriendRequester());
                continue;
            }
            friendList.add(friend.getFriendReceiver());
        }
        return friendList;
    }

    public List<Friend> getFriendRaw() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return friendRepository.findAllFriends(user.getId());
    }
}
