package com.example.demo.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Model.Favourite;
import com.example.demo.Repository.FavouriteRepository;

@Service
public class FavouriteService {
    @Autowired
    private FavouriteRepository favouriteRepository;

    public List<Favourite> findFavouriteByUserId(Long id) {
        return favouriteRepository.findByUserId(id);
    }

    public Optional<Favourite> findByUserIdAndPostId(Long userId, Long PostId) {
        return favouriteRepository.findByUserIdAndPostId(userId, PostId);
    }

    public Favourite save(Favourite favourite) {
        return favouriteRepository.save(favourite);
    }

    public void delete(Favourite favourite){
        favouriteRepository.delete(favourite);
    }

    public Optional<Favourite> findById(Long id){
        return favouriteRepository.findById(id);
    }
}
