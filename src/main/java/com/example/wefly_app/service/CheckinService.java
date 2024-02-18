package com.example.wefly_app.service;

import com.example.wefly_app.entity.Transaction;
import com.example.wefly_app.request.checkin.CheckinRequestModel;
import org.springframework.core.io.Resource;

import java.io.FileNotFoundException;
import java.util.Map;

public interface CheckinService {
    Resource getBoardingPass (Long eticketId);
    Map<Object, Object> checkIn(CheckinRequestModel request);
}
