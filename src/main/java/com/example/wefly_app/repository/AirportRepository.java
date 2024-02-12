package com.example.wefly_app.repository;

import com.example.wefly_app.entity.Airport;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface AirportRepository extends PagingAndSortingRepository<Airport, Long>, JpaSpecificationExecutor<Airport> {
    @Query("FROM Airport a WHERE a.name = :name")
    Airport checkExistingName(String name);

    @Query(value = "select count(a) from Airport a WHERE a.name = :name")
    Long getSimilarName (@Param("name") String name);
}
