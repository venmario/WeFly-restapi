package com.example.wefly_app.service.impl;

import com.example.wefly_app.entity.Transaction;
import com.example.wefly_app.repository.TransactionRepository;
import com.example.wefly_app.request.transaction.ReportDTO;
import com.example.wefly_app.util.SimpleStringUtils;
import com.example.wefly_app.util.TemplateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl {
    private final TransactionRepository transactionRepository;
    private final SimpleStringUtils simpleStringUtils;
    private final TemplateResponse templateResponse;
    public ReportServiceImpl(TransactionRepository transactionRepository, SimpleStringUtils simpleStringUtils,
                             TemplateResponse templateResponse) {
        this.transactionRepository = transactionRepository;
        this.simpleStringUtils = simpleStringUtils;
        this.templateResponse = templateResponse;
    }

    public Map<Object, Object> getReport(int page, int size, String orderBy, String orderType,
                                      String startDate, String endDate, String period) {
        try{
            log.info("Get Report");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end;
            if (endDate == null || endDate.isEmpty()) {
                end = LocalDate.now();
            } else {
                end = LocalDate.parse(endDate, formatter);
            }
            Pageable showData = simpleStringUtils.getShort(orderBy, orderType, page, size);
            Page<ReportDTO> reportDTOPage = generateMonthlyReport(showData, start,
                    end, period);
            return templateResponse.success(reportDTOPage);
        } catch (DateTimeException e) {
            log.error("Get Report Error: " + e.getMessage());
            throw new DateTimeException("Invalid Date");
        }
    }

    public Page<ReportDTO> generateMonthlyReport(Pageable pageable, LocalDate startDate,
                                                 LocalDate finishDate, String period) {
        log.info("Generate Monthly Report");
        int dayInc;
        switch (period.toLowerCase()) {
            case "monthly":
            case "months":
            case "month":
                dayInc = 30;
                period = "Month";
                break;
            case "weekly":
            case "weeks":
            case "week":
                dayInc = 7;
                period = "Week";
                break;
            default:
                throw new IllegalArgumentException("Invalid Period");
        }
        log.info("Generate " + period + " Report");
        LocalDate endDate = startDate.plusDays(dayInc);
        List<ReportDTO> reportDTOList = new ArrayList<>();
        int periodCount = 1;
        while (startDate.isBefore(finishDate)) {
            log.info("start Date: " + startDate );
            log.info("end Date: " + endDate );
            String periodString = period + " " + periodCount;
            if (endDate.isAfter(finishDate)) {
                long daysDiff = ChronoUnit.DAYS.between(startDate, finishDate);
                endDate = startDate.plusDays(daysDiff);
                periodString = period + " " + periodCount + "| Last Period " + daysDiff + " Days";
            }
            ReportDTO reportDTO = transactionRepository.getTransactionReport(asDate(startDate), asDate(endDate));
            log.info("Income: " + reportDTO.getIncome());
            reportDTO.setPeriod(periodString);
            reportDTO.setStartDate(startDate);
            reportDTO.setEndDate(endDate);
            reportDTOList.add(reportDTO);
            startDate = endDate;
            endDate = endDate.plusDays(dayInc);
            periodCount++;
        }
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), reportDTOList.size());
        List<ReportDTO> subList = reportDTOList.subList(start, end);

        return new PageImpl<>(subList, pageable, reportDTOList.size());

    }

    public Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }
}
