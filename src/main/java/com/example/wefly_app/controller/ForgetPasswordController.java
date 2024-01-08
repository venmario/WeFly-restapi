package com.example.wefly_app.controller;

import com.example.wefly_app.entity.User;
import com.example.wefly_app.repository.UserRepository;
import com.example.wefly_app.request.ResetPasswordModel;
import com.example.wefly_app.service.UserService;
import com.example.wefly_app.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/v1/forget-password/")
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
    public Map sendEmailPassword(@RequestBody ResetPasswordModel user) {
        if (StringUtils.isEmpty(user.getUsername())) return templateResponse.error("No username provided");
        User found = userRepository.findOneByUsername(user.getUsername());
        if (found == null) return templateResponse.error("Email not found"); //throw new BadRequest("Email not found");

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
                    "@UserName"
                    :
                    "@" + found.getUsername()));

            userRepository.save(found);
        } else {
            template = template.replaceAll("\\{\\{USERNAME}}", (found.getUsername() == null ? "" +
                    "@UserName"
                    :
                    "@" + found.getUsername()));
            template = template.replaceAll("\\{\\{PASS_TOKEN}}", found.getOtp());
        }
        emailSender.sendAsync(found.getUsername(), "Chute - Forget Password", template);


        return templateResponse.success("success, please check your email");

    }

    // Step 2 : lakukan reset password baru
    @PostMapping("/change-password")
    public Map resetPassword(@RequestBody ResetPasswordModel model) {
        if (model.getOtp() == null) return templateResponse.error("Token is required");
        if (model.getNewPassword() == null) return templateResponse.error("New Password is required");
        User user = userRepository.findOneByOTP(model.getOtp());
        String success;
        if (user == null) return templateResponse.error("Token not valid");
        
        if (!passwordValidatorUtil.validatePassword(model.getNewPassword())) {
            return templateResponse.error(passwordValidatorUtil.getMessage());
        }
        if (!model.getNewPassword().matches(model.getConfirmPassword())) {
            return templateResponse.error("password does not match");
        }
        user.setPassword(passwordEncoder.encode(model.getNewPassword().replaceAll("\\s+", "")));
        user.setOtpExpiredDate(null);
        user.setOtp(null);

        try {
            userRepository.save(user);
            success = "Reset password succeeded";
        } catch (Exception e) {
            return templateResponse.error("failed to reset password");
        }
        return templateResponse.success(success);
    }

    @PostMapping("/check-token/{otp}")
    public Map cheKTOkenValid(@PathVariable(value = "otp") String otp) {
        if (otp == null) return templateResponse.error("OTP is required");

        User user = userRepository.findOneByOTP(otp);
        if (user == null) {
            return templateResponse.error("OTP not valid");
        }

        return templateResponse.success("OTP valid");
    }

}
