package com.example.wefly_app.entity;

import com.example.wefly_app.entity.enums.SeatClass;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Table(name = "transaction")
@Entity
@Where(clause = "deleted_date is null")
public class Transaction extends AbstractDate implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_id_seq")
    @SequenceGenerator(name = "transaction_id_seq", sequenceName = "TRANSACTION_ID_SEQ", initialValue = 322, allocationSize = 1)
    private Long id;

    private SeatClass seatClass;

//  @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @JsonManagedReference
    @OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Passenger> passengers;

    @JsonManagedReference
    @OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TransactionDetail> transactionDetails = new ArrayList<>();

    @JsonManagedReference
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "orderer_id")
    private Orderer orderer;

    @JsonManagedReference
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @JsonBackReference
    @OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ETicket> etickets;

    private int adultPassenger;
    private int childPassenger;
    private int infantPassenger;
    @Column(name = "total_price", precision = 13)
    private BigDecimal totalPrice;
    private String eticketFile;
}
