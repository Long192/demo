package com.example.demo.Service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.Dto.Response.CustomPage;
import com.example.demo.Dto.Response.UserDto;
import com.example.demo.Enum.FriendStatusEnum;
import com.example.demo.Exception.CustomException;
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
    private ModelMapper mapper;


    public UserDto addFriend(Long friendId) throws Exception {
        User me = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (me.getId().equals(friendId)) {
            throw new CustomException(404, "can't add yourself");
        }
        User user = userService.findById(me.getId());
        User friend = userService.findById(friendId);
        if (friendRepository.findByFriendRequesterAndFriendReceiver(user.getId(), friend.getId()).isPresent()) {
            throw new Exception("already friend or waiting to be accept");
        }
        Friend friendShip = new Friend();
        friendShip.setFriendRequester(user);
        friendShip.setFriendReceiver(friend);
        friendShip.setStatus(FriendStatusEnum.pending);
        Friend result = friendRepository.save(friendShip);
        return mapper.map(result.getFriendReceiver(), UserDto.class);
    }

    public UserDto acceptFriendRequest(Long requesterId) throws Exception {
        User me = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Friend friend =
                findByFriendRequesterAndFriendReceiverAndStatus(requesterId, me.getId(), FriendStatusEnum.pending);
        if(friend == null){
            throw new CustomException(404, "friend request not found or already friend");
        }

        if(friend.getFriendReceiver().getId() != me.getId()){
            throw new CustomException(404, "cannot accept friend request");
        }

        friend.setStatus(FriendStatusEnum.accepted);
        Friend result = friendRepository.save(friend);
        return mapper.map(result.getFriendRequester(), UserDto.class);
    }

    public void deleteFriendRequest(Long requesterId) throws Exception {
        User me = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Friend friend = friendRepository
                .findByFriendRequesterAndFriendReceiverAndStatus(requesterId, me.getId(), FriendStatusEnum.pending)
                .orElseThrow(() -> new CustomException(404, "friend not found"));
        friendRepository.delete(friend);
    }

    public Friend findByFriendRequesterAndFriendReceiverAndStatus(Long requesterId, Long receiverId, FriendStatusEnum status) {
        return friendRepository.findByFriendRequesterAndFriendReceiverAndStatus(requesterId, receiverId, status).orElse(null);
    }

    public void removeFriend(Long friendId) throws Exception {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Friend friend = friendRepository
                .findByFriendRequesterAndFriendReceiverAndStatus(user.getId(), friendId, FriendStatusEnum.accepted)
                .orElseThrow(() -> new CustomException(404, "friend not found"));
        friendRepository.delete(friend);
    }

    public CustomPage<UserDto> getFriends(Pageable pageable, String search) throws Exception {
        User userToken = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            Page<Friend> friends = friendRepository.findFriends(userToken.getId(), search, pageable);
            List<User> users = friendToUser(friends.getContent(), userToken.getId());
            return mapper.map(new PageImpl<>(users, friends.getPageable(), friends.getTotalPages()),
                    new TypeToken<CustomPage<UserDto>>() {}.getType());
        } catch (InvalidDataAccessApiUsageException e) {
            throw new Exception("wrong sort by");
        }
    }

    public CustomPage<UserDto> getFriendRequests(Pageable pageable) throws Exception {
        User userToken = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            Page<Friend> friends = friendRepository.findFriendRequests(userToken.getId(), pageable);
            List<User> users = friendToUser(friends.getContent(), userToken.getId());
            return mapper.map(new PageImpl<>(users, friends.getPageable(), friends.getTotalPages()),
                    new TypeToken<CustomPage<UserDto>>() {}.getType());
        } catch (InvalidDataAccessApiUsageException e) {
            throw new Exception("wrong sort by");
        }
    }

    public CustomPage<UserDto> getFriendReceivers(Pageable pageable) throws Exception {
        User userToken = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            Page<Friend> friends = friendRepository.findFriendReceiver(userToken.getId(), pageable);
            List<User> users = friendToUser(friends.getContent(), userToken.getId());
            return mapper.map(new PageImpl<>(users, friends.getPageable(), friends.getTotalPages()),
                    new TypeToken<CustomPage<UserDto>>() {}.getType());
        } catch (InvalidDataAccessApiUsageException e) {
            throw new Exception("wrong sort by");
        }
    }

    public List<Friend> getFriendRaw() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return friendRepository.findAllFriends(user.getId());
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

    public List<User> getAllFriend() {
        User userToken = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Friend> friends = friendRepository.findAllFriends(userToken.getId());
        return friendToUser(friends, userToken.getId());
    }
}
