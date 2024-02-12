package com.example.wefly_app.entity;

import com.example.wefly_app.entity.enums.SeatClass;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@Table(name = "seat_config")
@Entity
public class SeatConfig extends AbstractDate implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "airplane_id")
    private Airplane airplane;
    private SeatClass seatClass;
    private int seatRow;
    private int seatColumn;
    @JsonBackReference
    @OneToMany(mappedBy = "seatConfig", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<SeatAvailability> seatAvailabilities;

}
