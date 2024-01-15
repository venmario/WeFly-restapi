package com.example.wefly_app.request;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class AirportRegisterModel {
    @NotEmpty(message = "name is required")
    private String name;
    @NotEmpty(message = "airport code is required")
    @Size(min = 3, max = 3, message = "airport code must be 3 characters")
    private String airportCode;
    @NotEmpty(message = "city is required")
    private String city;
    @NotEmpty(message = "country is required")
    private String country;
//    @NotEmpty(message = "total terminal is required")
//    @Min(value = 1, message = "terminal must be greater than or equal to 1")
//    private Integer totalTerminal;
    @NotEmpty(message = "status is required")
    private boolean status;
}
