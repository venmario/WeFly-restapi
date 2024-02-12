package com.example.wefly_app.controller;

import com.example.wefly_app.request.checkin.CheckinRequestModel;
import com.example.wefly_app.service.CheckinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("v1/checkin/")
@Slf4j
public class CheckInController {
    @Autowired
    public CheckinService checkinService;


    @PostMapping
    public ResponseEntity<Map> save(@Valid @RequestBody CheckinRequestModel request) {
        return new ResponseEntity<>(checkinService.checkIn(request), HttpStatus.OK);
    }
}
