package com.example.wefly_app.request.user;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class LoginGoogleModel {
    @NotEmpty(message = "token is required")
    private String token;
}
