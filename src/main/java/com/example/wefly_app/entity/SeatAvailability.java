package com.example.wefly_app.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import javax.persistence.*;

@Table(name = "seat_availability")
@Entity
@Data
public class SeatAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String seatNumber;
    private boolean available = true;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "airplane_seat_id")
    private AirplaneSeat airplaneSeat;
}
