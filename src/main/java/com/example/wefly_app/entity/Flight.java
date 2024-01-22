package com.example.wefly_app.entity;

import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Entity
@Table
@Where(clause = "deleted_date is null")
public class Flight extends AbstractDate implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
//    private Airport departure;
//    private Airport arrival;
    private BigDecimal basePrice;
    @ManyToOne
    @JoinColumn(name = "airline_id", foreignKey = @ForeignKey(name = "airline_id_constraint"))
    private Airline airline;
}
