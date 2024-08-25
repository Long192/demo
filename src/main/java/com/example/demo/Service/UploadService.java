package com.example.demo.Service;

import com.uploadcare.api.Client;
import com.uploadcare.upload.FileUploader;
import com.uploadcare.upload.Uploader;
import com.uploadcare.urls.CdnPathBuilder;
import com.uploadcare.urls.Urls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

@Service
public class UploadService {
    @Autowired
    private Client client;

    public String uploadAndGetUrl(MultipartFile multiPartFiles) throws Exception {
        BufferedImage image = ImageIO.read(convertToFile(multiPartFiles));
        if (image == null) {
            throw new Exception("invalid image");
        }
        Uploader uploader = new FileUploader(client, convertToFile(multiPartFiles));
        com.uploadcare.api.File uploadedFile = uploader.upload();
        String fileId = uploadedFile.getFileId();
        com.uploadcare.api.File fileResponse = client.getFile(fileId);
        CdnPathBuilder builder = fileResponse.cdnPath();
        return Urls.cdn(builder).toURL().toString();
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
