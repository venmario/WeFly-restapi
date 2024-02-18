package com.example.wefly_app.service.quartz;

import com.example.wefly_app.entity.FlightClass;
import com.example.wefly_app.entity.Payment;
import com.example.wefly_app.entity.Transaction;
import com.example.wefly_app.repository.FlightClassRepository;
import com.example.wefly_app.repository.PaymentRepository;
import com.example.wefly_app.repository.TransactionDetailRepository;
import com.example.wefly_app.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Slf4j
public class PaymentExpire extends QuartzJobBean {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private TransactionDetailRepository transactionDetailRepository;
    @Autowired
    private FlightClassRepository flightClassRepository;

    @Transactional
    @Override
    public void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("Payment Expire Job is running");
        JobDataMap dataMap = context.getMergedJobDataMap();
        Long transactionId = dataMap.getLong("transactionId");
        Optional<Transaction> checkDBTransaction = transactionRepository.findById(transactionId);
        if (checkDBTransaction.isPresent()) {
            Payment payment = checkDBTransaction.get().getPayment();
            if (payment.getTransactionStatus().equals("CHOOSING_PAYMENT")) {
                log.info("Reverting flight class seat available");
                int totalSeatBooked = checkDBTransaction.get().getAdultPassenger()
                        + checkDBTransaction.get().getChildPassenger();
                transactionDetailRepository.findByTransactionId(transactionId)
                        .forEach(transactionDetail -> {
                            FlightClass flightClass = transactionDetail.getFlightClass();
                            flightClass.setAvailableSeat(flightClass.getAvailableSeat() + totalSeatBooked);
                            flightClassRepository.save(flightClass);
                        });
                log.info("Revert success");
                payment.setTransactionStatus("EXPIRE");
                paymentRepository.save(payment);
                log.info("Payment with ID: {} has been expired", transactionId);
            }
        }
    }
}
