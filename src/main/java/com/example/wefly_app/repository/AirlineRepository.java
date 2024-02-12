package com.example.wefly_app.repository;

import com.example.wefly_app.entity.Airline;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AirlineRepository extends JpaSpecificationExecutor<Airline>, PagingAndSortingRepository<Airline, Long> {
}
