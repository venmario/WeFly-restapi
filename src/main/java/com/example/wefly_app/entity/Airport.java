package com.example.wefly_app.entity;

import lombok.Data;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.validation.constraints.Max;
import java.util.List;

@Data
@Entity
@Table(name = "airport")
@Where(clause = "deleted_date is null")
public class Airport extends AbstractDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String airportCode;
    private String city;
    private String country;
    private boolean status;

    public boolean getStatus(){
        return this.status;
    }

}
