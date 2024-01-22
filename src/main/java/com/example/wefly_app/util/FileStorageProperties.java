package com.example.wefly_app.util;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {
    @Value("${app.upload.payment.proof}")//FILE_SHOW_RUL
    private String uploadDir;
}
