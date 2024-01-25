package com.example.wefly_app.repository;

import com.example.wefly_app.entity.FlightClass;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FlightClassRepository extends PagingAndSortingRepository<FlightClass, Long>, JpaSpecificationExecutor<FlightClass> {
}
