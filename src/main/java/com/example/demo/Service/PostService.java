package com.example.demo.Service;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public void createPost(CreatePostRequest request) throws Exception {
        Post post = new Post();
        Optional<User> user = userRepository.findById(request.getUserId());
        post.setContent(request.getContent());
        user.ifPresent(post::setUser);
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

    public PostDto findById(Long id) throws Exception {
        return modelMapper.map(findPostById(id).get(), PostDto.class);
    }

    public Optional<Post> findPostById(Long id) throws Exception {
        Optional<Post> post = postRepository.findById(Long.valueOf(id));
        if (!post.isPresent()) {
            throw new Exception("post not found");
        }
        return post;
    }

    public List<PostDto> findAll() {
        List<Post> posts = postRepository.findAll();
        return modelMapper.map(posts, new TypeToken<List<PostDto>>() {}.getType());
    }

    public void editPost(String id, UpdatePostRequest data) throws Exception {
        Optional<Post> post = postRepository.findById(Long.valueOf(id));
        if (!post.isPresent()) {
            throw new Exception("post not found");
        }
        Post existPost = post.get();
        if (!existPost.getUser().getId().equals(data.getUserId())) {
            throw new Exception("you don't have permisson to edit this post");
        }
        existPost.setContent(data.getContent());
        existPost.setStatus(post.get().getStatus());
        existPost.setImages(imageService.editImage(data.getImages(), post.get(), data.getRemoveImages()));
        if (existPost.getContent() == null && existPost.getImages().isEmpty()) {
            throw new Exception("cannot delete all content and image");
        }
        postRepository.save(existPost);
    }

    public void deletePostById(String id) {
        postRepository.deleteById(Long.valueOf(id));
    }
}
