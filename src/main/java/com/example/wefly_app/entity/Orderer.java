package com.example.wefly_app.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Table
@Entity
@Where(clause = "deleted_date is null")
public class Orderer extends AbstractDate implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    @JsonBackReference
    @OneToOne(mappedBy = "orderer")
    private Transaction transaction;
}
