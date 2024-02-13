package com.example.wefly_app.entity;

import com.example.wefly_app.entity.enums.SeatClass;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "seat_availability")
@Entity
@Data
public class SeatAvailability implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "seat_configuration_id")
    private SeatConfiguration seatConfiguration;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "flight_class_id")
    private FlightClass flightClass;
    @JsonBackReference
    @OneToOne(mappedBy = "seatAvailability", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private BoardingPass boardingPass;
}
