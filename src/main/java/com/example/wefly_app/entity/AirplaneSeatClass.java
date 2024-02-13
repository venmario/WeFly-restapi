package com.example.wefly_app.entity;

import com.example.wefly_app.entity.enums.SeatClass;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@Entity
@Table(name = "airplane_seat_class")
public class AirplaneSeatClass implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer numberOfRow;
    private Integer numberOfColumn;
    private SeatClass seatClass;
    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "airplane_id")
    private Airplane airplane;
    @JsonBackReference
    @OneToMany(mappedBy = "airplaneSeatClass", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<SeatConfiguration> seatConfigurations;
}
