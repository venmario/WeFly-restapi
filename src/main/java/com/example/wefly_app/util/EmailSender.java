package com.example.wefly_app.util;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.mail.internet.MimeMessage;

@SuppressWarnings({"WeakerAccess", "ConstantConditions"})
@Component("emailSender")
@Slf4j
public class EmailSender {

    @Autowired
    private JavaMailSenderImpl mailSender;

    @Value("${spring.mail.sender.name:}")
    private String senderName;

    @Value("${spring.mail.sender.mail:}")
    private String senderEmail;

    @Qualifier("taskExecutor")
    @Autowired
    private TaskExecutor taskExecutor;

    public boolean send(String email, String subject, String message) {
        return send(null, email, subject, message);
    }

    public boolean send(String from, String email, String subject, String message) {
        MimeMessage mime = mailSender.createMimeMessage();
        if (StringUtils.isEmpty(from)) {
            from = senderEmail;
        }
        boolean success = false;
        try {
            log.info("Sending email to: " + email);
            log.info("Sending email from: " + from);
            log.info("Sending email with subject: " + subject);

            MimeMessageHelper helper = new MimeMessageHelper(mime, true);
            helper.setFrom(from,senderName);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(message, true);
            mailSender.send(mime);
            success = true;
        } catch (Exception e) {
            log.error("error: " + e.getMessage());
        }

        return success;
    }

    public void sendAsync(final String to, final String subject, final String message) {
        taskExecutor.execute(() -> send(to, subject, message));
    }

}

