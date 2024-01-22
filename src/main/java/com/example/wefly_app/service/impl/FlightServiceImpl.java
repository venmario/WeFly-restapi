package com.example.wefly_app.service.impl;

import com.example.wefly_app.entity.Flight;
import com.example.wefly_app.repository.FlightRepository;
import com.example.wefly_app.request.flight.FlightDeleteModel;
import com.example.wefly_app.request.flight.FlightRegisterModel;
import com.example.wefly_app.request.flight.FlightUpdateModel;
import com.example.wefly_app.service.FlightService;
import com.example.wefly_app.util.SimpleStringUtils;
import com.example.wefly_app.util.TemplateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityExistsException;
import java.util.Map;

@Service
@Slf4j
public class FlightServiceImpl implements FlightService {
    @Autowired
    private FlightRepository flightRepository;
    @Autowired
    private TemplateResponse templateResponse;
    @Autowired
    private SimpleStringUtils simpleStringUtils;
    
    
    @Override
    public Map<Object, Object> save(FlightRegisterModel request) {
        try {
            log.info("Save New Flight");
            if (flightRepository.checkExistingFlightNumber(request.getFlightNumber()) != null){
                throw new EntityExistsException("Flight with number " + request.getFlightNumber() + " already exists");
            }
            Flight flight = new Flight();
            flight.setFlightNumber(request.getFlightNumber());
            flight.setDepartureDate(request.getDepartureDate());
            flight.setArrivalDate(request.getArrivalDate());
            flight.setDepartureAirport(request.getDepartureAirport());
            flight.setArrivalAirport(request.getArrivalAirport());
            flight.setAirplane(request.getAirplane());

            log.info("Flight Saved");
            return templateResponse.success(flightRepository.save(flight));
        } catch (Exception e) {
            log.error("Error Saving Flight", e);
            throw e;
        }
    }

    @Override
    public Map<Object, Object> update(FlightUpdateModel request, Long id) {
        try {
            log.info("Update Flight");
            Flight checkDataDBFlight = flightRepository.checkExistingId(id);
            if (checkDataDBFlight == null) throw new EntityExistsException("Flight with id " + id + " not found");
            int count = 0;
            Flight flight = checkDataDBFlight;
            if (request.getFlightNumber() != null ) {
                flight.setFlightNumber(request.getFlightNumber());
                count++;
            }
            if (request.getDepartureDate() != null) {
                flight.setDepartureDate(request.getDepartureDate());
                count++;
            }
            if (request.getArrivalDate() != null) {
                flight.setArrivalDate(request.getArrivalDate());
                count++;
            }
            if (request.getDepartureAirport() != null) {
                flight.setDepartureAirport(request.getDepartureAirport());
                count++;
            }
            if (request.getArrivalAirport() != null) {
                flight.setArrivalAirport(request.getArrivalAirport());
                count++;
            }
            if (request.getAirplane() != null) {
                flight.setAirplane(request.getAirplane());
                count++;
            }
            if (count == 0) throw new IllegalArgumentException("No data to update");
            log.info("Flight Updated");
            return templateResponse.success(flightRepository.save(flight));
        } catch (Exception e) {
            log.error("Error Updating Flight", e);
            throw e;
        }
    }

    @Override
    public Map<Object, Object> delete(FlightDeleteModel request, Long id) {
        try {
            log.info("Delete Flight");
            Flight checkDataDBFlight = flightRepository.checkExistingId(id);
            if (checkDataDBFlight == null) throw new EntityExistsException("Flight with id " + id + " not found");
            flightRepository.delete(checkDataDBFlight);
            log.info("Flight Deleted");
            return templateResponse.success("Flight with id " + id + " deleted");
        } catch (Exception e) {
            log.error("Error Deleting Flight", e);
            throw e;
        }
    }

    @Override
    public Map<Object, Object> getAll(int page, int size, String orderBy, String orderType, String name, String city, String country, String airportCode) {
        try {
            log.info("Get All Flights");
            return templateResponse.success(flightRepository.findAll());
        } catch (Exception e) {
            log.error("Error Getting All Flights", e);
            throw e;
        }
    }

    @Override
    public Map<Object, Object> getById(Long id) {
        try {
            log.info("Get Flight By Id");
            Flight checkDataDBFlight = flightRepository.checkExistingId(id);
            if (checkDataDBFlight == null) throw new EntityExistsException("Flight with id " + id + " not found");
            log.info("Flight Found");
            return templateResponse.success(checkDataDBFlight);
        } catch (Exception e) {
            log.error("Error Getting Flight", e);
            throw e;
        }
    }
}
