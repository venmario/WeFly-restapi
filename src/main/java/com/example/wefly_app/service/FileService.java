package com.example.wefly_app.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface FileService {
    Map<Object, Object> save(MultipartFile file) throws IOException;
    Map<Object, Object> get(Long request, Long transactionId);
}
