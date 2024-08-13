package com.example.demo.Service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.Enum.StatusEnum;
import com.example.demo.Dto.Request.CreatePostRequest;
import com.example.demo.Dto.Request.UpdatePostRequest;
import com.example.demo.Dto.Response.PostDto;
import com.example.demo.Model.Post;
import com.example.demo.Model.User;
import com.example.demo.Repository.PostRepository;
import com.example.demo.Repository.UserRepository;

@Service
public class PostService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ImageService imageService;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserService userService;

    public void createPost(CreatePostRequest request) throws Exception {
        Post post = new Post();
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        post.setContent(request.getContent());
        post.setUser(user);
        post.setStatus(StatusEnum.active);
        postRepository.save(post);
        if (!request.getImages().getFirst().isEmpty()) {
            imageService.upload(request.getImages(), post);
        }
    }

    public Page<PostDto> findAndPaginate(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.findAll(pageable);
        return postPage.map(source -> modelMapper.map(source, PostDto.class));
    }

    public Post findById(Long id) throws Exception {
        return postRepository.findById(id).orElseThrow(() -> new Exception("post not found"));
    }

    public List<PostDto> findAll() {
        List<Post> posts = postRepository.findAll();
        return modelMapper.map(posts, new TypeToken<List<PostDto>>() {}.getType());
    }

    public void editPost(String id, UpdatePostRequest data) throws Exception {
        Post post = postRepository.findById(Long.valueOf(id)).orElseThrow(() -> new Exception("post not found"));
        Post existPost = post;
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!existPost.getUser().getId().equals(user.getId())) {
            throw new Exception("you don't have permisson to edit this post");
        }
        existPost.setContent(data.getContent());
        existPost.setStatus(post.getStatus());
        existPost.setImages(imageService.editImage(data.getImages(), post, data.getRemoveImages()));
        if (existPost.getContent() == null && existPost.getImages().isEmpty()) {
            throw new Exception("cannot delete all content and image");
        }
        postRepository.save(existPost);
    }

    public void like(Long postId) throws Exception {
        User me = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.findById(me.getId());
        Post post = findById(postId);
        if (post.getLikedByUsers().contains(user)) {
            post.getLikedByUsers().remove(user);
            user.getLikePosts().remove(post);
        } else {
            post.getLikedByUsers().add(user);
            user.getLikePosts().add(post);
        }
        userRepository.save(user);
        postRepository.save(post);
    }

    public void deletePostById(String id) {
        postRepository.deleteById(Long.valueOf(id));
    }

    // public List<PostDto> getFriendPost(){
    //     User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    //     userService.
    // }
}
