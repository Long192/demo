package com.example.demo.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Model.Image;
import com.example.demo.Model.Post;
import com.example.demo.Repository.ImageRepository;

@Service
public class ImageService {
    @Autowired
    private ImageRepository imageRepository;

    @Transactional
    public List<Image> saveImages(List<String> urls, Post post){
        if(urls == null || urls.isEmpty()){
            return null;
        }
        
        List<Image> images = new ArrayList<>();
        for (String url : urls) {
            Image image = new Image();
            image.setPost(post);
            image.setUrl(url);
            images.add(image);
        }
        return imageRepository.saveAll(images);
    }

    @Transactional
    public List<Image> editImage(List<String> urls, Post post){
        List<Image> images = new ArrayList<>();

        urls.forEach(url -> {
            if(post.getImages().stream().noneMatch(image -> image.getUrl().equals(url))){
                Image image = new Image();
                image.setPost(post);
                image.setUrl(url);
                images.add(image);
                return;
            }

            Image image = post.getImages().stream().filter(i -> i.getUrl().equals(url)).findFirst().get();
            image.setPost(post);
            images.add(image);
        });

        return images;
    }
}
