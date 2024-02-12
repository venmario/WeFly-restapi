package com.example.wefly_app.request.checkin;

import com.example.wefly_app.entity.enums.SeatClass;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BoardingPassDTO {
    private LocalDate departDate;
    private String flightCode;
    private SeatClass seatClass;
    private String departIata;
    private String arrivalIata;
    private LocalTime departureTime;
    private LocalTime arrivalTime;

    public BoardingPassDTO(LocalDate departDate, String flightCode, SeatClass seatClass,
                           String departIata, String arrivalIata, LocalTime departureTime,
                           LocalTime arrivalTime) {
        this.departDate = departDate;
        this.flightCode = flightCode;
        this.seatClass = seatClass;
        this.departIata = departIata;
        this.arrivalIata = arrivalIata;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }
}
