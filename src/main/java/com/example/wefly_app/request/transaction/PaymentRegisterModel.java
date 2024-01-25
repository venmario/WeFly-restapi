package com.example.wefly_app.request.transaction;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PaymentRegisterModel {
    @NotNull(message = "transaction id is required")
    private Long transactionId;
    @NotNull(message = "bank id is required")
    private Long bankId;
}
