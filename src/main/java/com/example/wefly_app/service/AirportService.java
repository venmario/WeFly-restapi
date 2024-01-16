package com.example.wefly_app.service;

import com.example.wefly_app.request.AirportDeleteModel;
import com.example.wefly_app.request.AirportRegisterModel;
import com.example.wefly_app.request.AirportUpdateModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface AirportService {
    Map<Object, Object> save(AirportRegisterModel request);
    Map<Object, Object> update(AirportUpdateModel request, Long id);
    Map<Object, Object> delete(AirportDeleteModel request, Long id);
    Map<Object, Object> getById(Long id);
    Map<Object, Object> getAll(int page, int size, String orderBy, String orderType
    , String name, String city, String country, String airportCode);
}
