package com.example.demo.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.Enum.FriendStatusEnum;
import com.example.demo.Enum.StatusEnum;
import com.example.demo.Model.Friend;
import com.example.demo.Model.Post;
import com.example.demo.Model.User;
import com.example.demo.Repository.FriendRepository;

@Service
public class FriendService {
    @Autowired
    private UserService userService;
    @Autowired
    private FriendRepository friendRepository;

    public List<Post> getFriendPost() throws Exception {
        List<Post> posts = new ArrayList<>();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7));
        List<User> friends = getFriends();
        for (User friend : friends) {
            posts.addAll(friend.getPosts().stream().filter(
                    post -> (post.getCreatedAt().before(timestamp) && post.getStatus().equals(StatusEnum.active)))
                    .toList());
        }
        return posts;
    }

    public void addFriend(Long userId, Long friendId) throws Exception {
        User user = userService.findById(userId);
        User friend = userService.findById(friendId);
        if (friendRepository.findByFriendRequesterAndFriendReceiver(user, friend) != null) {
            throw new Exception("already friend or waiting to be accept");
        }
        Friend friendShip = new Friend();
        friendShip.setFriendRequester(user);
        friendShip.setFriendReceiver(friend);
        friendShip.setStatus(FriendStatusEnum.pending);
        friendRepository.save(friendShip);
    }

    public void updateFriendStatus(Long requesterId, Long userId) throws Exception {
        User requester = userService.findById(requesterId);
        User user = userService.findById(userId);
        Friend friend = friendRepository.findByFriendRequesterAndFriendReceiver(requester, user);
        if (friend.getStatus().equals(FriendStatusEnum.accepted)) {
            throw new Exception("already friend");
        }
        friend.setStatus(FriendStatusEnum.accepted);
        friendRepository.save(friend);
    }

    public void removeFriend(Long userId, Long friendId) throws Exception {
        friendRepository.deleteFriend(userId, friendId);
    }

    public List<User> getFriends() throws Exception {
        User userToken = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Friend> friends = friendRepository.findFriends(userToken.getId());
        return friendToUser(friends, userToken.getId());
    }

    public List<User> getFriendRequests() throws Exception {
        User userToken = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Friend> friends = friendRepository.findFriendRequests(userToken.getId());
        return friendToUser(friends, userToken.getId());
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
        return friendRepository.findFriends(user.getId());
    }
}
