package com.example.wefly_app.service;

import com.example.wefly_app.entity.Transaction;
import com.example.wefly_app.request.checkin.CheckinRequestModel;
import org.springframework.core.io.Resource;

import java.util.Map;

public interface CheckinService {
    void save(Transaction request);
    void generateETicket(Transaction request);
    Resource getETicket(Long transactionId);
    Resource getBoardingPass (Long eticketId);
    Map<Object, Object> checkIn(CheckinRequestModel request);
}
