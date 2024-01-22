package com.example.wefly_app.service;
import com.example.wefly_app.util.FileStorageProperties;
import com.example.wefly_app.util.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class FileStorageService {
    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("ddMyyyyhhmmss");
    String strDate = formatter.format(date);

//    @Autowired
//    public FileStorageService(FileStorageProperties fileStorageProperties) {
//        this.
//    }

    public Resource loadFileAsResource(String fileName) {
        try {
            FileStorageProperties fileStorageProperties = new FileStorageProperties();
            Path fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                    .toAbsolutePath().normalize();
            Files.createDirectories(fileStorageLocation);
            Path filePath = fileStorageLocation.resolve(fileName).normalize();

            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                System.out.println("ini saya= "+filePath);
                System.out.println("ini saya 2= "+filePath.toUri());
                System.out.println("ini saya 3= "+filePath.toAbsolutePath());
                throw new FileStorageException("File not found " + fileName);
            }
        } catch (IOException e) {
            throw new FileStorageException("File not found " + fileName, e);
        }
    }
}

