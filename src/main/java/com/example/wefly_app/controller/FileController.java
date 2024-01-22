package com.example.wefly_app.controller;

import com.example.wefly_app.service.FileService;
import com.example.wefly_app.service.FileStorageService;
import com.example.wefly_app.util.UploadFileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@RestController
@EnableCaching
@RequestMapping("/v1/file")
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    
    @Value("${app.upload.payment.proof}")//FILE_SHOW_RUL
    private String BASE_UPLOAD_FOLDER;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private FileService fileService;


    @PostMapping(value = "/upload", consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity<Map> uploadFile(@RequestParam("file") MultipartFile file,
                                          @RequestParam("transactionId")Long id) throws IOException {
        return new ResponseEntity<>(fileService.save(file), HttpStatus.OK);
    }

//    @GetMapping("/showFile/{fileName:.+}")
//    public ResponseEntity<Resource> showFile(@PathVariable String fileName, HttpServletRequest request) {
//        // Load file as Resource : step 1 load path lokasi name file
//        Resource resource = fileStorageService.loadFileAsResource(fileName);
//        // Try to determine file's content type
//        String contentType = null;
//        try {
//            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
//
//        } catch (IOException ex) {
//            logger.info("Could not determine file type.");
//        }
//        // Fallback to the default content type if type could not be determined
//        if (contentType == null) {
//            contentType = "application/octet-stream";// type .json
//        }
//        return ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType(contentType))
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
//                .body(resource);
//    }


}

