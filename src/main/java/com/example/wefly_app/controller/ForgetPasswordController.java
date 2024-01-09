package com.example.wefly_app.controller;

import com.example.wefly_app.entity.User;
import com.example.wefly_app.repository.UserRepository;
import com.example.wefly_app.request.ForgotPasswordModel;
import com.example.wefly_app.request.ResetPasswordModel;
import com.example.wefly_app.service.UserService;
import com.example.wefly_app.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/v1/forget-password/")
@Slf4j
public class ForgetPasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public UserService serviceReq;
    @Autowired
    public PasswordValidatorUtil passwordValidatorUtil;

    @Value("${expired.token.password.minute:}")//FILE_SHOW_RUL
    private int expiredToken;
    @Autowired
    public TemplateResponse templateResponse;

    @Autowired
    public EmailTemplate emailTemplate;

    @Autowired
    public EmailSender emailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Step 1 : Send OTP
    @PostMapping("/forgot-password")//send OTP//send OTP
    public ResponseEntity<Map> sendEmailPassword(@Valid @RequestBody ForgotPasswordModel user) {
        if (StringUtils.isEmpty(user.getUsername())) return new ResponseEntity<Map>(templateResponse.error("No email provided"), HttpStatus.BAD_REQUEST);
        User found = userRepository.findOneByUsername(user.getUsername());
        if (found == null) return new ResponseEntity<Map>(templateResponse.notFound("Email Not Found"), HttpStatus.NOT_FOUND);

        String template = emailTemplate.getResetPassword();
        if (StringUtils.isEmpty(found.getOtp())) {
            User search;
            String otp;
            do {
                otp = SimpleStringUtils.randomString(6, true);
                search = userRepository.findOneByOTP(otp);
            } while (search != null);
            Date dateNow = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateNow);
            calendar.add(Calendar.MINUTE, expiredToken);
            Date expirationDate = calendar.getTime();

            found.setOtp(otp);
            found.setOtpExpiredDate(expirationDate);
            template = template.replaceAll("\\{\\{PASS_TOKEN}}", otp);
            template = template.replaceAll("\\{\\{USERNAME}}", (found.getUsername() == null ? "" +
                    "UserName"
                    :
                    found.getUsername()));

            userRepository.save(found);
        } else {
            template = template.replaceAll("\\{\\{USERNAME}}", (found.getUsername() == null ? "" +
                    "UserName"
                    :
                    found.getUsername()));
            template = template.replaceAll("\\{\\{PASS_TOKEN}}", found.getOtp());
        }
        emailSender.sendAsync(found.getUsername(), "Chute - Forget Password", template);


        return new ResponseEntity<Map>(templateResponse.success("Success, Please Check Your Email"), HttpStatus.OK);

    }

    // Step 2 : lakukan reset password baru
    @Transactional
    @PostMapping("/change-password")
    public ResponseEntity<Map> resetPassword(@Valid @RequestBody ResetPasswordModel model) {
        try {
            if (model.getOtp() == null) return new ResponseEntity<Map>(templateResponse.error("Token is required"), HttpStatus.BAD_REQUEST);
            if (model.getNewPassword() == null) return new ResponseEntity<Map>(templateResponse.error("New Password Must not Null"), HttpStatus.BAD_REQUEST);
            User user = userRepository.findOneByOTP(model.getOtp());
            String success;
            if (user == null) return new ResponseEntity<Map>(templateResponse.error("OTP is not valid"), HttpStatus.BAD_REQUEST);

            if (!passwordValidatorUtil.validatePassword(model.getNewPassword())) {
                return new ResponseEntity<Map>(templateResponse.error(passwordValidatorUtil.getMessage()), HttpStatus.BAD_REQUEST);
            }
            if (!model.getNewPassword().matches(model.getConfirmPassword())) {
                return new ResponseEntity<Map>(templateResponse.error("Confirm Password not Match"), HttpStatus.BAD_REQUEST);
            }
            user.setPassword(passwordEncoder.encode(model.getNewPassword().replaceAll("\\s+", "")));
            user.setOtpExpiredDate(null);
            user.setOtp(null);

            userRepository.save(user);
            log.info("reset password success");
            return new ResponseEntity<Map>(templateResponse.success("Reset Password Succeed"), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<Map>(templateResponse.error("Failed to Reset Password"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/check-token/{otp}")
    public ResponseEntity<Map> cheKTOkenValid(@PathVariable(value = "otp") String otp) {
        if (otp == null) return new ResponseEntity<Map>(templateResponse.error("OTP is Required"), HttpStatus.BAD_REQUEST);

        User user = userRepository.findOneByOTP(otp);
        if (user == null) {
            return new ResponseEntity<Map>(templateResponse.error("OTP is not valid"), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Map>(templateResponse.success("OTP is valid"), HttpStatus.OK);
    }

}
