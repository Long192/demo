package com.example.demo.Dto.Response;

import java.util.List;

import com.example.demo.Enum.FriendStatusEnum;
import com.example.demo.Model.User;

import lombok.Data;

@Data
public class FriendDto {
    private Long id;
    private List<User> user;
    private FriendStatusEnum status;
}
