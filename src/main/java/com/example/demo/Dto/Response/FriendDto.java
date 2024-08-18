package com.example.demo.Dto.Response;

import com.example.demo.Enum.FriendStatusEnum;
import com.example.demo.Model.User;
import lombok.Data;

import java.util.List;

@Data
public class FriendDto {
    private Long id;
    private List<User> user;
    private FriendStatusEnum status;
}
