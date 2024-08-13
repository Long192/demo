package com.example.demo.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.Model.Image;
import com.example.demo.Model.Post;
import com.example.demo.Repository.ImageRepository;
import com.uploadcare.api.Client;
import com.uploadcare.upload.FileUploader;
import com.uploadcare.upload.UploadFailureException;
import com.uploadcare.upload.Uploader;
import com.uploadcare.urls.CdnPathBuilder;
import com.uploadcare.urls.Urls;

@Service
public class ImageService {
    @Autowired
    private Client client;
    @Autowired
    private ImageRepository imageRepository;

    public List<Image> upload(List<MultipartFile> multiPartFiles, Post post)
            throws UploadFailureException, MalformedURLException {
        List<Image> images = new ArrayList<>();
        for (MultipartFile file : multiPartFiles) {
            Image image = new Image();
            image.setPost(post);
            image.setUrl(uploadAndGetUrl(file));
            images.add(image);
        }
        imageRepository.saveAll(images);
        return images;
    }

    public String uploadAndGetUrl(MultipartFile multiPartFiles) throws UploadFailureException, MalformedURLException {
        Uploader uploader = new FileUploader(client, convertToFile(multiPartFiles));
        com.uploadcare.api.File uploadedFile = uploader.upload();
        String fileId = uploadedFile.getFileId();
        com.uploadcare.api.File fileResponse = client.getFile(fileId);
        CdnPathBuilder builder = fileResponse.cdnPath();
        String url = Urls.cdn(builder).toURL().toString();
        return url;
    }

    public List<Image> editImage(List<MultipartFile> files, Post post, List<String> removeUrls)
            throws MalformedURLException, UploadFailureException {
        List<Image> images = post.getImages();
        if (files != null && !files.isEmpty() && !files.getFirst().isEmpty()) {
            images.addAll(upload(files, post));
        }
        if (removeUrls != null && (!removeUrls.isEmpty())) {
            List<Image> removeImages = imageRepository.findAllByUrls(removeUrls);
            // for(Image image: removeImages){
            // client.getFile(image.getUrl().substring(20, 56)).delete();
            // }
            images.removeAll(removeImages);
        }
        return images;
    }

    public void removeImage(List<Image> removeImages) {
        imageRepository.deleteAll(removeImages);
    }

    private File convertToFile(MultipartFile multiPartFile) {
        File file = new File(Objects.requireNonNull(multiPartFile.getOriginalFilename()));
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multiPartFile.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }
}
