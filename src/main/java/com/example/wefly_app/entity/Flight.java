package com.example.wefly_app.entity;

import lombok.Data;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Data
@Entity
@Table(name = "flight")
@Where(clause = "deleted_date is null")
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String flightNumber;

    @ManyToOne
    @JoinColumn(name = "departure_airport_id")
    private Airport departureAirport;

    @ManyToOne
    @JoinColumn(name = "arrival_airport_id")
    private Airport arrivalAirport;

    @ManyToOne
    @JoinColumn(name = "airplane_id")
    private Airplane airplane;

    private String departureDate;
    private String arrivalDate;
    private String departureTime;
    private String arrivalTime;

    private boolean status;

    public boolean getStatus(){
        return this.status;
    }
    private BigDecimal basePrice;
}
