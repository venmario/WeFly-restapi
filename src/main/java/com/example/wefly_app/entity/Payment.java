package com.example.wefly_app.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Table(name = "payment")
@Entity
@Where(clause = "deleted_date is null")
@Data
public class Payment extends AbstractDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String paymentProof;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    @JsonManagedReference
    @OneToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;
    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "bank_id")
    private Bank bank;

}
