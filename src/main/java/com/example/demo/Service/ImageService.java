package com.example.demo.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.Model.Image;
import com.example.demo.Model.Post;
import com.example.demo.Repository.ImageRepository;

@Service
public class ImageService {
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private UploadService uploadService;

    @Transactional
    public List<Image>  upload(List<MultipartFile> multiPartFiles, Post post)
        throws Exception {
        List<Image> images = new ArrayList<>();
        for (MultipartFile file : multiPartFiles) {
            Image image = new Image();
            image.setPost(post);
            image.setUrl(uploadService.uploadAndGetUrl(file));
            images.add(image);
        }

        imageRepository.saveAll(images);

        return images;
    }

    @Transactional
    public List<Image> editImage(List<MultipartFile> files, Post post, List<String> removeUrls) throws Exception {
        List<Image> images = post.getImages();
        if (files != null && !files.isEmpty() && !files.getFirst().isEmpty()) {
            images.addAll(upload(files, post));
        }
        if (removeUrls != null && (!removeUrls.isEmpty())) {
            List<Image> removeImages = imageRepository.findAllByUrls(removeUrls);
            images.removeAll(removeImages);
            imageRepository.deleteAll(images);
        }
        return images;
    }
}
