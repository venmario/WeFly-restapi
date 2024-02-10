package com.example.wefly_app.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

//@Data
//@Entity
//@Table(name = "flight_schedule")
//public class FlightSchedule {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
//    private LocalDate departureDate;
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
//    private LocalDate arrivalDate;
//    @ManyToOne
//    @JoinColumn(name = "flight_id")
//    private Flight flight;
//    @OneToOne
//    @JoinColumn(name = "airplane_id")
//    private Airplane airplane;
//    @JsonBackReference
//    @OneToMany(mappedBy = "flight", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private List<FlightClass> flightClasses;
//    @JsonBackReference
//    @OneToMany(mappedBy = "flightSchedule", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private List<SeatAvailability> seatAvailabilities;
//
//
//}
