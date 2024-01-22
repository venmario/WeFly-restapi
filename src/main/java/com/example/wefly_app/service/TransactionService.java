package com.example.wefly_app.service;

import com.example.wefly_app.request.transaction.TransactionSaveModel;

import java.util.Map;

public interface TransactionService {
    Map<Object, Object> save(TransactionSaveModel request);
    Map<Object,Object> delete(Long request);
    Map<Object, Object> getById(Long request);
    Map<Object, Object> getAll(int page, int size, String orderBy, String orderType
            , String startDate, String endDate, String status);
}
