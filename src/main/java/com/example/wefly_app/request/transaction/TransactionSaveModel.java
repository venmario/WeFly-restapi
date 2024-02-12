package com.example.wefly_app.request.transaction;

import com.example.wefly_app.entity.Flight;
import com.example.wefly_app.entity.Passenger;
import lombok.Data;

import javax.persistence.Column;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.List;

@Data
public class TransactionSaveModel {
    @NotEmpty(message = "passengers is required")
    @Valid
    private List<PassengerRegisterModel> passengers;
    @NotEmpty(message = "transaction details is required")
    @Valid
    private List<TransactionDetailSaveModel> transactionDetails;
    @NotNull(message = "adult passenger must not null")
    @Min(value = 1, message = "To book a ticket at least 1 adult passenger is needed")
    private int adultPassenger;
    @NotNull(message = "child passenger must not null")
    @Min(value = 0, message = "child passenger have 0 base value")
    private int childPassenger;
    @NotNull(message = "infant passenger must not null")
    @Min(value = 0, message = "infant passenger have 0 base value")
    private int infantPassenger;
    @NotNull(message = "Orderer data must not null")
    @Valid
    private OrdererRegisterModel orderer;
}
