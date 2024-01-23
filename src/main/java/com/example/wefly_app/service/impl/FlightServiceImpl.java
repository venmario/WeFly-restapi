package com.example.wefly_app.service.impl;

import com.example.wefly_app.entity.Airplane;
import com.example.wefly_app.entity.Airport;
import com.example.wefly_app.entity.Flight;
import com.example.wefly_app.repository.AirplaneRepository;
import com.example.wefly_app.repository.AirportRepository;
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
import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class FlightServiceImpl implements FlightService {
    @Autowired
    private FlightRepository flightRepository;
    @Autowired
    private TemplateResponse templateResponse;
    @Autowired
    private SimpleStringUtils simpleStringUtils;
    @Autowired
    private AirportRepository airportRepository;
    @Autowired
    private AirplaneRepository airplaneRepository;
    
    @Override
    public Map<Object, Object> save(FlightRegisterModel request) {
        try {
            log.info("Save New Flight");
            Flight flight = new Flight();
            Optional<Airport> checkDataDBDepartureAirport = airportRepository.findById(request.getDepartureAirportId());
            if (!checkDataDBDepartureAirport.isPresent())
                throw new EntityNotFoundException("Departure Airport with id " + request.getDepartureAirportId() + " not found");
            Optional<Airport> checkDataDBArrivalAirport = airportRepository.findById(request.getArrivalAirportId());
            if (!checkDataDBArrivalAirport.isPresent())
                throw new EntityNotFoundException("Arrival Airport with id " + request.getArrivalAirportId() + " not found");
            Optional<Airplane> checkDataDBAirplane = airplaneRepository.findById(request.getAirplane());
            if (!checkDataDBAirplane.isPresent())
                throw new EntityNotFoundException("Airplane with id " + request.getAirplane() + " not found");
            flight.setDepartureDate(request.getDepartureDate());
            flight.setArrivalDate(request.getArrivalDate());
            flight.setDepartureAirport(checkDataDBDepartureAirport.get());
            flight.setArrivalAirport(checkDataDBArrivalAirport.get());
            flight.setAirplane(checkDataDBAirplane.get());
            flight.setBasePriceAdult(request.getBasePrice());
            flight.setBasePriceChild(request.getBasePrice().multiply(
                    checkDataDBAirplane.get().getAirline().getDiscountChild().multiply(BigDecimal.valueOf(0.01)))
            );
            flight.setBasePriceInfant(request.getBasePrice().multiply(
                    checkDataDBAirplane.get().getAirline().getDiscountInfant().multiply(BigDecimal.valueOf(0.01)))
            );

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
            Optional<Flight> checkDataDBFlight = flightRepository.findById(id);
            if (!checkDataDBFlight.isPresent()) throw new EntityNotFoundException("Flight with id " + id + " not found");
            int count = 0;
            if (request.getDepartureDate() != null) {
                checkDataDBFlight.get().setDepartureDate(request.getDepartureDate());
                count++;
            }
            if (request.getArrivalDate() != null) {
                checkDataDBFlight.get().setArrivalDate(request.getArrivalDate());
                count++;
            }
            if (request.getDepartureAirport() != null) {
                checkDataDBFlight.get().setDepartureAirport(request.getDepartureAirport());
                count++;
            }
            if (request.getArrivalAirport() != null) {
                checkDataDBFlight.get().setArrivalAirport(request.getArrivalAirport());
                count++;
            }
            if (request.getAirplane() != null) {
                checkDataDBFlight.get().setAirplane(request.getAirplane());
                count++;
            }
            if (count == 0) throw new IllegalArgumentException("No data to update");
            log.info("Flight Updated");
            return templateResponse.success(flightRepository.save(checkDataDBFlight.get()));
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
