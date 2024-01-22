package com.example.wefly_app.repository;

import com.example.wefly_app.entity.Flight;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FlightRepository extends PagingAndSortingRepository<Flight, Long> {
}
