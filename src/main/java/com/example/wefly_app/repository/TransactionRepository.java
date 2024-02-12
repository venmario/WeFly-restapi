package com.example.wefly_app.repository;

import com.example.wefly_app.entity.Airport;
import com.example.wefly_app.entity.Transaction;
import com.example.wefly_app.request.transaction.ReportDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Date;

public interface TransactionRepository extends PagingAndSortingRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    @Query("SELECT new com.example.wefly_app.request.transaction.ReportDTO(" +
            "COUNT(t.id), " +
            "COUNT(CASE WHEN p.transactionStatus = 'PAID' THEN 1 ELSE null END), " +
            "COUNT(CASE WHEN p.transactionStatus != 'PAID' THEN 1 ELSE null END), " +
            "SUM(CASE WHEN p.transactionStatus = 'PAID' THEN p.grossAmount ELSE 0 END), " +
            "SUM(CASE WHEN p.transactionStatus != 'PAID' THEN p.grossAmount ELSE 0 END)) " +
            "FROM Transaction t JOIN t.payment p " +
            "WHERE t.createdDate BETWEEN :startDate AND :endDate")
    ReportDTO getTransactionReport(@Param("startDate") Date startDate,
                                   @Param("endDate") Date endDate);

}
