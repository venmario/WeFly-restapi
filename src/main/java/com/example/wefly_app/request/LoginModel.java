package com.example.wefly_app.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
@Data
public class LoginModel {
    @NotEmpty(message = "username is required.")
    private String username;
    @NotEmpty(message = "password is required.")
    private String password;
}
