package com.example.wefly_app.controller;

import com.example.wefly_app.request.user.ChangePasswordModel;
import com.example.wefly_app.request.user.ForgotPasswordModel;
import com.example.wefly_app.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @PutMapping("/change-password")
    public ResponseEntity<Map> changePassword(@Valid @RequestBody ChangePasswordModel request) {
        return new ResponseEntity<>(serviceReq.changePassword(request), HttpStatus.OK);
    }

    @PostMapping("/check-token/{request}")
    public ResponseEntity<Map> cheKTOkenValid(@PathVariable(value = "request") String request) {
        return new ResponseEntity<>(serviceReq.checkOtpValidity(request), HttpStatus.OK);
    }

}
