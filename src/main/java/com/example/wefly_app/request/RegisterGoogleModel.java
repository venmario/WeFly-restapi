package com.example.wefly_app.request;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class RegisterGoogleModel {
    @NotEmpty(message = "username is required.")
    private String username;
    @NotEmpty(message = "password is required.")
    private String password;
    @NotEmpty(message = "full name is required.")
    private String fullName;
}