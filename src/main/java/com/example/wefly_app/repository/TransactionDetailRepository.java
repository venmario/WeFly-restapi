package com.example.wefly_app.repository;

import com.example.wefly_app.entity.Transaction;
import com.example.wefly_app.entity.TransactionDetail;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TransactionDetailRepository extends PagingAndSortingRepository<TransactionDetail, Long>, JpaSpecificationExecutor<TransactionDetail> {
}
