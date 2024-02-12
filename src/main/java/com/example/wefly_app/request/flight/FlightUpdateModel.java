package com.example.wefly_app.request.flight;

import com.example.wefly_app.entity.Airplane;
import com.example.wefly_app.entity.Airport;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Column;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class FlightUpdateModel {
    private Long departureAirportId;
    private Long arrivalAirportId;
    private Long airplaneId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime departureTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime arrivalTime;
    @Column(precision = 14, scale = 2)
    private BigDecimal basePrice;
}
