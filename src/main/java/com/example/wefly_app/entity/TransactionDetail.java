package com.example.wefly_app.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Table(name = "transaction_detail")
@Entity
@Data
public class TransactionDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;
    @ManyToOne
    @JoinColumn(name = "flight_class_id")
    private FlightClass flightClass;
    @Column(name = "total_price_adult", precision = 14, scale = 2)
    private BigDecimal totalPriceAdult;
    @Column(name = "total_price_child", precision = 14, scale = 2)
    private BigDecimal totalPriceChild;
    @Column(name = "total_price_infant", precision = 14, scale = 2)
    private BigDecimal totalPriceInfant;
}
