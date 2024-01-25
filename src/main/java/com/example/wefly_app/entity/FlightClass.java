package com.example.wefly_app.entity;

import com.example.wefly_app.entity.enums.SeatClass;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Table(name = "flight_class")
@Entity
@Data
public class FlightClass implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private SeatClass seatClass;
    @Column(name = "base_price_adult", precision = 14, scale = 2)
    private BigDecimal basePriceAdult;
    @Column(name = "base_price_child", precision = 14, scale = 2)
    private BigDecimal basePriceChild;
    @Column(name = "base_price_infant", precision = 14, scale = 2)
    private BigDecimal basePriceInfant;
    private Integer availableSeat;
    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "flight_id")
    private Flight flight;
}
