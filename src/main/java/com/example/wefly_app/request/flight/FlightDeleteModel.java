package com.example.wefly_app.request.flight;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class FlightDeleteModel {
    @NotEmpty(message = "confirmation is required")
    @Pattern(regexp = "Delete Flight Data", message = "To proceed with deletion, type 'Delete Flight Data'")
    private String confirmation;
}
