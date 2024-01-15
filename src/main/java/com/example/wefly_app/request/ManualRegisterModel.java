package com.example.wefly_app.request;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;


@Data
public class ManualRegisterModel {
    @NotEmpty(message = "email is required.")
    private String email;
    @NotEmpty(message = "password is required.")
    private String password;
    @NotEmpty(message = "full name is required.")
    private String fullName;
    @NotEmpty(message = "phone number is required")
    @Pattern(regexp = "^[0-9\\-\\s]+$", message = "Phone number must contain only numbers, spaces, or dashes")
    private String phoneNumber;
    @NotNull(message = "date of birth is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate dateOfBirth;
}

