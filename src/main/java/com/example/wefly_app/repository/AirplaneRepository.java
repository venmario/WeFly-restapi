package com.example.wefly_app.repository;

import com.example.wefly_app.entity.Airplane;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface AirplaneRepository extends PagingAndSortingRepository<Airplane, Long>, JpaSpecificationExecutor<Airplane> {
    @Query("FROM Airplane a WHERE a.code = :code")
    Airplane checkExistingName(String code);

    @Query(value = "select count(a) from Airplane a WHERE a.code = :code")
    Long getSimilarCode (@Param("code") String code);

    @Query(value = "select count(a) from Airplane a WHERE a.airline.id = :airlineId")
    Long countByAirlineId(@Param("airlineId") Long airlineId);


    @Query("FROM Airplane a WHERE a.id = :id")
    Airplane checkExistingId(Long id);
}
