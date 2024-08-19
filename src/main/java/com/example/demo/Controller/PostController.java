package com.example.demo.Controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
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
import com.example.demo.Dto.Response.CustomResponse;
import com.example.demo.Dto.Response.MessageResponse;
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
    public ResponseEntity<CustomResponse<Page<PostDto>>> getPosts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "") String search,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") OrderEnum order
    ) throws Exception {
        Sort sort = Sort.by(Sort.Direction.fromString(order.toString()), sortBy);
        PageRequest pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(
            CustomResponse.<Page<PostDto>>builder().data(postService.findAndPaginate(pageable, search)).build()
        );
    }

    @Operation(summary = "get my post", description = "get all post of the currently logged in user")
    @GetMapping("/my-posts")
    public ResponseEntity<CustomResponse<Page<PostDto>>> getMyPosts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "") String search,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") OrderEnum order
    ) throws Exception {
        Sort sort = Sort.by(Sort.Direction.fromString(order.toString()), sortBy);
        PageRequest pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(
            CustomResponse.<Page<PostDto>>builder().data(postService.findMyPostsAndPaginate(pageable, search)).build()
        );
    }

    @Operation(summary = "get post by id", description = "get a post by id of the post")
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<PostDto>> getPostById(@PathVariable String id) throws Exception {
        Post post = postService.findById(Long.valueOf(id));
        return ResponseEntity.ok(CustomResponse.<PostDto>builder().data(modelMapper.map(post, PostDto.class)).build());
    }

    @Operation(summary = "add new post", description = "add a new post, use formdata if you want to upload file")
    @PostMapping(value = "", consumes = {"multipart/form-data"})
    public ResponseEntity<CustomResponse<MessageResponse>> createPostWithFormDataRequest(
        @ModelAttribute @Valid CreatePostRequest request
    ) throws Exception {
        postService.createPost(request);
        return ResponseEntity.ok(CustomResponse.<MessageResponse>builder().data(new MessageResponse()).build());
    }

    @Operation(summary = "add new post", description = "add a new post, use formdata if you want to upload file")
    @PostMapping(value = "", consumes = {"application/json"})
    public ResponseEntity<CustomResponse<MessageResponse>> createPostWithJsonRequest(
        @RequestBody @Valid CreatePostRequest request
    ) throws Exception {
        postService.createPost(request);
        return ResponseEntity.ok(CustomResponse.<MessageResponse>builder().data(new MessageResponse()).build());
    }

    @Operation(summary = "like post", description = "like a post")
    @PostMapping("/like")
    public ResponseEntity<CustomResponse<MessageResponse>> likePost(
        @RequestBody @Valid LikeRequest request
    ) throws Exception {
        postService.like(request.getPostId());
        return ResponseEntity.ok(CustomResponse.<MessageResponse>builder().data(new MessageResponse()).build());
    }

    @Operation(summary = "edit post", description = "edit a post by id")
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<CustomResponse<MessageResponse>> editPost(
        @PathVariable Long id,
        @ModelAttribute @Valid UpdatePostRequest entity
    ) throws Exception {
        postService.editPost(id, entity);
        return ResponseEntity.ok(CustomResponse.<MessageResponse>builder().data(new MessageResponse()).build());
    }

    @Operation(summary = "delete post", description = "delete a post by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<MessageResponse>> deletePost(@PathVariable Long id) {
        postService.deletePostById(id);
        return ResponseEntity.ok(CustomResponse.<MessageResponse>builder().data(new MessageResponse()).build());
    }
}
