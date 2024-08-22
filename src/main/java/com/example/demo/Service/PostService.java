package com.example.demo.Service;

import java.sql.Timestamp;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Dto.Request.CreatePostRequest;
import com.example.demo.Dto.Request.UpdatePostRequest;
import com.example.demo.Dto.Response.PostDto;
import com.example.demo.Enum.StatusEnum;
import com.example.demo.Exception.CustomException;
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

    @Transactional
    public void createPost(CreatePostRequest request) throws Exception {
        Post post = new Post();
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        post.setContent(request.getContent());
        post.setUser(user);
        post.setStatus(StatusEnum.active);
        postRepository.save(post);
        if (request.getImages() != null && !request.getImages().getFirst().isEmpty()) {
            imageService.upload(request.getImages(), post);
        }
    }

    public Page<PostDto> findPostByUserIdsAndCreatedAt(List<Long> ids, Timestamp timestamp, Pageable pageable) {
        Page<Post> posts = postRepository.findPostByUserIdsAndCreatedAt(ids, timestamp, pageable);
        return posts.map(source -> modelMapper.map(source, PostDto.class));
    }

    public Page<PostDto> findAndPaginate(Pageable pageable, String textSearch) throws Exception {
        try{
            Page<Post> postPage = postRepository.findPostWithSearchAndSort(textSearch, StatusEnum.active, pageable);
            return postPage.map(source -> modelMapper.map(source, PostDto.class));
        }catch (InvalidDataAccessApiUsageException e){
            throw new Exception("wrong sort by");
        }
    }

    public Page<PostDto> findMyPostsAndPaginate(Pageable pageable, String search) throws Exception {
        User me = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try{
            Page<Post> postPage = postRepository.getPostByUserIdAndSearch(me.getId(), pageable, search);
            return postPage.map(source -> modelMapper.map(source, PostDto.class));
        }catch(InvalidDataAccessApiUsageException e){
            throw new Exception("wrong sort by");
        }
    }

    public Post findById(Long id) throws Exception {
        return postRepository.findById(id).orElseThrow(() -> new CustomException(404, "post not found"));
    }

    @Transactional
    public void editPost(Long id, UpdatePostRequest data) throws Exception {
        Post post = postRepository.findById(id).orElseThrow(() -> new CustomException(404, "post not found"));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!post.getUser().getId().equals(user.getId())) {
            throw new CustomException(403, "you don't have permission to edit this post");
        }
        post.setContent(data.getContent());
        post.setStatus(data.getStatus());
        post.setImages(imageService.editImage(data.getImages(), post, data.getRemoveImages()));
        if (post.getContent() == null && post.getImages().isEmpty()) {
            throw new Exception("cannot delete all content and image");
        }
        postRepository.save(post);
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

    public void deletePostById(Long id) throws Exception {
        Post post = findById(id);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!post.getUser().getId().equals(user.getId())){
            throw new CustomException(403, "you don't have permission to delete this post");
        }

        postRepository.delete(post);
    }

}
