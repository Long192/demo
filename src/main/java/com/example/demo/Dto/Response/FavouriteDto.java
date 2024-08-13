package com.example.demo.Dto.Response;

import lombok.Data;

@Data
public class FavouriteDto {
    private Long id;
    private UserDto user;
    private PostDto post;
}
