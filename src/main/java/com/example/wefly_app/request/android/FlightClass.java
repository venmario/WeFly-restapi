package com.example.wefly_app.request.android;

import com.example.wefly_app.entity.AbstractDate;
import com.example.wefly_app.entity.enums.SeatClass;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FlightClass {
    private Long id;
    private SeatClass seatClass;
    private BigDecimal basePriceAdult;
    private BigDecimal basePriceChild;
    private BigDecimal basePriceInfant;
    private int availableSeat;
    private Flight flight;
}
