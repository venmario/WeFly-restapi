package com.example.wefly_app.service.impl;

import com.example.wefly_app.entity.Airport;
import com.example.wefly_app.repository.AirportRepository;
import com.example.wefly_app.request.AirportRegisterModel;
import com.example.wefly_app.service.AirportService;
import com.example.wefly_app.util.TemplateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

//@Service
//@Slf4j
//public class AirportServiceImpl implements AirportService {
//    @Autowired
//    private AirportRepository airportRepository;
//    @Autowired
//    private TemplateResponse templateResponse;
//    @Override
//    public Map<Map, HttpStatus> save(AirportRegisterModel request) {
//        try {
//            log.info("Save New Airport");
//            Map<Map<Object, Object>, HttpStatus> response = new HashMap<>();
//            if (airportRepository.getSimilarName(request.getName()) > 0){
//                return response(templateResponse.error("Airport name already registered"), HttpStatus.BAD_REQUEST);
//            }
//            return null;
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    @Override
//    public Map<Object, Object> update(AirportRegisterModel request) {
//        return null;
//    }
//
//    @Override
//    public Map<Object, Object> delete(AirportRegisterModel request) {
//        return null;
//    }
//
//    @Override
//    public Map<Object, Object> getById(Long id) {
//        return null;
//    }
//}
