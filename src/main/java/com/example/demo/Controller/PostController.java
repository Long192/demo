package com.example.demo.Controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public CustomResponse<Page<PostDto>> getPosts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "") String search,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String order
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageable = PageRequest.of(page, size, sort);
        return CustomResponse.<Page<PostDto>> builder().data(postService.findAndPaginate(pageable, search)).build();
    }

    @Operation(summary = "get my post", description = "get all post of the currently logged in user")
    @GetMapping("/my-posts")
    public CustomResponse<Page<PostDto>> getMyPosts(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws Exception {
        return CustomResponse.<Page<PostDto>> builder().data(postService.findMyPostsAndPaginate(page, size)).build();
    }

    @Operation(summary = "get post by id", description = "get a post by id of the post")
    @GetMapping("/{id}")
    public CustomResponse<PostDto> getPostById(@PathVariable String id) throws Exception {
        Post post = postService.findById(Long.valueOf(id));
        return CustomResponse.<PostDto> builder().data(modelMapper.map(post, PostDto.class)).build();
    }

    @Operation(summary = "get all post", description = "get all post without paginate")
    @GetMapping("/all")
    public CustomResponse<List<PostDto>> getAll() {
        return CustomResponse.<List<PostDto>> builder().data(postService.findAll()).build();
    }

    @Operation(summary = "add new post", description = "add a new post, use formdata if you want to upload file")
    @PostMapping(value = "", consumes = { "multipart/form-data" })
    public CustomResponse<MessageResponse> createPostWithFormDataRequest(@ModelAttribute @Valid CreatePostRequest request)
            throws Exception {
        postService.createPost(request);
        return CustomResponse.<MessageResponse> builder().data(new MessageResponse()).build();
    }
    
    @Operation(summary = "add new post", description = "add a new post, use formdata if you want to upload file")
    @PostMapping(value = "", consumes = { "application/json" })
    public CustomResponse<MessageResponse> createPostWithJsonRequest(@RequestBody @Valid CreatePostRequest request)
            throws Exception {
        postService.createPost(request);
        return CustomResponse.<MessageResponse> builder().data(new MessageResponse()).build();
    }

    @Operation(summary = "like post", description = "like a post")
    @PostMapping("/like")
    public CustomResponse<MessageResponse> likePost(@RequestBody @Valid LikeRequest request) throws Exception {
        postService.like(request.getPostId());
        return CustomResponse.<MessageResponse> builder().data(new MessageResponse()).build();
    }

    @Operation(summary = "edit post", description = "edit a post by id")
    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    public CustomResponse<MessageResponse> editPost(@PathVariable String id,
            @ModelAttribute @Valid UpdatePostRequest entity) throws Exception {
        postService.editPost(id, entity);
        return CustomResponse.<MessageResponse> builder().data(new MessageResponse()).build();
    }

    @Operation(summary = "delete post", description = "delete a post by id")
    @DeleteMapping("/{id}")
    public CustomResponse<MessageResponse> deletePost(@PathVariable String id) {
        postService.deletePostById(id);
        return CustomResponse.<MessageResponse> builder().data(new MessageResponse()).build();
    }
}
