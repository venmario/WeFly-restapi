package com.example.wefly_app.service;

import java.util.Map;

public interface ReportService {
    Map<Object, Object> getReport(int page, int size, String orderBy, String orderType,
                                  String startDate, String endDate, String period);
}
