package com.example.wefly_app.request.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class MidtransResponseModel {
    @JsonProperty("transaction_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDate transactionTime;
    @JsonProperty("settlement_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDate settlementTime;
    @JsonProperty("expiry_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDate expiryTime;
    @JsonProperty("transaction_status")
    private String transactionStatus;
    @JsonProperty("status_code")
    private Integer statusCode;
    @JsonProperty("payment_type")
    private String paymentType;
    @JsonProperty("order_id")
    private Long orderId;
    @JsonProperty("gross_amount")
    private String grossAmount;
    @JsonProperty("fraud_status")
    private String fraudStatus;
    @JsonProperty("signature_key")
    private String signatureKey;
    @JsonProperty("card_type")
    private String cardType;
    private String currency;
    private String bank;
    private String issuer;
    private String store;
    @JsonProperty("va_numbers")
    private List<Map<Object, Object>> vaNumbers;

}
