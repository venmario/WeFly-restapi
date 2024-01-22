package com.example.wefly_app.controller;

import com.example.wefly_app.request.user.LoginGoogleModel;
import com.example.wefly_app.request.user.LoginModel;
import com.example.wefly_app.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/v1/user-login/")
@Slf4j
public class LoginController {
    @Autowired
    public UserService serviceReq;

    @PostMapping("/login")
    public ResponseEntity<Map> login(@Valid @RequestBody LoginModel objModel) throws Exception {
        return new ResponseEntity<>(serviceReq.login(objModel), HttpStatus.OK);
    }

    @PostMapping({"/signin_google/{accessToken}", "/signin_google/{accessToken}/"})
    public ResponseEntity<Map> loginByGoogle(@PathVariable ("accessToken")String request) throws IOException {
        return new ResponseEntity<>(serviceReq.loginByGoogle(request), HttpStatus.OK);
    }


}

