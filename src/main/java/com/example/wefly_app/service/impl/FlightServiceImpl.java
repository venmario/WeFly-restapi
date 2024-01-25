package com.example.wefly_app.service.impl;

import com.example.wefly_app.entity.Airplane;
import com.example.wefly_app.entity.Airport;
import com.example.wefly_app.entity.Flight;
import com.example.wefly_app.entity.FlightClass;
import com.example.wefly_app.entity.enums.SeatClass;
import com.example.wefly_app.repository.AirplaneRepository;
import com.example.wefly_app.repository.AirportRepository;
import com.example.wefly_app.repository.FlightClassRepository;
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
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final SimpleStringUtils simpleStringUtils;
    private final AirportRepository airportRepository;
    private final AirplaneRepository airplaneRepository;
    private final FlightClassRepository flightClassRepository;
    @Autowired
    public FlightServiceImpl (FlightRepository flightRepository, TemplateResponse templateResponse,
                                SimpleStringUtils simpleStringUtils, AirportRepository airportRepository,
                                AirplaneRepository airplaneRepository, FlightClassRepository flightClassRepository){
        this.flightRepository = flightRepository;
        this.templateResponse = templateResponse;
        this.simpleStringUtils = simpleStringUtils;
        this.airportRepository = airportRepository;
        this.airplaneRepository = airplaneRepository;
        this.flightClassRepository = flightClassRepository;
    }

    @Transactional
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
            flight.setDepartureTime(request.getDepartureTime());
            flight.setArrivalTime(request.getArrivalTime());
            flight.setDepartureAirport(checkDataDBDepartureAirport.get());
            flight.setArrivalAirport(checkDataDBArrivalAirport.get());
            flight.setAirplane(checkDataDBAirplane.get());
            flight.setBasePrice(request.getBasePrice());
            flight.setFlightClasses(setFlightClass(checkDataDBAirplane.get(), flight, request.getBasePrice()));

            log.info("Flight Saved");
            return templateResponse.success(flightRepository.save(flight));
        } catch (Exception e) {
            log.error("Error Saving Flight", e);
            throw e;
        }
    }

    @Transactional
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
            if (request.getDepartureTime() != null) {
                checkDataDBFlight.get().setDepartureTime(request.getDepartureTime());
                count++;
            }
            if (request.getArrivalTime() != null) {
                checkDataDBFlight.get().setArrivalTime(request.getArrivalTime());
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
            if (request.getBasePrice() != null) {
                checkDataDBFlight.get().getFlightClasses().forEach(flightClass -> {
                    flightClass.setBasePriceAdult(flightClass.getBasePriceAdult().
                            divide(checkDataDBFlight.get().getBasePrice(), 2,RoundingMode.HALF_UP).multiply(request.getBasePrice()));
                    flightClass.setBasePriceChild(flightClass.getBasePriceChild().
                            divide(checkDataDBFlight.get().getBasePrice(), 2,RoundingMode.HALF_UP).multiply(request.getBasePrice()));
                    flightClass.setBasePriceInfant(request.getBasePrice().
                            divide(checkDataDBFlight.get().getBasePrice(), 2,RoundingMode.HALF_UP).multiply(request.getBasePrice()));
                });
                checkDataDBFlight.get().setBasePrice(request.getBasePrice());
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
            Optional<Flight> checkDataDBFlight = flightRepository.findById(id);
            if (!checkDataDBFlight.isPresent()) throw new EntityExistsException("Flight with id " + id + " not found");
            checkDataDBFlight.get().setDeletedDate(new Date());
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
                                      String departureTime, String arrivalTime, Integer numberOfPassengers,
                                      String seatClass) {
        try {
            log.info("Get All Flights");
            Pageable pageable = simpleStringUtils.getShort(orderBy, orderType, page, size);
            Specification<FlightClass> specification = ((root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                LocalDate depart = LocalDate.parse(departDate, formatter);
                predicates.add(criteriaBuilder.equal(root.get("flight").get("departureDate"), depart));
                predicates.add(criteriaBuilder.equal(root.get("flight").get("departureAirport").get("id"), departureAirportId));
                predicates.add(criteriaBuilder.equal(root.get("flight").get("arrivalAirport").get("id"), arrivalAirportId));
                predicates.add(criteriaBuilder.equal(root.get("seatClass"), SeatClass.valueOf(seatClass)));
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("availableSeat"), numberOfPassengers));
                if (airLineId != null) {
                    predicates.add(criteriaBuilder.equal(root.get("flight").get("airplane").get("airline").get("id"), airLineId));
                }
                if (departureTime != null && !departureTime.isEmpty() && arrivalTime != null && !arrivalTime.isEmpty()) {
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                    LocalTime departureTimeFormat = LocalTime.parse(departureTime, timeFormatter);
                    LocalTime arrivalTimeFormat = LocalTime.parse(arrivalTime, timeFormatter);
                    predicates.add(criteriaBuilder.between(root.get("flight").get("departureTime"), departureTimeFormat, arrivalTimeFormat));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            });

            Page<FlightClass> list = flightClassRepository.findAll(specification, pageable);
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
            Optional<Flight> checkDataDBFlight = flightRepository.findById(id);
            if (!checkDataDBFlight.isPresent()) throw new EntityNotFoundException("Flight with id " + id + " not found");
            log.info("Flight Found");
            return templateResponse.success(checkDataDBFlight.get());
        } catch (Exception e) {
            log.error("Error Getting Flight", e);
            throw e;
        }
    }

    private List<FlightClass> setFlightClass (Airplane airplane, Flight flight, BigDecimal basePrice) {
        return airplane.getSeats().stream()
                .map(seatClass -> {
                    FlightClass flightClass = new FlightClass();
                    flightClass.setFlight(flight);
                    flightClass.setSeatClass(seatClass.getSeatClass());
                    BigDecimal multiplier = BigDecimal.valueOf(1);
                    if (seatClass.getSeatClass().equals(SeatClass.BUSINESS)) {
                        multiplier = airplane.getAirline().getBusinessMultiplier();
                    }
                    flightClass.setBasePriceAdult(basePrice.multiply(multiplier));
                    flightClass.setBasePriceChild(basePrice.multiply(multiplier).
                            multiply(airplane.getAirline().getDiscountChild().
                                    multiply(BigDecimal.valueOf(0.01)))
                    );
                    flightClass.setBasePriceInfant(basePrice.multiply(multiplier).
                            multiply(airplane.getAirline().getDiscountInfant().
                                    multiply(BigDecimal.valueOf(0.01)))
                    );
                    flightClass.setAvailableSeat(seatClass.getSeatRow() * seatClass.getSeatColumn());
                    return flightClass;
                }).collect(Collectors.toList());
    }
}
