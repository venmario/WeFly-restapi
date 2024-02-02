package com.example.wefly_app.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "payment")
@Entity
@Data
public class Payment implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonProperty("settlement_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime settlementTime;
    @JsonProperty("expiry_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiryTime = LocalDateTime.now().plusHours(1);
    @JsonProperty("transaction_status")
    private String transactionStatus = "CHOOSING_PAYMENT";
    @JsonProperty("payment_type")
    private String paymentType;
    @JsonProperty("gross_amount")
    @Column(precision = 13)
    private BigDecimal grossAmount;
    private String issuer;
    @JsonBackReference
    @OneToOne(mappedBy = "payment")
    private Transaction transaction;

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus.toUpperCase();
    }
}
