package com.example.wefly_app.service;

import com.example.wefly_app.request.AirportRegisterModel;
import com.example.wefly_app.request.AirportUpdateModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface AirportService {
    Map<Map, HttpStatus> save(AirportRegisterModel request);
    Map<Object, HttpStatus> update(AirportUpdateModel request);
    Map<Object, HttpStatus> delete(AirportRegisterModel request);
    Map<Object, HttpStatus> getById(Long id);
}
