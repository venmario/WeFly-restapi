package com.example.wefly_app.service.quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class QuartzJobService {
    @Autowired
    private Scheduler scheduler;

    public void schedulePaymentExpire(Long transactionId) {
        log.info("Scheduling payment expire job for transaction ID: {}", transactionId);
        JobDetail jobDetail = JobBuilder.newJob(PaymentExpire.class)
                .withIdentity("paymentExpireJob" + transactionId, "payments")
                .usingJobData("transactionId", transactionId)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity("paymentExpireTrigger" + transactionId, "payments")
                .startAt(DateBuilder.futureDate(1, DateBuilder.IntervalUnit.HOUR))
                .build();
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            log.error("Error scheduling payment expire job", e);
            e.printStackTrace();
        }
    }

    public void cancelJob(Long transactionId) {
        log.info("Cancelling payment expire job for transaction ID: {}", transactionId);
        try {
            scheduler.deleteJob(new JobKey("paymentExpireJob" + transactionId, "payments"));
        } catch (SchedulerException e) {
            log.error("Error cancelling payment expire job", e);
            e.printStackTrace();
        }
        log.info("Payment expire job for transaction ID: {} has been cancelled", transactionId);
    }
}
