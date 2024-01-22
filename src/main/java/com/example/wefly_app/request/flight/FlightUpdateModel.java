package com.example.wefly_app.request.flight;

import com.example.wefly_app.entity.Airplane;
import com.example.wefly_app.entity.Airport;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class FlightUpdateModel {
    @NotEmpty(message = "flight number is required")
    private String flightNumber;
    @NotEmpty(message = "departure airport is required")
    private Airport departureAirport;
    @NotEmpty(message = "arrival airport is required")
    private Airport arrivalAirport;
    @NotEmpty(message = "airplane is required")
    private Airplane airplane;
    @NotEmpty(message = "departure date is required")
    private String departureDate;
    @NotEmpty(message = "arrival date is required")
    private String arrivalDate;
    @NotEmpty(message = "departure time is required")
    private String departureTime;
    @NotEmpty(message = "arrival time is required")
    private String arrivalTime;
    @NotNull(message = "status is required")
    private boolean status;
}
