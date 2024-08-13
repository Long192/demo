package com.example.demo.Service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.Enum.FriendStatusEnum;
import com.example.demo.Dto.Response.UserDto;
import com.example.demo.Model.Friend;
import com.example.demo.Model.User;
import com.example.demo.Repository.FriendRepository;
import com.example.demo.Repository.UserRepository;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FriendRepository friendRepository;
    @Autowired
    private ModelMapper mapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email).orElseThrow();
    }

    public User findById(Long id) throws Exception {
        return userRepository.findById(id).orElseThrow(() -> new Exception("user not found"));
    }

    public void addFriend(Long userId, Long friendId) throws Exception {
        User user = findById(userId);
        User friend = findById(friendId);
        if(friendRepository.findByFriendRequesterAndFriendReceiver(user, friend) != null){
            throw new Exception("already friend or waiting to be accept");
        }
        Friend friendShip = new Friend();
        friendShip.setFriendRequester(user);
        friendShip.setFriendReceiver(friend);
        friendShip.setStatus(FriendStatusEnum.pending);
        friendRepository.save(friendShip);
    }

    public List<UserDto> getFriends (Long id) throws Exception{
        User user = findById(id);
        List<Friend> friends = friendRepository.findFriends(user);
        return friendToUser(friends, id);
    }

    public List<UserDto> getFriendRequests (Long id) throws Exception{
        User user = findById(id);
        List<Friend> friends = friendRepository.findFriendRequests(user);
        return friendToUser(friends, id);
    }

    private List<UserDto> friendToUser (List<Friend> friends, Long id){
        List<UserDto> friendList = new ArrayList<>();
        for (Friend friend :friends){
            if(friend.getFriendReceiver().getId().equals(id)){
                friendList.add(mapper.map(friend.getFriendRequester(), UserDto.class));
                continue;
            }
            friendList.add(mapper.map(friend.getFriendReceiver(), UserDto.class));
        }

        return friendList;
    }

    public void updateFriendStatus(Long requesterId, Long userId) throws Exception{
        User requester = findById(requesterId);
        User user = findById(userId);
        Friend friend = friendRepository.findByFriendRequesterAndFriendReceiver(requester, user);
        friend.setStatus(FriendStatusEnum.accepted);
        friendRepository.save(friend);
    }

    public void removeFriend(Long userId, Long friendId) throws Exception{
        User user = findById(userId);
        User friendUser = findById(friendId);
        friendRepository.deleteFriend(user, friendUser);
    }

    // public List<User> 
}
