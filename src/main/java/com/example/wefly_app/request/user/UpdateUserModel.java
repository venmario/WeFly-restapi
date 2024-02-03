package com.example.wefly_app.request.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
public class UpdateUserModel {
    private String fullName;
    private String city;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate dateOfBirth;
    @Pattern(regexp = "^\\d+$", message = "Phone number must contain only numbers")
    private String phoneNumber;
    private String gender;
}
