package com.example.wefly_app.request.transaction;

import com.example.wefly_app.entity.Orderer;
import com.example.wefly_app.entity.Payment;
import com.example.wefly_app.entity.TransactionDetail;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class InvoiceDTO {
    private List<TransactionDetail> transactionDetails;
    private Payment payment;
    private Orderer orderer;
    private Map<String, Integer> transaction = new LinkedHashMap<>();
}
