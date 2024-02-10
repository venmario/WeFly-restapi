package com.example.wefly_app.request.transaction;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReportDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private String period;
    private long numberOfTransactions;
    private long successfulTransactions;
    private long failedTransactions;
    private BigDecimal income;
    private BigDecimal potentialIncome;

    public ReportDTO(long numberOfTransactions,
                     long successfulTransactions, long failedTransactions, BigDecimal income,
                     BigDecimal potentialIncome) {
        this.numberOfTransactions = numberOfTransactions;
        this.successfulTransactions = successfulTransactions;
        this.failedTransactions = failedTransactions;
        this.income = income;
        this.potentialIncome = potentialIncome;
    }
}
