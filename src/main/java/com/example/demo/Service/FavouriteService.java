package com.example.demo.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Model.Favourite;
import com.example.demo.Repository.FavouriteRepository;

@Service
public class FavouriteService {
    @Autowired
    private FavouriteRepository favouriteRepository;

    List<Favourite> findFavouriteByUserId(Long id) {
        return favouriteRepository.findByUserId(id);
    }
}
