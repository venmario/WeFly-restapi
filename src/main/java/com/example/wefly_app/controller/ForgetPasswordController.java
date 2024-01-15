package com.example.wefly_app.controller;

import com.example.wefly_app.entity.User;
import com.example.wefly_app.repository.UserRepository;
import com.example.wefly_app.request.ForgotPasswordModel;
import com.example.wefly_app.request.ChangePasswordModel;
import com.example.wefly_app.request.OtpRequestModel;
import com.example.wefly_app.service.UserService;
import com.example.wefly_app.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/v1/forget-password/")
@Slf4j
public class ForgetPasswordController {

    @Autowired
    public UserService serviceReq;

    // Step 1 : Send OTP
    @PostMapping("/forgot-password")
    public ResponseEntity<Map> sendEmailPassword(@Valid @RequestBody ForgotPasswordModel user) {
        return new ResponseEntity<>(serviceReq.forgotPasswordRequest(user), HttpStatus.OK);
    }

    // Step 2 : change password
    @Transactional
    @PostMapping("/change-password")
    public ResponseEntity<Map> changePassword(@Valid @RequestBody ChangePasswordModel request) {
        return new ResponseEntity<>(serviceReq.changePassword(request), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/check-token/{otp}")
    public ResponseEntity<Map> cheKTOkenValid(@PathVariable(value = "otp") OtpRequestModel request) {
        return new ResponseEntity<>(serviceReq.checkOtpValidity(request), HttpStatus.OK);
    }

}
