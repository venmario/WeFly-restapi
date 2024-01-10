package com.example.wefly_app.request;
import lombok.Data;

import javax.validation.constraints.NotEmpty;


@Data
public class RegisterModel {
    @NotEmpty(message = "email is required.")
    private String email;
    @NotEmpty(message = "password is required.")
    private String password;
    @NotEmpty(message = "full name is required.")
    private String fullName;
    @NotEmpty(message = "phone number is required")
    private String phoneNumber;
}

