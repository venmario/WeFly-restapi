package com.example.wefly_app.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class OtpRequestModel {
    @NotEmpty(message = "OTP is required")
    private String otp;
}
