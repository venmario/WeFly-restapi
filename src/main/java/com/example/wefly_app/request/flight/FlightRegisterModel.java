package com.example.wefly_app.request.flight;

import com.example.wefly_app.entity.Airplane;
import com.example.wefly_app.entity.Airport;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class FlightRegisterModel {
    @NotEmpty(message = "departure airport is required")
    private Long departureAirportId;
    @NotEmpty(message = "arrival airport is required")
    private Long arrivalAirportId;
    @NotEmpty(message = "airplane is required")
    private Long airplane;
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
    @NotNull(message = "base price is required")
    @Column(precision = 14, scale = 2)
    private BigDecimal basePrice;

    public boolean getStatus(){
        return this.status;
    }
}
