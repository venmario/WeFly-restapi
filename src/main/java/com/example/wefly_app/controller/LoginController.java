package com.example.wefly_app.controller;

import com.example.wefly_app.repository.UserRepository;
import com.example.wefly_app.request.LoginGoogleModel;
import com.example.wefly_app.request.LoginModel;
import com.example.wefly_app.service.UserService;
import com.example.wefly_app.util.TemplateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    public ResponseEntity<Map> loginByGoogle(@Valid @PathVariable ("accessToken")LoginGoogleModel request) throws IOException {
        return new ResponseEntity<>(serviceReq.loginByGoogle(request), HttpStatus.OK);
    }


}

