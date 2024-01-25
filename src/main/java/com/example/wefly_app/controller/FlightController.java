package com.example.wefly_app.controller;

import com.example.wefly_app.request.flight.FlightDeleteModel;
import com.example.wefly_app.request.flight.FlightRegisterModel;
import com.example.wefly_app.request.flight.FlightUpdateModel;
import com.example.wefly_app.service.FlightService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/v1/flight/")
@Slf4j
public class FlightController {
    @Autowired
    public FlightService flightService;

    @PostMapping(value = {"/save", "/save/"})
    public ResponseEntity<Map> save(@Valid @RequestBody FlightRegisterModel request) {
        return new ResponseEntity<>(flightService.save(request), HttpStatus.OK);
    }

    @PutMapping(value = "/update/{id}")
    public ResponseEntity<Map> update(@Valid @RequestBody FlightUpdateModel request, @PathVariable(value = "id") Long id) {
        return new ResponseEntity<>(flightService.update(request, id), HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<Map> delete(@Valid @RequestBody FlightDeleteModel request, @PathVariable(value = "id") Long id) {
        return new ResponseEntity<>(flightService.delete(request, id), HttpStatus.OK);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Map> getById(@PathVariable("id") Long id) {
        return new ResponseEntity<>(flightService.getById(id), HttpStatus.OK);
    }

    @GetMapping(value = {"/list", "/list/"})
    public ResponseEntity<Map> getAll(@RequestParam(required = true, defaultValue = "0") int page,
                                      @RequestParam(required = true, defaultValue = "10") int size,
                                      @RequestParam(required = false, defaultValue = "basePriceAdult") String orderBy,
                                      @RequestParam(required = false, defaultValue = "ascending") String orderType,
                                      @RequestParam(required = true) Long departureAirportId,
                                      @RequestParam(required = true) Long arrivalAirportId,
                                      @RequestParam(required = false) Long airLineId,
                                      @RequestParam(required = true) String departDate,
                                      @RequestParam(required = false) String departureTime,
                                      @RequestParam(required = false) String arrivalTime,
                                      @RequestParam(required = true, defaultValue = "1") Integer numberOfPassengers,
                                      @RequestParam(required = true, defaultValue = "ECONOMY") String seatClass) {
        return new ResponseEntity<>(flightService.getAll(page, size, orderBy, orderType, departureAirportId,
                arrivalAirportId, airLineId, departDate, departureTime, arrivalTime, numberOfPassengers
        ,seatClass), HttpStatus.OK);
    }
}
