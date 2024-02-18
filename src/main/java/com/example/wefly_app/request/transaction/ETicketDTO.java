package com.example.wefly_app.request.transaction;

import com.example.wefly_app.entity.Passenger;
import com.example.wefly_app.entity.enums.SeatClass;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class ETicketDTO {
    private String bookCode;
    private LocalDate departureDate;
    private LocalDate arrivalDate;
    private String flightCode;
    private SeatClass seatClass;
    private String airlineName;
    private String departAirportName;
    private String departIata;
    private String departCity;
    private String departProvince;
    private String arrivalAirportName;
    private String arrivalIata;
    private String arrivalCity;
    private String arrivalProvince;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private List<Passenger> passengers;

    public ETicketDTO(LocalDate departureDate, LocalDate arrivalDate, String flightCode,
                      SeatClass seatClass, String airlineName, String departAirportName, String departIata,
                      String departCity, String departProvince, String arrivalAirportName, String arrivalIata,
                      String arrivalCity, String arrivalProvince, LocalTime departureTime, LocalTime arrivalTime) {
        this.departureDate = departureDate;
        this.arrivalDate = arrivalDate;
        this.flightCode = flightCode;
        this.seatClass = seatClass;
        this.airlineName = airlineName;
        this.departAirportName = departAirportName;
        this.departIata = departIata;
        this.departCity = departCity;
        this.departProvince = departProvince;
        this.arrivalAirportName = arrivalAirportName;
        this.arrivalIata = arrivalIata;
        this.arrivalCity = arrivalCity;
        this.arrivalProvince = arrivalProvince;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }
}
