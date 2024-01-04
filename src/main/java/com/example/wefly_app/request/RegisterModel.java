package com.example.wefly_app.request;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class RegisterModel {
    @NotEmpty(message = "username is required.")
    private String username;
    @NotEmpty(message = "password is required.")
    private String password;
    @NotEmpty(message = "first name is required.")
    private String firstName;
    @NotEmpty(message = "last name is required.")
    private String lastName;
}

