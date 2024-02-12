package com.example.wefly_app.request.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class MidtransRequestModel {
    @JsonProperty("transaction_details")
    private Map<String, Object> transactionDetails;
    @JsonProperty("customer_details")
    private Map<String, Object> customerDetails;
}
