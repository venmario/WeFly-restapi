package com.example.wefly_app.repository;

import com.example.wefly_app.entity.Airplane;
import com.example.wefly_app.entity.Flight;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface FlightRepository extends PagingAndSortingRepository<Flight, Long>, JpaSpecificationExecutor<Flight> {
    @Query(value = "select count(f) from Flight f WHERE f.airline.id = :airlineId")
    Long countByAirlineId(@Param("airlineId") Long airlineId);
}
