package com.example.wefly_app.repository;

import com.example.wefly_app.entity.Bank;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface BankRepository extends PagingAndSortingRepository<Bank, Long>, JpaSpecificationExecutor<Bank> {
}
