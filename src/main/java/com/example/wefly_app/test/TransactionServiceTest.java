package com.example.wefly_app.test;

import com.example.wefly_app.entity.Passenger;
import com.example.wefly_app.entity.Transaction;
import com.example.wefly_app.repository.TransactionRepository;
import com.example.wefly_app.request.transaction.InvoiceDTO;
import org.hibernate.Hibernate;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TransactionServiceTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    public Transaction getInvoiceDTO(Long transactionId) {
        Optional<Transaction> checkDBTransaction = transactionRepository.findById(transactionId);
        if (!checkDBTransaction.isPresent()) {
            throw new RuntimeException("Transaction not found");
        }
//        ModelMapper modelMapper = new ModelMapper();
//        Transaction transaction = checkDBTransaction.get();
//        Hibernate.initialize(transaction.getTransactionDetails());
//        InvoiceDTO invoiceDTO = modelMapper.map(transaction, InvoiceDTO.class);
//        Map<String, Integer> transactionMap = new LinkedHashMap<>();
//        transactionMap.put("Adult", transaction.getAdultPassenger());
//        transactionMap.put("Child", transaction.getChildPassenger());
//        transactionMap.put("Infant", transaction.getInfantPassenger());
//        invoiceDTO.setTransaction(transactionMap);
        return checkDBTransaction.get();
    }

    @Transactional
    public List<Passenger> getPassengerList(Long transactionId) {
        Optional<Transaction> checkDBTransaction = transactionRepository.findById(transactionId);
        if (!checkDBTransaction.isPresent()) {
            throw new RuntimeException("Transaction not found");
        }
        Hibernate.initialize(checkDBTransaction.get().getPassengers());
        return checkDBTransaction.get().getPassengers();
    }
}
