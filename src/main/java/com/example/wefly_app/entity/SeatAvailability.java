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
    private String seatColumn;
    private String seatRow;
    private boolean available = true;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "seat_config_id")
    private SeatConfig seatConfig;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "flight_schedule_id")
    private FlightSchedule flightSchedule;
    @JsonBackReference
    @OneToOne
    @JoinColumn(name = "boarding_pass_id")
    private BoardingPass boardingPass;
    private SeatClass seatClass;
}
