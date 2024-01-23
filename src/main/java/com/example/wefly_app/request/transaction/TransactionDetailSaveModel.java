package com.example.wefly_app.request.transaction;

import lombok.Data;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class TransactionDetailSaveModel {
    @NotNull(message = "flight id must not null")
    private Long flightId;
}
