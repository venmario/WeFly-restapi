package com.example.wefly_app.repository;

import com.example.wefly_app.entity.Airplane;
import com.example.wefly_app.entity.Flight;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FlightRepository extends PagingAndSortingRepository<Flight, Long>, JpaSpecificationExecutor<Flight> {
}
