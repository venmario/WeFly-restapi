package com.example.wefly_app.request.airport;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class AirportRegisterModel {
    @NotEmpty(message = "name is required")
    private String name;
    @NotEmpty(message = "airport code is required")
    @Pattern(regexp = "[A-Z]{3}", message = "airport code must be 3 uppercase characters")
    private String airportCode;
    @NotEmpty(message = "city is required")
    private String city;
    @NotEmpty(message = "country is required")
    private String country;
//    @NotEmpty(message = "total terminal is required")
//    @Min(value = 1, message = "terminal must be greater than or equal to 1")
//    private Integer totalTerminal;
    @NotNull(message = "status is required")
    private boolean status;

    public boolean getStatus() {return this.status;}
}
