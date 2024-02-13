package com.example.wefly_app.entity;

import com.example.wefly_app.entity.enums.SeatClass;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@Table(name = "seat_configuration")
@Entity
public class SeatConfiguration extends AbstractDate implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String seatColumn;
    private String seatRow;
    private SeatClass seatClass;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "airplane_seat_class_id")
    private AirplaneSeatClass airplaneSeatClass;
//    @JsonBackReference
//    @OneToMany(mappedBy = "seatConfig", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private List<SeatAvailability> seatAvailabilities;

}
