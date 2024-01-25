package com.example.wefly_app.repository;

import com.example.wefly_app.entity.Payment;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PaymentRepository extends PagingAndSortingRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {
}
