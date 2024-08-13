package com.example.demo.Controller;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Dto.Request.CreatePostRequest;
import com.example.demo.Dto.Request.LikeRequest;
import com.example.demo.Dto.Request.UpdatePostRequest;
import com.example.demo.Dto.Response.ApiResponse;
import com.example.demo.Dto.Response.MessageResponse;
import com.example.demo.Dto.Response.PostDto;
import com.example.demo.Model.Post;
import com.example.demo.Service.PostService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/post")
public class PostController {
    @Autowired
    private PostService postService;
    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("")
    public ApiResponse<Page<PostDto>> getPosts(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<Page<PostDto>> builder().data(postService.findAndPaginate(page, size)).build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PostDto> getPostById(@PathVariable String id) throws Exception {
        Post post = postService.findById(Long.valueOf(id));
        return ApiResponse.<PostDto> builder().data(modelMapper.map(post, PostDto.class)).build();
    }

    @GetMapping("/get-all")
    public ApiResponse<List<PostDto>> getAll() {
        return ApiResponse.<List<PostDto>> builder().data(postService.findAll()).build();
    }

    @GetMapping("/get-friend-post")
    public ApiResponse<List<PostDto>> getFriendPost(@RequestParam String param) {
        
        return ApiResponse.<List<PostDto>> builder().data(new ArrayList<PostDto>()).build();
    }

    @PostMapping(value = "", consumes = { "multipart/form-data" })
    public ApiResponse<MessageResponse> createPost(@ModelAttribute @Valid CreatePostRequest request) throws Exception {
        postService.createPost(request);
        return ApiResponse.<MessageResponse> builder().data(new MessageResponse()).build();
    }

    @PostMapping("/like")
    public ApiResponse<MessageResponse> likePost(@RequestBody @Valid LikeRequest request) throws Exception {
        postService.like(request.getPostId());
        return ApiResponse.<MessageResponse> builder().data(new MessageResponse()).build();
    }

    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    public ApiResponse<MessageResponse> editPost(@PathVariable String id,
            @ModelAttribute @Valid UpdatePostRequest entity) throws Exception {
        postService.editPost(id, entity);
        return ApiResponse.<MessageResponse> builder().data(new MessageResponse()).build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<MessageResponse> deletePost(@PathVariable String id) {
        postService.deletePostById(id);
        return ApiResponse.<MessageResponse> builder().data(new MessageResponse()).build();
    }
}
