package com.example.wefly_app.entity;

import lombok.Data;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@Table(name = "bank")
@Where(clause = "deleted_date is null")
@Data
public class Bank extends AbstractDate{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String bankName;
    private String accountNumber;
    private String accountName;
}
