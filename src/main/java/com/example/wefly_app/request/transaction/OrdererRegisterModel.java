package com.example.wefly_app.request.transaction;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class OrdererRegisterModel {
    @NotEmpty(message = "first name is required")
    private String firstName;
    @NotEmpty(message = "last name is required")
    private String lastName;
    @NotEmpty(message = "phone number is required")
    private String phoneNumber;
    @NotEmpty(message = "email is required")
    private String email;
}
