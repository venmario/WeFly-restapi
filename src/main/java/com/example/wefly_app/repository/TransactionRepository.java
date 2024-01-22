package com.example.wefly_app.repository;

import com.example.wefly_app.entity.Airport;
import com.example.wefly_app.entity.Transaction;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TransactionRepository extends PagingAndSortingRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

}
