package com.example.wefly_app.util;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {
    @Value("${app.file.payment-proof}")
    private String paymentProofDir;
    @Value("${app.file.e-ticket}")
    private String eTicketDir;
    @Value("${app.file.boarding-pass}")
    private String boardingPassDir;
}
