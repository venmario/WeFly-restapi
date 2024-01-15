package com.example.wefly_app.request.user;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
@Data
public class ForgotPasswordModel {
    @NotEmpty(message = "email is required")
    private String email;
}
