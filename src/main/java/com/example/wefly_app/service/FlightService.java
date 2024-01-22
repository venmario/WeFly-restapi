package com.example.wefly_app.service;

import com.example.wefly_app.request.flight.FlightDeleteModel;
import com.example.wefly_app.request.flight.FlightRegisterModel;
import com.example.wefly_app.request.flight.FlightUpdateModel;

import java.util.Map;

public interface FlightService {
    Map<Object, Object> save(FlightRegisterModel request);
    Map<Object, Object> update(FlightUpdateModel request, Long id);
    Map<Object, Object> delete(FlightDeleteModel request, Long id);
    Map<Object, Object> getAll(int page, int size, String orderBy, String orderType
            , String name, String city, String country, String airportCode);
    Map<Object, Object> getById(Long id);
}
