package com.example.wefly_app.request;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class RegisterGoogleModel {
    private String email;
    private String password;
    private String fullName;
}