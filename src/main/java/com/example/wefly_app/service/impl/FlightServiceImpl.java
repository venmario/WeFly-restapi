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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    private final TemplateResponse templateResponse;
    private SimpleStringUtils simpleStringUtils;
    private final AirportRepository airportRepository;
    private final AirplaneRepository airplaneRepository;
    @Autowired
    public FlightServiceImpl (FlightRepository flightRepository, TemplateResponse templateResponse,
                                SimpleStringUtils simpleStringUtils, AirportRepository airportRepository,
                                AirplaneRepository airplaneRepository){
        this.flightRepository = flightRepository;
        this.templateResponse = templateResponse;
        this.simpleStringUtils = simpleStringUtils;
        this.airportRepository = airportRepository;
        this.airplaneRepository = airplaneRepository;
    }
    
    @Override
    public Map<Object, Object> save(FlightRegisterModel request) {
        try {
            log.info("Save New Flight");
            Optional<Airport> checkDataDBDepartureAirport = airportRepository.findById(request.getDepartureAirportId());
            Optional<Airport> checkDataDBArrivalAirport = airportRepository.findById(request.getArrivalAirportId());
            Optional<Airplane> checkDataDBAirplane = airplaneRepository.findById(request.getAirplaneId());
            String missingEntities = Stream.of(
                    !checkDataDBAirplane.isPresent() ? "Airplane with id " + request.getAirplaneId() : null,
                    !checkDataDBArrivalAirport.isPresent() ? "Arrival Airport with id " + request.getArrivalAirportId() : null,
                    !checkDataDBDepartureAirport.isPresent() ? "Departure Airport with id " + request.getDepartureAirportId() : null
            ).filter(Objects::nonNull).collect(Collectors.joining(","));
            if (!missingEntities.isEmpty()) throw new EntityNotFoundException(missingEntities + " not found");
            Flight flight = new Flight();
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
            if (!checkDataDBFlight.isPresent()) throw new EntityExistsException("Flight with id " + id + " not found");
            int count = 0;
            if (request.getDepartureDate() != null) {
                checkDataDBFlight.get().setDepartureDate(request.getDepartureDate());
                count++;
            }
            if (request.getArrivalDate() != null) {
                checkDataDBFlight.get().setArrivalDate(request.getArrivalDate());
                count++;
            }
            if (request.getDepartureAirportId() != null) {
                Optional<Airport> checkDataDBDepartureAirport = airportRepository.findById(request.getDepartureAirportId());
                if (!checkDataDBDepartureAirport.isPresent()) {
                    throw new EntityNotFoundException("Departure Airport with id " + request.getDepartureAirportId() + " not found");
                } else {
                    checkDataDBFlight.get().setDepartureAirport(checkDataDBDepartureAirport.get());
                    count++;
                }
            }
            if (request.getArrivalAirportId() != null) {
                Optional<Airport> checkDataDBArrivalAirport = airportRepository.findById(request.getArrivalAirportId());
                if (!checkDataDBArrivalAirport.isPresent()) {
                    throw new EntityNotFoundException("Arrival Airport with id " + request.getArrivalAirportId() + " not found");
                } else {
                    checkDataDBFlight.get().setArrivalAirport(checkDataDBArrivalAirport.get());
                    count++;
                }
            }
            if (request.getAirplaneId() != null) {
                Optional<Airplane> checkDataDBAirplane = airplaneRepository.findById(request.getAirplaneId());
                if (!checkDataDBAirplane.isPresent()) {
                    throw new EntityNotFoundException("Airplane with id " + request.getAirplaneId() + " not found");
                } else {
                    checkDataDBFlight.get().setAirplane(checkDataDBAirplane.get());
                    count++;
                }
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
            Optional<Flight> checkDataDBFlight = flightRepository.findById(id);
            if (!checkDataDBFlight.isPresent()) throw new EntityExistsException("Flight with id " + id + " not found");
            flightRepository.delete(checkDataDBFlight.get());
            log.info("Flight Deleted");
            return templateResponse.success("Flight with id " + id + " deleted");
        } catch (Exception e) {
            log.error("Error Deleting Flight", e);
            throw e;
        }
    }

    @Override
    public Map<Object, Object> getAll(int page, int size, String orderBy, String orderType, Long departureAirportId,
                                      Long arrivalAirportId, Long airLineId, String departDate,
                                      String departureTime, String departureTimeTo, Integer numberOfPassengers) {
        try {
            log.info("Get All Flights");
            Pageable pageable = simpleStringUtils.getShort(orderBy, orderType, page, size);
            Specification<Flight> specification = (((root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                LocalDate depart = LocalDate.parse(departDate, formatter);
                predicates.add(criteriaBuilder.equal(root.get("departureDate"), depart));
                predicates.add(criteriaBuilder.equal(root.get("departureAirport").get("id"), departureAirportId));
                predicates.add(criteriaBuilder.equal(root.get("arrivalAirport").get("id"), arrivalAirportId));
                if (airLineId != null) {
                    predicates.add(criteriaBuilder.equal(root.get("airplane").get("airline").get("id"), airLineId));
                }
                if (departureTime != null && !departureTime.isEmpty() && departureTimeTo != null && !departureTimeTo.isEmpty()) {
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                    LocalTime departureTimeFormat = LocalTime.parse(departureTime, timeFormatter);
                    LocalTime departureTimeFormat2 = LocalTime.parse(departureTimeTo, timeFormatter);
                    predicates.add(criteriaBuilder.between(root.get("departureTime"), departureTimeFormat, departureTimeFormat2));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }));

            Page<Flight> list = flightRepository.findAll(specification, pageable);
            log.info("Flights Found");
            return templateResponse.success(list);
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
