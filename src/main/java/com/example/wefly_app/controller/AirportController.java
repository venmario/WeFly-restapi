package com.example.wefly_app.controller;

import com.example.wefly_app.request.airport.AirportDeleteModel;
import com.example.wefly_app.request.airport.AirportRegisterModel;
import com.example.wefly_app.request.airport.AirportUpdateModel;
import com.example.wefly_app.service.AirportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/v1/airport/")
@Slf4j
public class AirportController {
    @Autowired
    public AirportService airportService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = {"/save", "/save/"})
    public ResponseEntity<Map> save(@Valid @RequestBody AirportRegisterModel request) {
        return new ResponseEntity<>(airportService.save(request), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(value = "/update/{id}")
    public ResponseEntity<Map> update(@Valid @RequestBody AirportUpdateModel request, @PathVariable(value = "id") Long id) {
        return new ResponseEntity<>(airportService.update(request, id), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<Map> delete(@Valid @RequestBody AirportDeleteModel request, @PathVariable(value = "id") Long id) {
        return new ResponseEntity<>(airportService.delete(request ,id), HttpStatus.OK);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Map> getById(@PathVariable("id") Long id) {
        return new ResponseEntity<>(airportService.getById(id), HttpStatus.OK);
    }

    @GetMapping(value = {"/list", "/list/"})
    public ResponseEntity<Map> getAll(@RequestParam(required = true, defaultValue = "0") int page,
                                       @RequestParam(required = true, defaultValue = "10") int size,
                                       @RequestParam(required = false, defaultValue = "name") String orderBy,
                                       @RequestParam(required = false, defaultValue = "ascending") String orderType,
                                       @RequestParam(required = false) String name,
                                       @RequestParam(required = false) String city,
                                       @RequestParam(required = false) String country,
                                       @RequestParam(required = false) String iata,
                                       @RequestParam(required = false) String icao,
                                       @RequestParam(required = false) String province) {
        return new ResponseEntity<>(airportService.getAll(page, size, orderBy, orderType, name, city, country, iata,
                icao, province), HttpStatus.OK);
    }

    @GetMapping(value = {"/listDropDown", "/listDropDown/"})
    public ResponseEntity<Map> getAll(@RequestParam(required = false, defaultValue = "name") String orderBy,
                                      @RequestParam(required = false, defaultValue = "ASC") String orderType,
                                      @RequestParam(required = false) String name,
                                      @RequestParam(required = false) String city,
                                      @RequestParam(required = false) String country,
                                      @RequestParam(required = false) String iata,
                                      @RequestParam(required = false) String icao,
                                      @RequestParam(required = false) String province) {
        return new ResponseEntity<>(airportService.getAllDropDown(orderBy, orderType, name, city, country, iata,
                icao, province), HttpStatus.OK);
    }


}
