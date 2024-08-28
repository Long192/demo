package com.example.demo.Controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.example.demo.Dto.Response.CustomPage;
import com.example.demo.Dto.Response.CustomResponse;
import com.example.demo.Dto.Response.PostDto;
import com.example.demo.Enum.OrderEnum;
import com.example.demo.Model.Post;
import com.example.demo.Service.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "post", description = "post")
@RestController
@RequestMapping("/post")
public class PostController {
    @Autowired
    private PostService postService;
    @Autowired
    private ModelMapper modelMapper;

    @Operation(summary = "get posts", description = "get all paginated posts")
    @GetMapping("")
    public ResponseEntity<CustomResponse<CustomPage<PostDto>>> getPosts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "") String search,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") OrderEnum order
    ) throws Exception {
        Sort sort = Sort.by(Sort.Direction.fromString(order.toString()), sortBy);
        PageRequest pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(
            CustomResponse.<CustomPage<PostDto>>builder().data(postService.findAndPaginate(pageable, search)).build()
        );
    }

    @Operation(summary = "get my post", description = "get all post of the currently logged in user")
    @GetMapping("/my-posts")
    public ResponseEntity<CustomResponse<CustomPage<PostDto>>> getMyPosts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "") String search,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") OrderEnum order
    ) throws Exception {
        Sort sort = Sort.by(Sort.Direction.fromString(order.toString()), sortBy);
        PageRequest pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(
            CustomResponse.<CustomPage<PostDto>>builder().data(postService.findMyPostsAndPaginate(pageable, search)).build()
        );
    }

    @Operation(summary = "get post by id", description = "get a post by id of the post")
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<PostDto>> getPostById(@PathVariable String id) throws Exception {
        Post post = postService.findById(Long.valueOf(id));
        return ResponseEntity.ok(CustomResponse.<PostDto>builder().data(modelMapper.map(post, PostDto.class)).build());
    }

    @Operation(summary = "add new post", description = "add a new post")
    @PostMapping("")
    public ResponseEntity<CustomResponse<PostDto>> createPostWithJsonRequest(
        @RequestBody @Valid CreatePostRequest request
    ) throws Exception {
        return ResponseEntity.ok(CustomResponse.<PostDto>builder()
                .data(postService.createPost(request))
                .build());
    }

    @Operation(summary = "like post", description = "like a post")
    @PostMapping("/like")
    public ResponseEntity<CustomResponse<PostDto>> likePost(
        @RequestBody @Valid LikeRequest request
    ) throws Exception {
        return ResponseEntity.ok(CustomResponse.<PostDto>builder().data(postService.like(request.getPostId())).build());
    }

    @Operation(summary = "edit post", description = "edit a post by id")
    @PutMapping("/{id}")
    public ResponseEntity<CustomResponse<PostDto>> editPost(
        @PathVariable Long id,
        @RequestBody @Valid UpdatePostRequest entity
    ) throws Exception {
        return ResponseEntity.ok(CustomResponse.<PostDto>builder().data(postService.editPost(id, entity)).build());
    }

    @Operation(summary = "delete post", description = "delete a post by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<?>> deletePost(@PathVariable Long id) throws Exception {
        postService.deletePostById(id);
        return ResponseEntity.ok(new CustomResponse<>());
    }
}
