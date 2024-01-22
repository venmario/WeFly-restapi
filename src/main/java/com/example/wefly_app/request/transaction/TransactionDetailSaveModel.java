package com.example.wefly_app.request.transaction;

import lombok.Data;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class TransactionDetailSaveModel {
    @NotNull(message = "flight id must not null")
    private Long flightId;

    @NotNull
    @Column(name = "total_price_adult", precision = 14, scale = 2)
    private BigDecimal totalPriceAdult;
    @NotNull
    @Column(name = "total_price_child", precision = 14, scale = 2)
    private BigDecimal totalPriceChild;
    @NotNull
    @Column(name = "total_price_infant", precision = 14, scale = 2)
    private BigDecimal totalPriceInfant;
}
