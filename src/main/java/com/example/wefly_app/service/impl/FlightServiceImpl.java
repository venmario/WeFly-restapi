package com.example.wefly_app.service.impl;

import com.example.wefly_app.entity.*;
import com.example.wefly_app.entity.enums.SeatClass;
import com.example.wefly_app.repository.*;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
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
    private final AirlineRepository airlineRepository;
    private final AirplaneRepository airplaneRepository;
    private final FlightClassRepository flightClassRepository;
    @Autowired
    public FlightServiceImpl (FlightRepository flightRepository, TemplateResponse templateResponse,
                                SimpleStringUtils simpleStringUtils, AirportRepository airportRepository,
                                AirlineRepository airlineRepository, FlightClassRepository flightClassRepository,
                                AirplaneRepository airplaneRepository){
        this.flightRepository = flightRepository;
        this.templateResponse = templateResponse;
        this.simpleStringUtils = simpleStringUtils;
        this.airportRepository = airportRepository;
        this.airlineRepository = airlineRepository;
        this.flightClassRepository = flightClassRepository;
        this.airplaneRepository = airplaneRepository;
    }

    @Transactional
    @Override
    public Map<Object, Object> save(FlightRegisterModel request) {
        try {
            log.info("Save New Flight");
            Optional<Airport> checkDataDBDepartureAirport = airportRepository.findById(request.getDepartureAirportId());
            Optional<Airport> checkDataDBArrivalAirport = airportRepository.findById(request.getArrivalAirportId());
            Optional<Airline> checkDataDBAirline = airlineRepository.findById(request.getAirlineId());
            Optional<Airplane> checkDataDBAirplane = airplaneRepository.findById(request.getAirplaneId());
            String missingEntities = Stream.of(
                    !checkDataDBAirline.isPresent() ? "Airline with id " + request.getAirplaneId() : null,
                    !checkDataDBAirplane.isPresent() ? "Airplane with id " + request.getAirplaneId() : null,
                    !checkDataDBArrivalAirport.isPresent() ? "Arrival Airport with id " + request.getArrivalAirportId() : null,
                    !checkDataDBDepartureAirport.isPresent() ? "Departure Airport with id " + request.getDepartureAirportId() : null
            ).filter(Objects::nonNull).collect(Collectors.joining(","));
            if (!missingEntities.isEmpty()) throw new EntityNotFoundException(missingEntities + " not found");
            if (checkDataDBAirplane.get().getAirline().getId() != checkDataDBAirline.get().getId()) {
                throw new IllegalArgumentException("Airplane with id " + request.getAirplaneId() + " not belong to Airline with id " + request.getAirlineId());
            }
            Long count = flightRepository.countByAirlineId(checkDataDBAirline.get().getId());
            Flight flight = new Flight();
            flight.setFlightCode(checkDataDBAirline.get().getCode() + "-" + (count + 1));
            flight.setDepartureTime(request.getDepartureTime());
            flight.setArrivalTime(request.getArrivalTime());
            flight.setDepartureAirport(checkDataDBDepartureAirport.get());
            flight.setArrivalAirport(checkDataDBArrivalAirport.get());
            flight.setAirplane(checkDataDBAirplane.get());
            flight.setBasePrice(request.getBasePrice());
            flight.setScheduleMonday(request.isScheduleMonday());
            flight.setScheduleTuesday(request.isScheduleTuesday());
            flight.setScheduleWednesday(request.isScheduleWednesday());
            flight.setScheduleThursday(request.isScheduleThursday());
            flight.setScheduleFriday(request.isScheduleFriday());
            flight.setScheduleSaturday(request.isScheduleSaturday());
            flight.setScheduleSunday(request.isScheduleSunday());
            flight.setAirline(checkDataDBAirline.get());
            flight.setFlightSchedules(flightSchedules(flight, 3));
            log.info("Flight Saved");
            flightRepository.save(flight);
            return templateResponse.success("Flight Saved until 3 months from now");
        } catch (Exception e) {
            log.error("Error Saving Flight", e);
            throw e;
        }
    }

    public List<FlightSchedule> flightSchedules (Flight flight, int numberOfMonths) {
        List<FlightSchedule> flightSchedules = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        LocalDate endDate = currentDate.plusMonths(numberOfMonths);
        List<AirplaneSeatClass> airplaneSeatClasses = flight.getAirplane().getAirplaneSeatClasses();
        int dayInc = 0;
        if (flight.getDepartureTime().isBefore(LocalTime.parse("23:59")) && flight.getArrivalTime().isAfter(LocalTime.parse("23:59"))) {
            dayInc = 1;
        }
        for (DayOfWeek day : DayOfWeek.values()) {
            if (flight.operatesOn(day)) {
                LocalDate firstDate = currentDate.with(TemporalAdjusters.nextOrSame(day));
                for (LocalDate date = firstDate; date.isBefore(endDate); date = date.plusWeeks(1)) {
                    FlightSchedule flightSchedule = new FlightSchedule();
                    flightSchedule.setFlight(flight);
                    flightSchedule.setDepartureDate(date);
                    flightSchedule.setArrivalDate(date.plusDays(dayInc));
                    flightSchedule.setFlightClasses(flightClasses(flightSchedule, airplaneSeatClasses));
                    flightSchedules.add(flightSchedule);
                }
            }
        }
        return flightSchedules;
    }

//    public List<FlightClass> flightClasses (List<SeatConfig> seatConfigs, FlightSchedule flightSchedule) {
//        List<FlightClass> flightClasses = new ArrayList<>();
//        for (SeatConfig seatConfig : seatConfigs) {
//            flightClasses.add(flightClasses(flightSchedule, seatConfig));
//        }
//        return flightClasses;
//    }
//
//    public List<SeatAvailability> seatAvailabilities (List<SeatConfig> seatConfigs, FlightSchedule flightSchedule){
//        List<SeatAvailability> seatAvailabilities = new ArrayList<>();
//        for (SeatConfig seatConfig : seatConfigs) {
//            char rowLetter = 'A';
//            for (int i = 0; i < seatConfig.getSeatRow(); i++) {
//                rowLetter = (char) (rowLetter + i);
//                for (int j = 1; j <= seatConfig.getSeatColumn(); j++) {
//                    SeatAvailability seatAvailability = new SeatAvailability();
//                    seatAvailability.setSeatConfig(seatConfig);
//                    seatAvailability.setSeatRow(String.valueOf(rowLetter));
//                    seatAvailability.setSeatColumn(String.valueOf(j));
//                    seatAvailability.setFlightSchedule(flightSchedule);
//                    seatAvailability.setSeatClass(seatConfig.getSeatClass());
//                    seatAvailabilities.add(seatAvailability);
//                }
//            }
//        }
//
//        return seatAvailabilities;
//    }

    public List<FlightClass> flightClasses (FlightSchedule flightSchedule, List<AirplaneSeatClass> airplaneSeatClasses) {
        BigDecimal basePrice = flightSchedule.getFlight().getBasePrice();
        return airplaneSeatClasses.stream()
                .map(airplaneSeatClass -> {
            BigDecimal multiplier = BigDecimal.valueOf(1);
            SeatClass seatClass = airplaneSeatClass.getSeatClass();
            if (seatClass.equals(SeatClass.BUSINESS)) {
                multiplier = flightSchedule.getFlight().getAirplane().getAirline().getBusinessMultiplier();
            }
            FlightClass flightClass = new FlightClass();
            flightClass.setFlightSchedule(flightSchedule);
            flightClass.setSeatClass(seatClass);
            flightClass.setBasePriceAdult(basePrice.multiply(multiplier));
            flightClass.setBasePriceChild(basePrice.multiply(multiplier).
                    multiply(flightSchedule.getFlight().getAirplane().getAirline().getDiscountChild().
                            multiply(BigDecimal.valueOf(0.01)))
            );
            flightClass.setBasePriceInfant(basePrice.multiply(multiplier).
                    multiply(flightSchedule.getFlight().getAirplane().getAirline().getDiscountInfant().
                            multiply(BigDecimal.valueOf(0.01)))
            );
            flightClass.setAvailableSeat(airplaneSeatClass.getNumberOfRow() * airplaneSeatClass.getNumberOfColumn());
            return flightClass;
        }).collect(Collectors.toList());

    }

    @Transactional
    @Override
    public Map<Object, Object> update(FlightUpdateModel request, Long id) {
        try {
            log.info("Update Flight");
            Optional<Flight> checkDataDBFlight = flightRepository.findById(id);
            if (!checkDataDBFlight.isPresent()) throw new EntityExistsException("Flight with id " + id + " not found");
            int count = 0;
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
                checkDataDBFlight.get().getFlightSchedules().forEach(flightSchedule -> {
                    flightSchedule.getFlightClasses().forEach(flightClass -> {
                        flightClass.setBasePriceAdult(flightClass.getBasePriceAdult().
                                divide(checkDataDBFlight.get().getBasePrice(), 2,RoundingMode.HALF_UP).multiply(request.getBasePrice()));
                        flightClass.setBasePriceChild(flightClass.getBasePriceChild().
                                divide(checkDataDBFlight.get().getBasePrice(), 2,RoundingMode.HALF_UP).multiply(request.getBasePrice()));
                        flightClass.setBasePriceInfant(request.getBasePrice().
                                divide(checkDataDBFlight.get().getBasePrice(), 2,RoundingMode.HALF_UP).multiply(request.getBasePrice()));
                    });
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
                                      Long arrivalAirportId, Long airLineId, String departureDate,
                                      String departureTime, String arrivalTime, Integer numberOfPassengers,
                                      String seatClass) {
        try {
            log.info("Get All Flights");
            Pageable pageable = simpleStringUtils.getShort(orderBy, orderType, page, size);
            Specification<FlightClass> specification = ((root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                LocalDate depart = LocalDate.parse(departureDate, formatter);
                predicates.add(criteriaBuilder.equal(root.get("flightSchedule").get("departureDate"), depart));
                predicates.add(criteriaBuilder.equal(root.get("flightSchedule").get("flight").get("departureAirport").get("id"), departureAirportId));
                predicates.add(criteriaBuilder.equal(root.get("flightSchedule").get("flight").get("arrivalAirport").get("id"), arrivalAirportId));
                predicates.add(criteriaBuilder.equal(root.get("seatClass"), SeatClass.valueOf(seatClass)));
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("availableSeat"), numberOfPassengers));
                if (airLineId != null) {
                    predicates.add(criteriaBuilder.equal(root.get("flightSchedule").get("flight").get("airline").get("id"), airLineId));
                }
                if (departureTime != null && !departureTime.isEmpty() && arrivalTime != null && !arrivalTime.isEmpty()) {
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                    LocalTime departureTimeFormat = LocalTime.parse(departureTime, timeFormatter);
                    LocalTime arrivalTimeFormat = LocalTime.parse(arrivalTime, timeFormatter);
                    predicates.add(criteriaBuilder.between(root.get("flightSchedule").get("flight").
                            get("departureTime"), departureTimeFormat, arrivalTimeFormat));
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

//    private List<FlightClass> setFlightClass (Airplane airplane, Flight flight, BigDecimal basePrice) {
//        return airplane.getSeats().stream()
//                .map(seatClass -> {
//                    FlightClass flightClass = new FlightClass();
//                    flightClass.setFlight(flight);
//                    flightClass.setSeatClass(seatClass.getSeatClass());
//                    BigDecimal multiplier = BigDecimal.valueOf(1);
//                    if (seatClass.getSeatClass().equals(SeatClass.BUSINESS)) {
//                        multiplier = airplane.getAirline().getBusinessMultiplier();
//                    }
//                    flightClass.setBasePriceAdult(basePrice.multiply(multiplier));
//                    flightClass.setBasePriceChild(basePrice.multiply(multiplier).
//                            multiply(airplane.getAirline().getDiscountChild().
//                                    multiply(BigDecimal.valueOf(0.01)))
//                    );
//                    flightClass.setBasePriceInfant(basePrice.multiply(multiplier).
//                            multiply(airplane.getAirline().getDiscountInfant().
//                                    multiply(BigDecimal.valueOf(0.01)))
//                    );
//                    flightClass.setAvailableSeat(seatClass.getSeatRow() * seatClass.getSeatColumn());
//                    return flightClass;
//                }).collect(Collectors.toList());
//    }
}
