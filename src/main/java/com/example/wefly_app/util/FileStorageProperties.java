package com.example.wefly_app.util;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {
    @Value("${app.file.invoice}")
    private String invoiceDir;
    @Value("${app.file.e-ticket}")
    private String eTicketDir;
}
