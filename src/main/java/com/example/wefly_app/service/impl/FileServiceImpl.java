package com.example.wefly_app.service.impl;

import com.example.wefly_app.entity.User;
import com.example.wefly_app.repository.UserRepository;
import com.example.wefly_app.service.FileService;
import com.example.wefly_app.util.UploadFileResponse;
import com.example.wefly_app.util.exception.IncorrectUserCredentialException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class FileServiceImpl implements FileService {
    @Value("${app.upload.payment.proof}")//FILE_SHOW_RUL
    private String BASE_UPLOAD_FOLDER;
    @Autowired
    private UserRepository userRepository;
    @Override
    public Map<Object, Object> save(MultipartFile file) throws IOException {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyhhmmss");
        String strDate = formatter.format(date);

        String nameFormat= file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") );
        if(nameFormat.isEmpty()){
            nameFormat = ".png";
        }
        String userFolder = BASE_UPLOAD_FOLDER + "user_" + 1 + "/";
        Path userFolderPath = Paths.get(userFolder);
        if (Files.notExists(userFolderPath)) {
            Files.createDirectories(userFolderPath);
        }
        String fileName = userFolder + strDate + nameFormat;
        Path to = Paths.get(fileName);
        Map<Object, Object> map = new HashMap<>();

        try {
            Files.copy(file.getInputStream(), to);
        } catch (Exception e) {
            log.error("Error while saving file", e);
            map.put("file name", fileName);
            map.put("file download uri", null);
            map.put("file type", file.getContentType());
            map.put("file size", file.getSize());
            map.put("status", e.getMessage());
            return map;
        }

        map.put("file name", fileName);
        map.put("file download uri", null);
        map.put("file type", file.getContentType());
        map.put("file size", file.getSize());
        map.put("status", "success");
        return map;
    }

    @Override
    public Map<Object, Object> get(Long request, Long transactionId) {

        return null;
    }
}
