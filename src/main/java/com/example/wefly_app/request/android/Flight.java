package com.example.wefly_app.request.android;

import com.example.wefly_app.entity.AbstractDate;
import com.example.wefly_app.entity.Airport;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class Flight extends AbstractDate {
    private Long id;
    private String flightNumber;
    private Airport departureAirport;
    private Airport arrivalAirport;
    private Airplane airplane;
    private LocalDate departureDate;
    private LocalDate arrivalDate;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private BigDecimal basePrice;
}
