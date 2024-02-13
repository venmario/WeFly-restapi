package com.example.wefly_app.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "boarding_pass")
public class BoardingPass implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "e_ticket_id")
    private ETicket eTicket;
    @OneToOne
    @JoinColumn(name = "passenger_id")
    private Passenger passenger;
    @JsonManagedReference
    @OneToOne
    @JoinColumn(name = "seat_availability_id")
    private SeatAvailability seatAvailability;

}
