package com.example.wefly_app.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Entity
@Table(name = "flight")
@Where(clause = "deleted_date is null")
public class Flight extends AbstractDate implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String flightCode;

    @ManyToOne
    @JoinColumn(name = "departure_airport_id")
    private Airport departureAirport;

    @ManyToOne
    @JoinColumn(name = "arrival_airport_id")
    private Airport arrivalAirport;

    @ManyToOne
    @JoinColumn(name = "airline_id")
    private Airline airline;

    @ManyToOne
    @JoinColumn(name = "airplane_id")
    private Airplane airplane;

    @JsonBackReference
    @OneToMany(mappedBy = "flight", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<FlightSchedule> flightSchedules;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime departureTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime arrivalTime;
    @Column(name = "base_price",precision = 14)
    private BigDecimal basePrice;

    private boolean scheduleMonday;
    private boolean scheduleTuesday;
    private boolean scheduleWednesday;
    private boolean scheduleThursday;
    private boolean scheduleFriday;
    private boolean scheduleSaturday;
    private boolean scheduleSunday;

    public boolean operatesOn(DayOfWeek day) {
        switch (day) {
            case MONDAY:
                return scheduleMonday;
            case TUESDAY:
                return scheduleTuesday;
            case WEDNESDAY:
                return scheduleWednesday;
            case THURSDAY:
                return scheduleThursday;
            case FRIDAY:
                return scheduleFriday;
            case SATURDAY:
                return scheduleSaturday;
            case SUNDAY:
                return scheduleSunday;
            default:
                return false;
        }
    }
}
