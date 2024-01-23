package com.example.wefly_app.repository;

import com.example.wefly_app.entity.Airplane;
import com.example.wefly_app.entity.Flight;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FlightRepository extends PagingAndSortingRepository<Flight, Long>, JpaSpecificationExecutor<Flight> {
    @Query("FROM Flight f WHERE f.id = :id")
    Flight checkExistingId(Long id);

//    @Query("FROM Flight f WHERE f.flightNumber = :flightNumber")
//    Flight checkExistingFlightNumber(String flightNumber);
//
//    @Query("FROM Flight f WHERE f.departureAirport = :departureAirport")
//    Flight checkExistingDepartureAirport(String departureAirport);
//
//    @Query("FROM Flight f WHERE f.arrivalAirport = :arrivalAirport")
//    Flight checkExistingArrivalAirport(String arrivalAirport);
//
//    @Query("FROM Flight f WHERE f.airplane = :airplane")
//    Flight checkExistingAirplane(String airplane);
//
//    @Query("FROM Flight f WHERE f.departureDate = :departureDate")
//    Flight checkExistingDepartureDate(String departureDate);
//
//    @Query("FROM Flight f WHERE f.arrivalDate = :arrivalDate")
//    Flight checkExistingArrivalDate(String arrivalDate);
}
