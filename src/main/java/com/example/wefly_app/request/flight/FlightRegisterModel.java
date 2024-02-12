package com.example.wefly_app.request.flight;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Column;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class FlightRegisterModel {
    @NotNull(message = "departure airport is required")
    private Long departureAirportId;
    @NotNull(message = "arrival airport is required")
    private Long arrivalAirportId;
    @NotNull(message = "airplane is required")
    private Long airplaneId;
    @NotNull(message = "airline is required")
    private Long airlineId;
    @NotNull(message = "departure time is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime departureTime;
    @NotNull(message = "arrival time is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime arrivalTime;
    @NotNull(message = "base price is required")
    @Column(precision = 14, scale = 2)
    private BigDecimal basePrice;

    private boolean scheduleMonday = false;
    private boolean scheduleTuesday = false;
    private boolean scheduleWednesday = false;
    private boolean scheduleThursday = false;
    private boolean scheduleFriday = false;
    private boolean scheduleSaturday = false;
    private boolean scheduleSunday = false;

}
