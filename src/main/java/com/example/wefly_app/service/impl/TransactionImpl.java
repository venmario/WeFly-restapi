package com.example.wefly_app.service.impl;

import com.example.wefly_app.entity.*;
import com.example.wefly_app.repository.FlightRepository;
import com.example.wefly_app.repository.TransactionRepository;
import com.example.wefly_app.repository.UserRepository;
import com.example.wefly_app.request.transaction.TransactionSaveModel;
import com.example.wefly_app.service.TransactionService;
import com.example.wefly_app.util.SimpleStringUtils;
import com.example.wefly_app.util.TemplateResponse;
import com.example.wefly_app.util.exception.IncorrectUserCredentialException;
import com.example.wefly_app.util.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionImpl implements TransactionService {
    @Autowired
    public TransactionRepository transactionRepository;
    @Autowired
    public UserRepository userRepository;
    @Autowired
    public FlightRepository flightRepository;
    @Autowired
    public TemplateResponse templateResponse;
    @Autowired
    public SimpleStringUtils simpleStringUtils;

    @Transactional
    @Override
    public Map<Object, Object> save(TransactionSaveModel request) {
        try {
            log.info("save transaction");
            if (request.getInfantPassenger() > request.getAdultPassenger()) {
                throw new ValidationException("The number of infant passengers cannot exceed the total number of adult passengers");
            }
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            Long userId = (Long) attributes.getRequest().getAttribute("userId");
            Optional<User> checkDataDBUser = userRepository.findById(userId);
            if (!checkDataDBUser.isPresent()) {
                log.error("Save transaction error : unidentified user");
                throw new IncorrectUserCredentialException("unidentified token user");
            }

            Orderer orderer = new Orderer();
            orderer.setFirstName(request.getOrderer().getFirstName());
            orderer.setLastName(request.getOrderer().getLastName());
            orderer.setEmail(request.getOrderer().getEmail());
            orderer.setPhoneNumber(request.getOrderer().getPhoneNumber());

            Transaction transaction = new Transaction();
            List<TransactionDetail> transactionDetails = request.getTransactionDetails().stream()
                    .map(transactionDetail -> {
                                TransactionDetail transactionDetail1 = new TransactionDetail();
                                Flight checkDataDBFlight = flightRepository.findById(transactionDetail.getFlightId())
                                                .orElseThrow(() -> new EntityNotFoundException("Flight not found"));
                                transactionDetail1.setFlight(checkDataDBFlight);
                                transactionDetail1.setTotalPriceAdult(transactionDetail.getTotalPriceAdult());
                                transactionDetail1.setTotalPriceChild(transactionDetail.getTotalPriceChild());
                                transactionDetail1.setTotalPriceInfant(transactionDetail.getTotalPriceInfant());
                                transactionDetail1.setTransaction(transaction);
                                return transactionDetail1;
                            })
                    .collect(Collectors.toList());
            BigDecimal totalPrice = transactionDetails.stream()
                    .map(transactionDetail -> transactionDetail.getTotalPriceAdult()
                            .add(transactionDetail.getTotalPriceChild())
                            .add(transactionDetail.getTotalPriceInfant())
                    )
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            transaction.setTransactionDetails(transactionDetails);
            transaction.setTotalPrice(totalPrice);
            transaction.setOrderer(orderer);
            transaction.setUser(checkDataDBUser.get());
            transaction.setAdultPassenger(request.getAdultPassenger());
            transaction.setChildPassenger(request.getChildPassenger());
            transaction.setInfantPassenger(request.getInfantPassenger());
            List<Passenger> passengers = request.getPassengers().stream()
                            .map(passenger -> {
                                Passenger passenger1 = new Passenger();
                                passenger1.setFirstName(passenger.getFirstName());
                                passenger1.setLastName(passenger.getLastName());
                                passenger1.setDateOfBirth(passenger.getDateOfBirth());
                                passenger1.setTransaction(transaction);
                                return passenger1;
                            })
                    .collect(Collectors.toList());
            transaction.setPassengers(passengers);
            log.info("save transaction success, proceed to payment");
            return templateResponse.success(transactionRepository.save(transaction));
        } catch (Exception e) {
            log.error("Save transaction error ", e);
            throw e;
        }
    }

    @Override
    public Map<Object, Object> delete(Long request) {
        try {
            log.info("delete transaction");
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            Long userId = (Long) attributes.getRequest().getAttribute("userId");
            System.out.println("userId = " + userId);
            Optional<User> checkDataDBUser = userRepository.findById(userId);
            if (!checkDataDBUser.isPresent()) {
                log.error("Save transaction error : unidentified user");
                throw new IncorrectUserCredentialException("unidentified token user");
            }
            Optional<Transaction> checkDataDBTransaction = transactionRepository.findById(request);
            if (checkDataDBTransaction.get().getUser().getId() != userId) throw new ValidationException("transaction not found");
            checkDataDBTransaction.get().setDeletedDate(new Date());

            log.info("transaction deleted");
            return templateResponse.success(checkDataDBTransaction.get());
        } catch (Exception e) {
            log.error("Transaction deletion error ", e);
            throw e;
        }
    }

    @Override
    public Map<Object, Object> getById(Long request) {
        try {
            log.info("get transaction");
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            Long userId = (Long) attributes.getRequest().getAttribute("userId");
            Optional<User> checkDataDBUser = userRepository.findById(userId);
            if (!checkDataDBUser.isPresent()) {
                log.error("Save transaction error : unidentified user");
                throw new IncorrectUserCredentialException("unidentified token user");
            }
            Optional<Transaction> checkDataDBTransaction = transactionRepository.findById(request);
            if (checkDataDBTransaction.get().getUser().getId() != userId) throw new ValidationException("transaction not found");

            log.info("get transaction succeed");
            return templateResponse.success(checkDataDBTransaction.get());
        } catch (Exception e) {
            log.error("get transaction error ", e);
            throw e;
        }
    }

    @Override
    public Map<Object, Object> getAll(int page, int size, String orderBy, String orderType, String startDate, String endDate, String status) {
        try {
            log.info("get all transaction");
            Pageable pageable = simpleStringUtils.getShort(orderBy, orderType, page, size);
            Specification<Transaction> specification = ((root, criteriaQuery, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    LocalDate start = LocalDate.parse(startDate, formatter);
                    LocalDate end = LocalDate.parse(endDate, formatter);
                    predicates.add(criteriaBuilder.between(root.get("createdDate").as(LocalDate.class), start, end));
                    log.info("filtering by date range");
                }
                if (status != null && !status.isEmpty()) {
                    Status statusEnum = Status.valueOf(status);
                    predicates.add(criteriaBuilder.equal(root.get("status"), statusEnum));
                    log.info("filtering by status");
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            });

            Page<Transaction> list = transactionRepository.findAll(specification, pageable);
            Map<Object, Object> map = new HashMap<>();
            map.put("data", list);
            log.info("get all transaction succeed");
            return map;
        } catch (Exception e) {
            log.error("get all transaction error ", e);
            throw e;
        }
    }


}
