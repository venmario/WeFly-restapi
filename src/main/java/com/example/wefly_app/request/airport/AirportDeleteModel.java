package com.example.wefly_app.request.airport;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class AirportDeleteModel {
    @NotEmpty(message = "confirmation is required")
    @Pattern(regexp = "Delete Airport Data", message = "To proceed with deletion, type 'Delete Airport Data'")
    private String confirmation;
}
