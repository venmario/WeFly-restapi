package com.example.wefly_app.repository;

import com.example.wefly_app.entity.Airplane;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface AirplaneRepository extends PagingAndSortingRepository<Airplane, Long>, JpaSpecificationExecutor<Airplane> {
    @Query("FROM Airplane a WHERE a.name = :name")
    Airplane checkExistingName(String name);

    @Query(value = "select count(a) from Airplane a WHERE a.name = :name")
    Long getSimilarName (@Param("name") String name);

    @Query("FROM Airplane a WHERE a.id = :id")
    Airplane checkExistingId(Long id);
}
