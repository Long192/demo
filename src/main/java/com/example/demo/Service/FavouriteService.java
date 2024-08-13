package com.example.demo.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Dto.Request.FavouriteRequest;
import com.example.demo.Model.Favourite;
import com.example.demo.Model.Post;
import com.example.demo.Model.User;
import com.example.demo.Repository.FavouriteRepository;

@Service
public class FavouriteService {
    @Autowired
    private FavouriteRepository favouriteRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private PostService postService;

    public void like (FavouriteRequest request) throws Exception{
        Favourite removeFavourite = favouriteRepository.findByUserIdAndPostId(request.getUserId(), request.getPostId());
        if(removeFavourite != null){
            favouriteRepository.delete(removeFavourite);
            return;
        }
        Favourite addFavourite = new Favourite();
        Optional<User> user = userService.findById(request.getUserId());
        Optional<Post> post = postService.findPostById(request.getPostId());
        addFavourite.setPost(post.get());
        addFavourite.setUser(user.get());
        favouriteRepository.save(addFavourite);
    }
}
