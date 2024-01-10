package com.example.wefly_app.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
@Data
public class LoginModel {
    @NotEmpty(message = "email is required.")
    private String email;
    @NotEmpty(message = "password is required.")
    private String password;
}
