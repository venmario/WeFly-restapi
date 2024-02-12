package com.example.wefly_app.request.airport;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class AirportUpdateModel {
    private String name;
//    @Size(min = 3, max = 3, message = "airport code must be 3 characters")
    @Pattern(regexp = "[A-Z]{3}", message = "iata code must be 3 uppercase characters")
    private String iata;
    @Pattern(regexp = "[A-Z]{4}", message = "icao code must be 4 uppercase characters")
    private String icao;
    private String city;
    private String province;
    private String country;
    private boolean status;

    public boolean getStatus(){
        return this.status;
    }
}
