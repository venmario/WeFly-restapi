package com.example.wefly_app.controller;

import com.example.wefly_app.request.airplane.AirplaneDeleteModel;
import com.example.wefly_app.request.airplane.AirplaneRegisterModel;
import com.example.wefly_app.request.airplane.AirplaneUpdateModel;
import com.example.wefly_app.service.AirplaneService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/v1/airplane/")
@Slf4j
public class AirplaneController {
    @Autowired
    public AirplaneService airplaneService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = {"/save", "/save/"})
    public ResponseEntity<Map> save(@Valid @RequestBody AirplaneRegisterModel request) {
        return new ResponseEntity<>(airplaneService.save(request), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping
    public ResponseEntity<Map> update(@Valid @RequestBody AirplaneUpdateModel request, @PathVariable(value = "id") Long id) {
        return new ResponseEntity<>(airplaneService.update(request, id), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<Map> delete(@Valid @RequestBody AirplaneDeleteModel request, @PathVariable(value = "id") Long id) {
        return new ResponseEntity<>(airplaneService.delete(request, id), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = "/{id}")
    public ResponseEntity<Map> getById(@PathVariable("id") Long id) {
        return new ResponseEntity<>(airplaneService.getById(id), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = {"/list", "/list/"})
    public ResponseEntity<Map> getAll(@RequestParam(value = "page", defaultValue = "0") int page,
                                       @RequestParam(value = "size", defaultValue = "10") int size,
                                       @RequestParam(value = "orderBy", defaultValue = "id") String orderBy,
                                       @RequestParam(value = "orderType", defaultValue = "DESC") String orderType,
                                       @RequestParam(value = "name", defaultValue = "") String name,
                                       @RequestParam(value = "type", defaultValue = "") String type) {
        return new ResponseEntity<>(airplaneService.getAll(page, size, orderBy, orderType, name, type), HttpStatus.OK);
    }
}
