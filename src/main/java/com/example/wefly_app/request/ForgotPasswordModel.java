package com.example.wefly_app.request;

import javax.validation.constraints.NotEmpty;

public class ForgotPasswordModel {
    @NotEmpty(message = "email is required")
    private String username;
    @NotEmpty(message = "new password required")
    private String password;
    @NotEmpty(message = "confirm password required")
    private String confirmPassword;
    @NotEmpty(message = "OTP is required")
    private String otp;

}
