package com.example.wefly_app.request;

import lombok.Data;

@Data
public class AirportUpdateModel {
    private String name;
    private String airportCode;
    private boolean status;
}
