package com.example.wefly_app.service;

import com.example.wefly_app.request.airplane.AirplaneDeleteModel;
import com.example.wefly_app.request.airplane.AirplaneRegisterModel;
import com.example.wefly_app.request.airplane.AirplaneUpdateModel;

import javax.validation.Valid;
import java.util.Map;

public interface AirplaneService {
    Map<Object, Object> save(AirplaneRegisterModel request);
    Map<Object, Object> update(@Valid AirplaneUpdateModel request, Long id);
    Map<Object, Object> delete(AirplaneDeleteModel request, Long id);
    Map<Object, Object> getById(Long id);
    Map<Object, Object> getAll(int page, int size, String orderBy, String orderType
            , String name, String type);
}
