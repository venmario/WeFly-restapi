package com.example.wefly_app.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;


@Data
public class ResetPasswordModel {
    @NotEmpty(message = "email is required")
    private String email;
    @NotEmpty(message = "new password required")
    private String newPassword;
    @NotEmpty(message = "confirm password required")
    private String confirmPassword;
    @NotEmpty(message = "OTP is required")
    private String otp;
}

