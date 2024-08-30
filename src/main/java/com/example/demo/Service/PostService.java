package com.example.demo.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Dto.Request.CreatePostRequest;
import com.example.demo.Dto.Request.UpdatePostRequest;
import com.example.demo.Dto.Response.CustomPage;
import com.example.demo.Dto.Response.PostDto;
import com.example.demo.Enum.PostStatusEnum;
import com.example.demo.Enum.StatusEnum;
import com.example.demo.Exception.CustomException;
import com.example.demo.Model.Favourite;
import com.example.demo.Model.Image;
import com.example.demo.Model.Post;
import com.example.demo.Model.User;
import com.example.demo.Repository.PostRepository;

@Service
public class PostService {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ImageService imageService;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private FavouriteService favouriteService;
    @Autowired
    private FriendService friendService;
    @Autowired
    private ModelMapper mapper;

    @Transactional
    public PostDto createPost(CreatePostRequest request) throws Exception {
        Post post = new Post();
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        post.setContent(request.getContent());
        post.setImages(imageService.saveImages(request.getImages(), post));
        post.setUser(user);
        try{
            post.setStatus(request.getStatus() != null ? PostStatusEnum.valueOf(request.getStatus()) : PostStatusEnum.PUBLIC);
        }catch(IllegalArgumentException e){
            throw new Exception("status only accept 'PRIVATE', 'PUBLIC' or 'FRIEND_ONLY'");
        }
        Post result = postRepository.save(post);
        return modelMapper.map(result, PostDto.class);
    }

        public CustomPage<PostDto> getFriendPost(Pageable pageable) throws Exception {
        List<Long> friendIds = new ArrayList<>();
        friendService.getAllFriend().forEach(user -> friendIds.add(user.getId()));
        try {
            return mapper.map(findByUserIdsOrderByCreatedAt(friendIds, pageable),
                    new TypeToken<CustomPage<PostDto>>() {}.getType());
        } catch (InvalidDataAccessApiUsageException e) {
            throw new Exception("wrong sort by");
        }
    }

    private Page<PostDto> findByUserIdsOrderByCreatedAt(List<Long> ids, Pageable pageable) {
        Page<Post> posts = postRepository.findByUserIdsOrderByCreatedAt(ids, pageable);
        return posts.map(source -> modelMapper.map(source, PostDto.class));
    }

    public CustomPage<PostDto> findAndPaginate(Pageable pageable, String textSearch) throws Exception {
        try{
            Page<Post> postPage = postRepository.findPostWithSearchAndSort(textSearch, StatusEnum.active, pageable);
            return  modelMapper.map(postPage, new TypeToken<CustomPage<PostDto>>() {}.getType());
        }catch (InvalidDataAccessApiUsageException e){
            throw new Exception("wrong sort by");
        }
    }

    public CustomPage<PostDto> findMyPostsAndPaginate(Pageable pageable, String search) throws Exception {
        User me = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try{
            Page<Post> postPage = postRepository.getPostByUserIdAndSearch(me.getId(), pageable, search);
            return modelMapper.map(postPage, new TypeToken<CustomPage<PostDto>>() {}.getType());
        }catch(InvalidDataAccessApiUsageException e){
            throw new Exception("wrong sort by");
        }
    }

    public Post findById(Long id) throws Exception {
        return postRepository.findById(id).orElseThrow(() -> new CustomException(404, "post not found"));
    }

    @Transactional
    public PostDto editPost(Long id, UpdatePostRequest data) throws Exception {
        Post post = postRepository.findById(id).orElseThrow(() -> new CustomException(404, "post not found"));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!post.getUser().getId().equals(user.getId())) {
            throw new CustomException(403, "you don't have permission to edit this post");
        }
        post.setContent(data.getContent());
        try{
            post.setStatus(PostStatusEnum.valueOf(data.getStatus()));
        }catch(IllegalArgumentException e){
            throw new Exception("status only accept 'PRIVATE', 'PUBLIC' or 'FRIEND_ONLY'");
        }
        List<Image> images = imageService.editImage(data.getImages(), post);
        post.getImages().clear();
        post.getImages().addAll(images);
        if (post.getContent() == null && post.getImages().isEmpty()) {
            throw new Exception("cannot delete all content and image");
        }
        Post result = postRepository.save(post);

        return modelMapper.map(result, PostDto.class);
    }

    public PostDto like(Long postId) throws Exception {
        User me = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.findById(me.getId());
        Post post = findById(postId);
        Optional<Favourite> favourite = favouriteService.findByUserIdAndPostId(user.getId(), post.getId());
        if (favourite.isPresent()) {
            favouriteService.delete(favourite.get());
            return null;
        }

        Favourite newFavourite = Favourite.builder().post(post).user(user).build();

        return modelMapper.map(favouriteService.save(newFavourite), PostDto.class);
        
    }

    @Transactional
    public void deletePostById(Long id) throws Exception {
        Post post = findById(id);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!post.getUser().getId().equals(user.getId())){
            throw new CustomException(403, "you don't have permission to delete this post");
        }

        for(User likeUser: post.getLikedByUsers()){
            likeUser.getLikePosts().remove(post);
            userService.save(likeUser);
        }

        postRepository.delete(post);
    }

}
