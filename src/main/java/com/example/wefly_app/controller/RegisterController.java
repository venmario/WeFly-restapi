package com.example.wefly_app.controller;

import com.example.wefly_app.request.user.ManualRegisterModel;
import com.example.wefly_app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/v1/user-register/")
public class RegisterController {
    @Autowired
    public UserService serviceReq;

    @PostMapping("/register-user")
    public ResponseEntity<Map> saveRegisterManual(@Valid @RequestBody ManualRegisterModel objModel) {
        return new ResponseEntity<>(serviceReq.registerManual(objModel), HttpStatus.OK);
    }

    @GetMapping("/register-confirm-otp/{token}")
    public ResponseEntity<Map> saveRegisterManual(@PathVariable(value = "token") String request) {
        return new ResponseEntity<>(serviceReq.accountActivation(request), HttpStatus.OK);
    }

}
