package com.example.wefly_app.entity;

import lombok.Data;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "flight")
@Where(clause = "deleted_date is null")
public class Flight extends AbstractDate implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
//    private String flightNumber;

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
    @Column(name = "base_price_adult", precision = 14, scale = 2)
    private BigDecimal basePriceAdult;
    @Column(name = "base_price_child", precision = 14, scale = 2)
    private BigDecimal basePriceChild;
    @Column(name = "base_price_infant", precision = 14, scale = 2)
    private BigDecimal basePriceInfant;
}
