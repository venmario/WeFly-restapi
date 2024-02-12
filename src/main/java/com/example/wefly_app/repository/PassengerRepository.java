package com.example.wefly_app.repository;

import com.example.wefly_app.entity.Passenger;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PassengerRepository extends PagingAndSortingRepository<Passenger, Long> {
}
