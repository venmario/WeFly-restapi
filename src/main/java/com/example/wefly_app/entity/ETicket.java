package com.example.wefly_app.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "e_ticket")
public class ETicket implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String bookCode;
    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;
    @OneToOne
    @JoinColumn(name = "transaction_detail_id")
    private TransactionDetail transactionDetail;
    private String boardingPassFile;
}
